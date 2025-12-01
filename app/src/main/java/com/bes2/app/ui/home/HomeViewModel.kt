package com.bes2.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bes2.background.worker.ClusteringWorker
import com.bes2.background.worker.MemoryEventWorker
import com.bes2.background.worker.PhotoDiscoveryWorker
import com.bes2.background.worker.PhotoAnalysisWorker
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ReviewItemDao
import com.bes2.data.dao.TrashItemDao
import com.bes2.data.repository.DateGroup
import com.bes2.data.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ReportStats(
    val total: Int = 0,
    val kept: Int = 0,
    val deleted: Int = 0
) {
    val efficiency: Int
        get() = if (kept > 0) ((total - kept).toFloat() / kept * 100).toInt() else 0
}

data class HomeUiState(
    val dailyTotal: Int = 0,
    val dailyKept: Int = 0,
    val dailyDeleted: Int = 0,
    val galleryTotalCount: Int = 0,
    val screenshotCount: Int = 0,
    val hasPendingReview: Boolean = false,
    val readyToCleanCount: Int = 0, // This is Diet items count
    val memoryEvent: DateGroup? = null,
    val isMemoryPrepared: Boolean = false,
    val monthlyReport: ReportStats = ReportStats(),
    val yearlyReport: ReportStats = ReportStats()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val reviewItemDao: ReviewItemDao,
    private val trashItemDao: TrashItemDao,
    private val imageClusterDao: ImageClusterDao,
    private val galleryRepository: GalleryRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val TAG = "HomeViewModel"

    init {
        Timber.tag(TAG).d("init - START")
        
        loadScreenshotCount()
        // monitorTrashCount() // [MODIFIED] Disabled to prefer immediate MediaStore count
        Timber.tag(TAG).d("init - monitorTrashCount DISABLED")
        
        checkPendingReviews()
        Timber.tag(TAG).d("init - checkPendingReviews OK")

        monitorDietCount()
        Timber.tag(TAG).d("init - monitorDietCount OK")
        
        loadGalleryCounts()
        Timber.tag(TAG).d("init - loadGalleryCounts OK")
        
        loadMemoryEvent()
        Timber.tag(TAG).d("init - loadMemoryEvent OK")
        
        startBackgroundAnalysis()
        Timber.tag(TAG).d("init - startBackgroundAnalysis OK")
        
        Timber.tag(TAG).d("init - END")
    }

    private fun loadScreenshotCount() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = galleryRepository.getScreenshotCount()
            _uiState.update { it.copy(screenshotCount = count) }
        }
    }

    private fun monitorTrashCount() {
        viewModelScope.launch {
            trashItemDao.getReadyTrashCountFlow().collectLatest { count ->
                _uiState.update { it.copy(screenshotCount = count) }
            }
        }
    }
    
    private fun monitorDietCount() {
        viewModelScope.launch {
            reviewItemDao.getActiveDietCountFlow().collectLatest { count ->
                _uiState.update { it.copy(readyToCleanCount = count) }
            }
        }
    }

    private fun checkPendingReviews() {
        viewModelScope.launch {
            imageClusterDao.getImageClustersByReviewStatus("PENDING_REVIEW")
                .collectLatest { _ ->
                    val instantItems = reviewItemDao.getItemsBySourceAndStatus("INSTANT", "CLUSTERED")
                    val hasInstantPending = instantItems.isNotEmpty()
                    
                    _uiState.update { 
                        it.copy(
                            hasPendingReview = hasInstantPending
                        ) 
                    }
                }
        }
    }

    private fun loadGalleryCounts() {
        viewModelScope.launch(Dispatchers.IO) {
            val totalCount = galleryRepository.getTotalImageCount()
            _uiState.update { it.copy(galleryTotalCount = totalCount) }
        }
    }

    private fun loadMemoryEvent() {
        viewModelScope.launch(Dispatchers.IO) {
            val events = galleryRepository.findLargePhotoGroups(20)
            if (events.isNotEmpty()) {
                val bestEvent = events.first()
                _uiState.update { it.copy(memoryEvent = bestEvent, isMemoryPrepared = false) }
                startMemoryAnalysis(bestEvent.date)
            }
        }
    }
    
    private fun startMemoryAnalysis(date: String) {
        val inputData = Data.Builder().putString(MemoryEventWorker.KEY_TARGET_DATE, date).build()
        val workRequest = OneTimeWorkRequestBuilder<MemoryEventWorker>().setInputData(inputData).build()
        workManager.enqueueUniqueWork(MemoryEventWorker.WORK_NAME + "_$date", ExistingWorkPolicy.KEEP, workRequest)
        
        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(workRequest.id).collect { workInfo ->
                if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                    _uiState.update { it.copy(isMemoryPrepared = true) }
                }
            }
        }
    }

    private fun startBackgroundAnalysis() {
        val discoveryRequest = OneTimeWorkRequestBuilder<PhotoDiscoveryWorker>().build()
        
        // [FIX] Force restart the worker to ensure it runs immediately
        workManager.enqueueUniqueWork(
            PhotoDiscoveryWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE, // Changed from KEEP to REPLACE
            discoveryRequest
        )
        
        val analysisRequest = OneTimeWorkRequestBuilder<PhotoAnalysisWorker>()
            .setInputData(Data.Builder().putBoolean(PhotoAnalysisWorker.KEY_IS_BACKGROUND_DIET, true).build())
            .build()
        workManager.enqueueUniqueWork(
            PhotoAnalysisWorker.WORK_NAME, 
            ExistingWorkPolicy.REPLACE, 
            analysisRequest
        )
        
        val clusteringRequest = OneTimeWorkRequestBuilder<ClusteringWorker>().build()
        workManager.enqueueUniqueWork(
            ClusteringWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            clusteringRequest
        )
    }
    
    fun refreshGalleryCount() {
        startBackgroundAnalysis()
    }
    
    private fun loadDailyStats() { }
    private fun loadReportData() { }
}
