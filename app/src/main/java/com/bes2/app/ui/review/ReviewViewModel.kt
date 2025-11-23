package com.bes2.app.ui.review

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bes2.background.worker.DailyCloudSyncWorker
import com.bes2.background.worker.PhotoAnalysisWorker
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.ImageClusterEntity
import com.bes2.data.model.ImageItemEntity
import com.bes2.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModel @Inject constructor(
    private val imageClusterDao: ImageClusterDao,
    private val imageItemDao: ImageItemDao,
    private val settingsRepository: SettingsRepository,
    private val workManager: WorkManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReviewUiState>(ReviewUiState.Loading)
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    // Session statistics
    private var sessionClusterCount = 0
    private var sessionSavedImageCount = 0
    
    // User's manual selection state. If null, auto-selection is active.
    private var manualSelectionIds: List<Long>? = null

    // SharedPreference Key and Threshold
    private val PREF_KEY_REVIEW_COUNT = "pref_review_accumulated_count"
    // TEST VALUE: 20 (Release: 100)
    private val AD_THRESHOLD = 20

    // Track current cluster processed count to add to accumulated count later
    private var currentClusterProcessedCount = 0

    init {
        Timber.d("ReviewViewModel init")
        viewModelScope.launch {
            imageClusterDao.getImageClustersByReviewStatus("PENDING_REVIEW")
                .flatMapLatest { clusters ->
                    Timber.d("PENDING_REVIEW clusters found: ${clusters.size}")
                    if (clusters.isEmpty()) {
                        // Reset manual selection for new session/empty state
                        manualSelectionIds = null
                        
                        // Check for background prepared clusters
                        val prepared = promoteBackgroundClusters()
                        if (prepared) {
                             // If promoted, the Flow will emit again with new PENDING_REVIEW clusters
                             flowOf(ReviewUiState.Loading)
                        } else {
                            // Really finished
                            Timber.w("No clusters and no ready-to-clean images. Finishing.")
                            schedulePostReviewSync()
                            
                            val showAd = checkAdCondition()
                            _navigationEvent.emit(NavigationEvent.NavigateToHome(sessionClusterCount, sessionSavedImageCount, showAd))
                            
                            flowOf(ReviewUiState.NoClustersToReview)
                        }
                    } else {
                        val currentCluster = clusters.first()
                        Timber.d("Processing cluster ID: ${currentCluster.id}")
                        
                        imageItemDao.getImageItemsByClusterId(currentCluster.id)
                            .filter { it.isNotEmpty() }
                            .map { images ->
                                val validStatus = setOf("ANALYZED", "READY_TO_CLEAN", "STATUS_REJECTED")
                                val candidates = images.filter { it.status in validStatus }
                                calculateReadyState(currentCluster, candidates)
                            }
                    }
                }
                .collect { newState -> _uiState.value = newState }
        }
    }

    private suspend fun promoteBackgroundClusters(): Boolean {
        val readyItems = imageItemDao.getImageItemsListByStatus("READY_TO_CLEAN")
        if (readyItems.isEmpty()) return false
        
        val clusterIds = readyItems.mapNotNull { it.clusterId }.distinct()
        
        if (clusterIds.isNotEmpty()) {
            val targetClusterId = clusterIds.first()
            val clusterFlow = imageClusterDao.getImageClusterById(targetClusterId)
            val cluster = clusterFlow.first()
            
            if (cluster != null) {
                imageClusterDao.updateImageClusterReviewStatus(targetClusterId, "PENDING_REVIEW")
                Timber.d("Promoted cluster $targetClusterId to PENDING_REVIEW")
                return true
            } else {
                Timber.w("Found READY_TO_CLEAN items but cluster $targetClusterId not found in DB.")
                return false
            }
        }
        return false
    }

    fun selectImage(imageToSelect: ImageItemEntity) {
        val currentState = _uiState.value
        if (currentState is ReviewUiState.Ready) {
            if (imageToSelect.status == "STATUS_REJECTED") {
                restoreRejectedImage(imageToSelect)
                return
            }
            
            if (imageToSelect.status != "ANALYZED" && imageToSelect.status != "READY_TO_CLEAN") return

            // Determine current selection
            val currentSelection = if (manualSelectionIds == null) {
                // Initialize from current Auto Selection
                listOfNotNull(currentState.selectedBestImage, currentState.selectedSecondBestImage)
            } else {
                val allImgs = currentState.allImages
                manualSelectionIds!!.mapNotNull { id -> allImgs.find { it.id == id } }
            }

            val alreadySelected = currentSelection.any { it.id == imageToSelect.id }

            val newSelection = if (alreadySelected) {
                currentSelection.filterNot { it.id == imageToSelect.id }
            } else {
                if (currentSelection.size < 2) {
                    currentSelection + imageToSelect
                } else {
                    val sortedSelection = currentSelection.sortedBy { calculateFinalScore(it) }
                    listOf(sortedSelection.last(), imageToSelect)
                }
            }

            // Update state
            manualSelectionIds = newSelection.map { it.id }
            _uiState.value = calculateReadyState(currentState.cluster, currentState.allImages)
        }
    }
    
    private fun restoreRejectedImage(image: ImageItemEntity) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ReviewUiState.Ready && manualSelectionIds == null) {
                manualSelectionIds = listOfNotNull(currentState.selectedBestImage?.id, currentState.selectedSecondBestImage?.id)
            }

            val restoredImage = image.copy(status = "ANALYZED")
            imageItemDao.updateImageItem(restoredImage)
            Timber.d("Restored rejected image: ${image.id}")
        }
    }
    
    private fun calculateFinalScore(image: ImageItemEntity): Float {
        val nimaScore = (image.nimaScore ?: 0f) * 10
        val smileProb = image.smilingProbability ?: 0f
        
        val smileBonus = if (smileProb < 0.1f) {
            -10f 
        } else {
            smileProb * 30f
        }
        
        return nimaScore + smileBonus
    }

    private fun calculateReadyState(cluster: ImageClusterEntity, allImages: List<ImageItemEntity>): ReviewUiState.Ready {
        val (analyzedImages, rejectedImages) = allImages.partition { 
            it.status == "ANALYZED" || it.status == "READY_TO_CLEAN" 
        }

        val (finalBest, finalSecond) = if (manualSelectionIds == null) {
            val sortedCandidates = analyzedImages.sortedByDescending { calculateFinalScore(it) }
            Pair(sortedCandidates.getOrNull(0), sortedCandidates.getOrNull(1))
        } else {
            val selectedObjs = manualSelectionIds!!.mapNotNull { id -> analyzedImages.find { it.id == id } }
            val sortedSelected = selectedObjs.sortedByDescending { calculateFinalScore(it) }
            Pair(sortedSelected.getOrNull(0), sortedSelected.getOrNull(1))
        }
        
        val selectedUris = setOfNotNull(finalBest?.uri, finalSecond?.uri)
        
        val otherImages = analyzedImages
            .filterNot { it.uri in selectedUris }
            .sortedByDescending { calculateFinalScore(it) }

        return ReviewUiState.Ready(
            cluster = cluster,
            allImages = allImages,
            otherImages = otherImages,
            rejectedImages = rejectedImages,
            selectedBestImage = finalBest,
            selectedSecondBestImage = finalSecond,
            pendingDeleteRequest = null
        )
    }

    private suspend fun schedulePostReviewSync() {
        val settings = settingsRepository.storedSettings.first()
        if (settings.syncOption == "NONE" || settings.syncOption == "DAILY") {
            Timber.d("Post-review sync skipped for sync option: ${settings.syncOption}")
            return
        }

        val delayInMillis = if (settings.syncOption == "DELAYED") {
            TimeUnit.HOURS.toMillis(settings.syncDelayHours.toLong()) + TimeUnit.MINUTES.toMillis(settings.syncDelayMinutes.toLong())
        } else {
            0L // IMMEDIATE
        }

        val constraints = if (settings.uploadOnWifiOnly) {
            Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()
        } else {
            Constraints.NONE
        }
        
        val inputData = Data.Builder().putBoolean(DailyCloudSyncWorker.KEY_IS_ONE_TIME_SYNC, true).build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<DailyCloudSyncWorker>()
            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()
        
        workManager.enqueue(syncWorkRequest)

        Timber.d("Enqueued post-review sync with option: ${settings.syncOption}, Delay: $delayInMillis ms, Wi-Fi only: ${settings.uploadOnWifiOnly}")
    }

    fun deleteOtherImages() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ReviewUiState.Ready) {
                updateSessionStats(currentState)
                
                // Calculate total processed count for this cluster (kept + deleted)
                // Kept: Best 1/2
                // Deleted: Others + Rejected
                // Actually, total processed = allImages.size (assuming all are dealt with)
                // Or strictly what is acted upon.
                // 'deleteOtherImages' deletes 'other' and 'rejected'. 'Best' are kept.
                // So effectively all images in the cluster are processed.
                currentClusterProcessedCount = currentState.allImages.size

                val imagesToDelete = currentState.otherImages + currentState.rejectedImages
                if (imagesToDelete.isNotEmpty()) {
                    val urisToDelete = imagesToDelete.map { Uri.parse(it.uri) }
                     _uiState.value = currentState.copy(pendingDeleteRequest = urisToDelete)
                } else {
                    completeReviewAndTryNext(currentState)
                }
            }
        }
    }

    fun onDeletionRequestHandled(successfullyDeleted: Boolean) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ReviewUiState.Ready) {
                _uiState.value = currentState.copy(pendingDeleteRequest = null)

                withContext(NonCancellable) {
                    if (successfullyDeleted) {
                        val imageIdsToDelete = (currentState.otherImages + currentState.rejectedImages).map { it.id }
                        if (imageIdsToDelete.isNotEmpty()) {
                            val deletedCount = imageItemDao.updateImageStatusesByIds(imageIdsToDelete, "DELETED")
                            Timber.d("Updated $deletedCount images to DELETED")
                        }
                    }
                    completeReviewAndTryNext(currentState)
                }
            }
        }
    }
    
    private suspend fun completeReviewAndTryNext(state: ReviewUiState.Ready) {
        // Update Accumulated Count
        updateAccumulatedCount(currentClusterProcessedCount)
        
        // Reset manual selection for next cluster
        manualSelectionIds = null
        
        val keptImageIds = listOfNotNull(state.selectedBestImage, state.selectedSecondBestImage).map { it.id }
        if (keptImageIds.isNotEmpty()) {
            val updatedCount = imageItemDao.updateImageStatusesByIds(keptImageIds, "KEPT")
            Timber.d("Updated $updatedCount images to KEPT")
        }

        imageClusterDao.updateImageClusterReviewStatus(state.cluster.id, "REVIEW_COMPLETED")
        Timber.d("Updated cluster ${state.cluster.id} to REVIEW_COMPLETED")
    }
    
    private fun updateSessionStats(state: ReviewUiState.Ready) {
        sessionClusterCount++
        val keptCount = listOfNotNull(state.selectedBestImage, state.selectedSecondBestImage).size
        sessionSavedImageCount += keptCount
    }
    
    private fun updateAccumulatedCount(count: Int) {
        val prefs = context.getSharedPreferences("bes2_prefs", Context.MODE_PRIVATE)
        val current = prefs.getInt(PREF_KEY_REVIEW_COUNT, 0)
        prefs.edit().putInt(PREF_KEY_REVIEW_COUNT, current + count).apply()
    }
    
    private fun checkAdCondition(): Boolean {
        val prefs = context.getSharedPreferences("bes2_prefs", Context.MODE_PRIVATE)
        val current = prefs.getInt(PREF_KEY_REVIEW_COUNT, 0)
        
        return if (current >= AD_THRESHOLD) {
            prefs.edit().putInt(PREF_KEY_REVIEW_COUNT, current - AD_THRESHOLD).apply()
            true
        } else {
            false
        }
    }
}

sealed interface ReviewUiState {
    object Loading : ReviewUiState
    object NoClustersToReview : ReviewUiState
    data class Ready(
        val cluster: ImageClusterEntity,
        val allImages: List<ImageItemEntity>,
        val otherImages: List<ImageItemEntity>,
        val rejectedImages: List<ImageItemEntity>,
        val selectedBestImage: ImageItemEntity?,
        val selectedSecondBestImage: ImageItemEntity?,
        val pendingDeleteRequest: List<Uri>? = null
    ) : ReviewUiState
}

sealed interface NavigationEvent {
    data class NavigateToHome(val clusterCount: Int, val savedCount: Int, val showAd: Boolean) : NavigationEvent // Added showAd
    object NavigateToSettings : NavigationEvent
}
