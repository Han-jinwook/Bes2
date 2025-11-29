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
import com.bes2.background.util.ImageClusteringHelper
import com.bes2.background.worker.DailyCloudSyncWorker
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.ImageClusterEntity
import com.bes2.data.model.ImageItemEntity
import com.bes2.data.repository.GalleryRepository
import com.bes2.data.repository.SettingsRepository
import com.bes2.ml.ImagePhashGenerator
import com.bes2.ml.ImageRestorationProcessor
import com.bes2.ml.NimaQualityAnalyzer
import com.bes2.ml.SmileDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModel @Inject constructor(
    private val imageClusterDao: ImageClusterDao,
    private val imageItemDao: ImageItemDao,
    private val galleryRepository: GalleryRepository,
    private val settingsRepository: SettingsRepository,
    private val workManager: WorkManager,
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val imageRestorationProcessor: ImageRestorationProcessor,
    private val nimaAnalyzer: NimaQualityAnalyzer,
    private val smileDetector: SmileDetector,
    private val clusteringHelper: ImageClusteringHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReviewUiState>(ReviewUiState.Loading)
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent

    private var allClusterIds: List<String> = emptyList()
    private var allMemoryClusters: List<List<ImageItemEntity>> = emptyList()
    private var currentIndex = 0
    
    private var isMemoryEventMode = false
    private var memoryEventDateString: String = ""
    
    // [FIX] Restore session state variables
    private var sessionClusterCount = 0
    private var sessionSavedImageCount = 0
    private var manualSelectionIds: List<Long>? = null

    private val PREF_KEY_REVIEW_COUNT = "pref_review_accumulated_count"
    private val AD_THRESHOLD = 20 

    init {
        Timber.d("ReviewViewModel init")
        
        val dateArg = savedStateHandle.get<String>("date")
        if (dateArg != null) {
            isMemoryEventMode = true
            memoryEventDateString = dateArg
            loadMemoryEvent(dateArg)
        } else {
            loadPendingClusters()
        }
    }
    
    fun nextCluster() {
        val total = if (isMemoryEventMode) allMemoryClusters.size else allClusterIds.size
        if (currentIndex < total - 1) {
            currentIndex++
            manualSelectionIds = null
            loadCurrentCluster()
        }
    }
    
    fun prevCluster() {
        if (currentIndex > 0) {
            currentIndex--
            manualSelectionIds = null
            loadCurrentCluster()
        }
    }
    
    private fun loadCurrentCluster() {
        _uiState.value = ReviewUiState.Loading
        if (isMemoryEventMode) {
            loadMemoryClusterAtIndex(currentIndex)
        } else {
            loadNormalClusterAtIndex(currentIndex)
        }
    }

    private fun loadPendingClusters() {
        viewModelScope.launch {
            val clusters = imageClusterDao.getImageClustersByReviewStatus("PENDING_REVIEW").first()
            
            if (clusters.isEmpty()) {
                val prepared = promoteBackgroundClusters()
                if (prepared) {
                    loadPendingClusters()
                } else {
                    finishReview()
                }
            } else {
                allClusterIds = clusters.map { it.id }
                currentIndex = 0
                loadNormalClusterAtIndex(0)
            }
        }
    }
    
    private fun loadNormalClusterAtIndex(index: Int) {
        if (index !in allClusterIds.indices) return
        
        val clusterId = allClusterIds[index]
        viewModelScope.launch {
            val cluster = imageClusterDao.getImageClusterById(clusterId).first() ?: return@launch
            val images = imageItemDao.getImageItemsByClusterId(clusterId).first()
            val validStatus = setOf("ANALYZED", "READY_TO_CLEAN", "STATUS_REJECTED", "KEPT")
            val candidates = images.filter { it.status in validStatus }
            _uiState.value = calculateReadyState(cluster, candidates, allClusterIds.size, index)
        }
    }

    private fun loadMemoryEvent(dateString: String) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val date = LocalDate.parse(dateString, formatter)
                val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
                
                val dbImages = imageItemDao.getImagesByDateRange(startOfDay, endOfDay)
                val validDbImages = dbImages.filter { it.status == "ANALYZED" || it.status == "KEPT" }
                
                val imagesToCluster = if (validDbImages.size > 5) {
                    validDbImages
                } else {
                    val mediaImages = galleryRepository.getImagesForDateString(dateString)
                    if (mediaImages.isEmpty()) {
                        finishReview()
                        return@launch
                    }
                    mediaImages.mapNotNull { mediaImage ->
                        try {
                            val bitmap = loadBitmap(mediaImage.uri) ?: return@mapNotNull null
                            val pHash = ImagePhashGenerator.generatePhash(bitmap)
                            val nimaScores = nimaAnalyzer.analyze(bitmap)
                            val nimaScore = nimaScores?.mapIndexed { i, s -> (i + 1) * s }?.sum()
                            bitmap.recycle()
                            
                            ImageItemEntity(
                                id = mediaImage.id, uri = mediaImage.uri, filePath = mediaImage.filePath, timestamp = mediaImage.timestamp,
                                status = "ANALYZED", pHash = pHash, nimaScore = nimaScore, blurScore = 100f, exposureScore = 0f,
                                areEyesClosed = false, smilingProbability = null, clusterId = null, category = "MEMORY"
                            )
                        } catch (e: Exception) { null }
                    }
                }
                
                if (imagesToCluster.isEmpty()) {
                    finishReview()
                    return@launch
                }

                val clusters = clusteringHelper.clusterImages(imagesToCluster)
                allMemoryClusters = clusters.map { it.images }
                currentIndex = 0
                loadMemoryClusterAtIndex(0)
                
            } catch (e: Exception) {
                Timber.e(e, "Error loading memory event")
                _uiState.value = ReviewUiState.NoClustersToReview
            }
        }
    }
    
    private fun loadMemoryClusterAtIndex(index: Int) {
        if (index !in allMemoryClusters.indices) return
        
        val currentImages = allMemoryClusters[index]
        val dummyCluster = ImageClusterEntity(
            id = "MEMORY_EVENT_${memoryEventDateString}_$index",
            creationTime = System.currentTimeMillis(),
            reviewStatus = "MEMORY_EVENT"
        )
        
        _uiState.value = calculateReadyState(dummyCluster, currentImages, allMemoryClusters.size, index)
    }

    fun selectImage(imageToSelect: ImageItemEntity) {
        val currentState = _uiState.value
        if (currentState is ReviewUiState.Ready) {
            if (imageToSelect.status == "STATUS_REJECTED") {
                restoreRejectedImage(imageToSelect)
                return
            }
            if (imageToSelect.status != "ANALYZED" && imageToSelect.status != "READY_TO_CLEAN" && imageToSelect.status != "KEPT" && imageToSelect.status != "NEW") return

            val currentSelection = if (manualSelectionIds == null) {
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

            manualSelectionIds = newSelection.map { it.id }
            
            _uiState.value = calculateReadyState(currentState.cluster, currentState.allImages, if (isMemoryEventMode) allMemoryClusters.size else allClusterIds.size, currentIndex)
        }
    }
    
    private fun restoreRejectedImage(image: ImageItemEntity) {
        viewModelScope.launch {
            val restoredImage = image.copy(status = "ANALYZED")
            imageItemDao.updateImageItem(restoredImage)
            loadCurrentCluster() 
        }
    }
    
    private fun calculateFinalScore(image: ImageItemEntity): Float {
        val nimaScore = (image.nimaScore ?: 5f) * 10 
        val musiqRaw = image.musiqScore ?: (nimaScore / 10f)
        val musiqScore = musiqRaw * 10
        val smileProb = image.smilingProbability ?: 0f
        val smileBonus = if (smileProb < 0.1f) -10f else smileProb * 30f
        return (nimaScore * 0.3f) + (musiqScore * 0.5f) + smileBonus
    }

    private fun calculateReadyState(cluster: ImageClusterEntity, allImages: List<ImageItemEntity>, totalCount: Int, index: Int): ReviewUiState.Ready {
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

        return ReviewUiState.Ready(
            cluster = cluster,
            allImages = allImages,
            otherImages = otherImages,
            rejectedImages = rejectedImages,
            selectedBestImage = finalBest,
            selectedSecondBestImage = finalSecond,
            pendingDeleteRequest = null,
            totalClusterCount = totalCount,
            currentClusterIndex = index + 1
        )
    }

    private suspend fun promoteBackgroundClusters(): Boolean {
        val readyItems = imageItemDao.getImageItemsListByStatus("READY_TO_CLEAN")
        if (readyItems.isEmpty()) return false
        val clusterIds = readyItems.mapNotNull { it.clusterId }.distinct()
        if (clusterIds.isNotEmpty()) {
            val targetClusterId = clusterIds.first()
            val cluster = imageClusterDao.getImageClusterById(targetClusterId).first()
            if (cluster != null) {
                imageClusterDao.updateImageClusterReviewStatus(targetClusterId, "PENDING_REVIEW")
                return true
            }
        }
        return false
    }
    
    private suspend fun schedulePostReviewSync() {
        val settings = settingsRepository.storedSettings.first()
        if (settings.syncOption == "NONE" || settings.syncOption == "DAILY") return
        val constraints = if (settings.uploadOnWifiOnly) Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build() else Constraints.NONE
        val inputData = Data.Builder().putBoolean(DailyCloudSyncWorker.KEY_IS_ONE_TIME_SYNC, true).build()
        val syncWorkRequest = OneTimeWorkRequestBuilder<DailyCloudSyncWorker>().setConstraints(constraints).setInputData(inputData).build()
        workManager.enqueue(syncWorkRequest)
    }

    fun deleteOtherImages() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ReviewUiState.Ready) {
                sessionClusterCount++
                val keptCount = listOfNotNull(currentState.selectedBestImage, currentState.selectedSecondBestImage).size
                sessionSavedImageCount += keptCount
                updateAccumulatedCount(currentState.allImages.size)

                val imagesToDelete = currentState.otherImages + currentState.rejectedImages
                if (imagesToDelete.isNotEmpty()) {
                    val urisToDelete = imagesToDelete.map { Uri.parse(it.uri) }
                     _uiState.value = currentState.copy(pendingDeleteRequest = urisToDelete)
                } else {
                    markClusterCompleted(currentState)
                    nextCluster() 
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
                            imageItemDao.updateImageStatusesByIds(imageIdsToDelete, "DELETED")
                        }
                    }
                    markClusterCompleted(currentState)
                    nextCluster()
                }
            }
        }
    }
    
    private suspend fun markClusterCompleted(state: ReviewUiState.Ready) {
        val keptImageIds = listOfNotNull(state.selectedBestImage, state.selectedSecondBestImage).map { it.id }
        if (keptImageIds.isNotEmpty()) {
            imageItemDao.updateImageStatusesByIds(keptImageIds, "KEPT")
        }
        
        if (!isMemoryEventMode) {
            imageClusterDao.updateImageClusterReviewStatus(state.cluster.id, "REVIEW_COMPLETED")
        }
    }
    
    private suspend fun finishReview() {
        schedulePostReviewSync()
        val showAd = checkAdCondition()
        _navigationEvent.emit(NavigationEvent.NavigateToHome(sessionClusterCount, sessionSavedImageCount, showAd))
        _uiState.value = ReviewUiState.NoClustersToReview
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

    fun restoreImage(image: ImageItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val originalBitmap = loadBitmap(image.uri) ?: return@launch
                val restoredBitmap = imageRestorationProcessor.restore(originalBitmap)
                val uri = Uri.parse(image.uri)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    restoredBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, out)
                }
                val updatedImage = image.copy(status = "ANALYZED", blurScore = 100.0f, exposureScore = 0.0f)
                imageItemDao.updateImageItem(updatedImage)
                loadCurrentCluster()
            } catch (e: Exception) { Timber.e(e) }
        }
    }

    private fun loadBitmap(uriString: String): android.graphics.Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { 
                android.graphics.BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) { null }
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
        val totalClusterCount: Int = 0,
        val currentClusterIndex: Int = 0
    ) : ReviewUiState
}

sealed interface NavigationEvent {
    data class NavigateToHome(val clusterCount: Int, val savedCount: Int, val showAd: Boolean) : NavigationEvent
    object NavigateToSettings : NavigationEvent
}
