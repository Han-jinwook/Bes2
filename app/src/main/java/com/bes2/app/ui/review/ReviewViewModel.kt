package com.bes2.app.ui.review

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
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
import com.bes2.ml.ImagePhashGenerator
import com.bes2.ml.ImageRestorationProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val imageRestorationProcessor: ImageRestorationProcessor
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
    
    // Mode flag: True if we are in Memory Event mode (single date review)
    private var isMemoryEventMode = false
    
    // Memory Event Queue
    private var memoryEventClusters: List<List<ImageItemEntity>> = emptyList()
    private var memoryEventIndex = 0
    private var memoryEventDateString: String = ""

    init {
        Timber.d("ReviewViewModel init")
        
        val dateArg = savedStateHandle.get<String>("date")
        if (dateArg != null) {
            Timber.d("Starting in Memory Event Mode for date: $dateArg")
            isMemoryEventMode = true
            memoryEventDateString = dateArg
            loadMemoryEvent(dateArg)
        } else {
            Timber.d("Starting in Normal Review Mode")
            loadPendingClusters()
        }
    }
    
    private fun loadMemoryEvent(dateString: String) {
        viewModelScope.launch(Dispatchers.IO) { // Run clustering on IO
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val date = LocalDate.parse(dateString, formatter)
                val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
                
                // Fetch images (Documents excluded by DAO)
                val images = imageItemDao.getImagesByDateRange(startOfDay, endOfDay)
                
                if (images.isEmpty()) {
                    Timber.w("No images found for date $dateString")
                    _navigationEvent.emit(NavigationEvent.NavigateToHome(0, 0, false))
                    _uiState.value = ReviewUiState.NoClustersToReview
                    return@launch
                }
                
                val validImages = images.filter { 
                    it.status != "DELETED" 
                }
                
                if (validImages.isEmpty()) {
                     _uiState.value = ReviewUiState.NoClustersToReview
                     return@launch
                }

                // Perform on-the-fly clustering
                Timber.d("Clustering ${validImages.size} images for Memory Event...")
                val clusters = clusterImages(validImages)
                Timber.d("Created ${clusters.size} clusters.")
                
                memoryEventClusters = clusters.map { it.images }
                memoryEventIndex = 0
                
                loadNextMemoryCluster()
                
            } catch (e: Exception) {
                Timber.e(e, "Error loading memory event")
                _uiState.value = ReviewUiState.NoClustersToReview
            }
        }
    }
    
    private fun loadNextMemoryCluster() {
        if (memoryEventIndex < memoryEventClusters.size) {
            val currentImages = memoryEventClusters[memoryEventIndex]
            val dummyCluster = ImageClusterEntity(
                id = "MEMORY_EVENT_${memoryEventDateString}_$memoryEventIndex",
                creationTime = System.currentTimeMillis(),
                reviewStatus = "MEMORY_EVENT"
            )
            
            // Update UI State
            val remaining = memoryEventClusters.size - memoryEventIndex
            // But total count should be fixed.
            // currentClusterIndex = memoryEventIndex + 1
            
            _uiState.value = calculateReadyState(dummyCluster, currentImages, 0) // remainingCount passed differently here?
            // Actually, calculateReadyState uses logic: 
            // totalCount = if (isMemoryEventMode) 1 else ...
            // We should update that logic.
        } else {
            // Finished
            viewModelScope.launch {
                val showAd = checkAdCondition()
                _navigationEvent.emit(NavigationEvent.NavigateToHome(memoryEventClusters.size, sessionSavedImageCount, showAd))
            }
        }
    }

    // Copied from ClusteringWorker and adapted
    private data class Cluster(val images: MutableList<ImageItemEntity>)
    
    private fun clusterImages(images: List<ImageItemEntity>): List<Cluster> {
        val clusters = mutableListOf<Cluster>()
        val unclusteredImages = images.toMutableList()
        val HAMMING_DISTANCE_THRESHOLD = 15

        while (unclusteredImages.isNotEmpty()) {
            val currentImage = unclusteredImages.removeAt(0)
            val newCluster = Cluster(mutableListOf(currentImage))
            val iterator = unclusteredImages.iterator()

            while (iterator.hasNext()) {
                val otherImage = iterator.next()
                if (areSimilar(currentImage, otherImage, HAMMING_DISTANCE_THRESHOLD)) {
                    newCluster.images.add(otherImage)
                    iterator.remove()
                }
            }
            clusters.add(newCluster)
        }
        return clusters
    }

    private fun areSimilar(image1: ImageItemEntity, image2: ImageItemEntity, threshold: Int): Boolean {
        val pHash1 = image1.pHash
        val pHash2 = image2.pHash
        if (pHash1 == null || pHash2 == null || pHash1.length != pHash2.length) {
            return false
        }
        val distance = ImagePhashGenerator.calculateHammingDistance(pHash1, pHash2)
        return distance <= threshold
    }

    private fun loadPendingClusters() {
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
                        val remainingCount = clusters.size
                        Timber.d("Processing cluster ID: ${currentCluster.id}")
                        
                        imageItemDao.getImageItemsByClusterId(currentCluster.id)
                            .filter { it.isNotEmpty() }
                            .map { images ->
                                val validStatus = setOf("ANALYZED", "READY_TO_CLEAN", "STATUS_REJECTED")
                                // Filter out items that are classified as DOCUMENT
                                val candidates = images.filter { 
                                    it.status in validStatus && it.category != "DOCUMENT"
                                }
                                calculateReadyState(currentCluster, candidates, remainingCount)
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
            
            // Allow KEPT items selection in Memory Event mode
            if (imageToSelect.status != "ANALYZED" && imageToSelect.status != "READY_TO_CLEAN" && imageToSelect.status != "KEPT") return

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
            // Recalculate with existing counts
            // FIX: Use correct counts for Memory Mode
            val totalCount = if (isMemoryEventMode) memoryEventClusters.size else (sessionClusterCount + currentState.totalClusterCount - currentState.currentClusterIndex) // Wait, logic below handles it better
            // Just call calculateReadyState again, it handles indices.
            val remaining = if (isMemoryEventMode) 0 else 0 // calculateReadyState uses this differently
            _uiState.value = calculateReadyState(currentState.cluster, currentState.allImages, remaining)
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

    private fun calculateReadyState(cluster: ImageClusterEntity, allImages: List<ImageItemEntity>, remainingCount: Int): ReviewUiState.Ready {
        // In Memory Event mode, we might have KEPT images too.
        val (analyzedImages, rejectedImages) = allImages.partition { 
            it.status == "ANALYZED" || it.status == "READY_TO_CLEAN" || it.status == "KEPT" || it.status == "NEW" || it.status == "PENDING_ANALYSIS"
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

        // Logic for minimap counts
        // Normal Mode: sessionClusterCount (completed) + remainingCount (future including current)
        // Memory Mode: memoryEventClusters.size (total), memoryEventIndex + 1 (current)
        
        val totalCount = if (isMemoryEventMode) memoryEventClusters.size else (sessionClusterCount + remainingCount)
        val currentIndex = if (isMemoryEventMode) (memoryEventIndex + 1) else (sessionClusterCount + 1)

        return ReviewUiState.Ready(
            cluster = cluster,
            allImages = allImages,
            otherImages = otherImages,
            rejectedImages = rejectedImages,
            selectedBestImage = finalBest,
            selectedSecondBestImage = finalSecond,
            pendingDeleteRequest = null,
            totalClusterCount = totalCount,
            currentClusterIndex = currentIndex
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

        if (isMemoryEventMode) {
            // Load Next Memory Cluster
            memoryEventIndex++
            loadNextMemoryCluster()
        } else {
            imageClusterDao.updateImageClusterReviewStatus(state.cluster.id, "REVIEW_COMPLETED")
            Timber.d("Updated cluster ${state.cluster.id} to REVIEW_COMPLETED")
            // Flow in init block will pick up next cluster automatically
        }
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

    // --- Restoration Logic ---
    
    fun restoreImage(image: ImageItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Load Bitmap
                val originalBitmap = loadBitmap(image.uri) ?: return@launch
                
                // 2. Run Restoration
                val restoredBitmap = imageRestorationProcessor.restore(originalBitmap)
                
                // 3. Save Restored Bitmap (Overwrite original)
                val uri = Uri.parse(image.uri)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    restoredBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, out)
                }
                
                // 4. Update DB (Promote to ANALYZED and reset bad scores)
                val updatedImage = image.copy(
                    status = "ANALYZED", 
                    blurScore = 100.0f, // Fake score to indicate it's sharp now
                    exposureScore = 0.0f // Reset exposure score if it was backlit
                )
                imageItemDao.updateImageItem(updatedImage)
                
                // 5. Refresh UI
                val currentState = _uiState.value
                if (currentState is ReviewUiState.Ready) {
                    val newRejected = currentState.rejectedImages.filterNot { it.id == image.id }
                    val newOther = currentState.otherImages + updatedImage
                    
                    // Sort Other Images again? Not strictly necessary for UX, just append.
                    
                    _uiState.value = currentState.copy(
                        rejectedImages = newRejected,
                        otherImages = newOther
                    )
                }
                Timber.d("Image restored and promoted: ${image.id}")
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to restore image")
            }
        }
    }

    private fun loadBitmap(uriString: String): android.graphics.Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { 
                android.graphics.BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
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
        val pendingDeleteRequest: List<Uri>? = null,
        val totalClusterCount: Int = 0, // Added
        val currentClusterIndex: Int = 0 // Added
    ) : ReviewUiState
}

sealed interface NavigationEvent {
    data class NavigateToHome(val clusterCount: Int, val savedCount: Int, val showAd: Boolean) : NavigationEvent
    object NavigateToSettings : NavigationEvent
}
