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

    init {
        Timber.d("ReviewViewModel init")
        // Observe both PENDING_REVIEW clusters and READY_TO_CLEAN images
        // But primarily we want to show clusters.
        // If a cluster is ready (from regular flow), show it.
        // If no clusters but READY_TO_CLEAN images exist (from background diet),
        // we need to "promote" them to PENDING_REVIEW status.
        
        viewModelScope.launch {
            imageClusterDao.getImageClustersByReviewStatus("PENDING_REVIEW")
                .flatMapLatest { clusters ->
                    Timber.d("PENDING_REVIEW clusters found: ${clusters.size}")
                    if (clusters.isEmpty()) {
                        // Check for background prepared clusters
                        val prepared = promoteBackgroundClusters()
                        if (prepared) {
                             // If promoted, the Flow will emit again with new PENDING_REVIEW clusters
                             flowOf(ReviewUiState.Loading)
                        } else {
                            // Really finished
                            Timber.w("No clusters and no ready-to-clean images. Finishing.")
                            schedulePostReviewSync()
                            
                            _navigationEvent.emit(NavigationEvent.NavigateToHome(sessionClusterCount, sessionSavedImageCount))
                            
                            flowOf(ReviewUiState.NoClustersToReview)
                        }
                    } else {
                        val currentCluster = clusters.first()
                        Timber.d("Processing cluster ID: ${currentCluster.id}")
                        // We need to find images for this cluster.
                        // Note: Background Diet items are READY_TO_CLEAN, regular items are ANALYZED.
                        // We should support both.
                        imageItemDao.getImageItemsByClusterId(currentCluster.id)
                            .filter { it.isNotEmpty() }
                            .map { images ->
                                // Filter for valid status (ANALYZED or READY_TO_CLEAN)
                                val validStatus = setOf("ANALYZED", "READY_TO_CLEAN")
                                val candidates = images.filter { it.status in validStatus }
                                val sortedCandidates = candidates.sortedByDescending { calculateFinalScore(it) }

                                val initialBest = sortedCandidates.getOrNull(0)
                                val initialSecond = sortedCandidates.getOrNull(1)

                                calculateReadyState(currentCluster, images, initialBest, initialSecond)
                            }
                    }
                }
                .collect { newState -> _uiState.value = newState }
        }
    }

    private suspend fun promoteBackgroundClusters(): Boolean {
        // Find items that are READY_TO_CLEAN but their cluster is not PENDING_REVIEW
        // Actually, we just need to find ANY cluster that contains READY_TO_CLEAN items 
        // and set its status to PENDING_REVIEW.
        
        // 1. Get a list of cluster IDs from READY_TO_CLEAN items
        val readyItems = imageItemDao.getImageItemsListByStatus("READY_TO_CLEAN")
        if (readyItems.isEmpty()) return false
        
        // Group by cluster ID
        val clusterIds = readyItems.mapNotNull { it.clusterId }.distinct()
        
        if (clusterIds.isNotEmpty()) {
            // Take the first one (or all?) -> Let's take one to start the chain
            val targetClusterId = clusterIds.first()
            
            // Verify if this cluster exists in DB
            val clusterFlow = imageClusterDao.getImageClusterById(targetClusterId)
            val cluster = clusterFlow.first()
            
            if (cluster != null) {
                // Update cluster status to PENDING_REVIEW
                // This will trigger the main flow
                imageClusterDao.updateImageClusterReviewStatus(targetClusterId, "PENDING_REVIEW")
                Timber.d("Promoted cluster $targetClusterId to PENDING_REVIEW")
                return true
            } else {
                // Orphaned items? Should not happen with ClusteringWorker logic
                Timber.w("Found READY_TO_CLEAN items but cluster $targetClusterId not found in DB.")
                return false
            }
        }
        return false
    }

    // Removed createDietSessionIfNeeded() as it was breaking clustering logic

    fun selectImage(imageToSelect: ImageItemEntity) {
        val currentState = _uiState.value
        if (currentState is ReviewUiState.Ready) {
            if (imageToSelect.status != "ANALYZED" && imageToSelect.status != "READY_TO_CLEAN") return

            val allSelected = listOfNotNull(currentState.selectedBestImage, currentState.selectedSecondBestImage)
            val alreadySelected = allSelected.any { it.id == imageToSelect.id }

            val newSelection = if (alreadySelected) {
                allSelected.filterNot { it.id == imageToSelect.id }
            } else {
                if (allSelected.size < 2) {
                    allSelected + imageToSelect
                } else {
                    val sortedSelection = allSelected.sortedBy { calculateFinalScore(it) }
                    listOf(sortedSelection.last(), imageToSelect)
                }
            }

            val selectionSortedByScore = newSelection.sortedByDescending { calculateFinalScore(it) }
            val newBest = selectionSortedByScore.getOrNull(0)
            val newSecond = selectionSortedByScore.getOrNull(1)

            _uiState.value = calculateReadyState(currentState.cluster, currentState.allImages, newBest, newSecond)
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

    private fun calculateReadyState(cluster: ImageClusterEntity, allImages: List<ImageItemEntity>, newFirst: ImageItemEntity?, newSecond: ImageItemEntity?): ReviewUiState.Ready {
        val validStatus = setOf("ANALYZED", "READY_TO_CLEAN")
        val (analyzedImages, rejectedImages) = allImages.partition { it.status in validStatus }

        val selection = listOfNotNull(newFirst, newSecond)
            .sortedByDescending { calculateFinalScore(it) }
      
        val finalBest = selection.getOrNull(0)
        val finalSecond = selection.getOrNull(1)
        
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
            selectedSecondBestImage = finalSecond
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
        val keptImageIds = listOfNotNull(state.selectedBestImage, state.selectedSecondBestImage).map { it.id }
        if (keptImageIds.isNotEmpty()) {
            val updatedCount = imageItemDao.updateImageStatusesByIds(keptImageIds, "KEPT")
            Timber.d("Updated $updatedCount images to KEPT")
        }

        imageClusterDao.updateImageClusterReviewStatus(state.cluster.id, "REVIEW_COMPLETED")
        Timber.d("Updated cluster ${state.cluster.id} to REVIEW_COMPLETED")
        
        // The loop continues automatically because flatMapLatest will re-run 
        // when the list of PENDING_REVIEW clusters changes (becomes empty).
        // promoteBackgroundClusters() will be called again.
    }
    
    private fun updateSessionStats(state: ReviewUiState.Ready) {
        sessionClusterCount++
        val keptCount = listOfNotNull(state.selectedBestImage, state.selectedSecondBestImage).size
        sessionSavedImageCount += keptCount
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
    data class NavigateToHome(val clusterCount: Int, val savedCount: Int) : NavigationEvent
    object NavigateToSettings : NavigationEvent
}
