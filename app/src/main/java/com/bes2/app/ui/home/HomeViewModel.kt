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
import com.bes2.background.worker.PhotoAnalysisWorker
import com.bes2.background.worker.PhotoDiscoveryWorker
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ReviewItemDao
import com.bes2.data.dao.TrashItemDao
import com.bes2.data.repository.DateGroup
import com.bes2.data.repository.GalleryRepository
import com.bes2.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
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
    val readyToCleanCount: Int = 0, 
    val memoryEvent: DateGroup? = null,
    val isMemoryPrepared: Boolean = false,
    val monthlyReport: ReportStats = ReportStats(),
    val yearlyReport: ReportStats = ReportStats(),
    val isDiscoveryInProgress: Boolean = true,
    val isAnalysisInProgress: Boolean = true,
    val analysisProgressCurrent: Int = 0,
    val analysisProgressTotal: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val reviewItemDao: ReviewItemDao,
    private val trashItemDao: TrashItemDao,
    private val imageClusterDao: ImageClusterDao,
    private val galleryRepository: GalleryRepository,
    private val settingsRepository: SettingsRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val TAG = "HomeViewModel"

    init {
        Timber.tag(TAG).d("init - START")
        
        loadScreenshotCount()
        checkPendingReviews()
        monitorDietCount()
        loadGalleryCounts()
        
        monitorAnalysisStatus() 
        monitorProgress()
        
        monitorStats() 
        monitorReports() 
        
        loadMemoryEvent()
        
        Timber.tag(TAG).d("init - END")
    }
    
    private fun monitorProgress() {
        viewModelScope.launch {
            settingsRepository.analysisProgress.collectLatest { (current, total) ->
                _uiState.update { 
                    it.copy(
                        analysisProgressCurrent = current,
                        analysisProgressTotal = total
                    ) 
                }
            }
        }
    }
    
    private fun monitorAnalysisStatus() {
        viewModelScope.launch {
            workManager.getWorkInfosForUniqueWorkFlow(PIPELINE_WORK_NAME)
                .onStart { emit(emptyList()) }
                .collectLatest { workInfos ->
                    val discoveryInfo = workInfos.find { it.tags.contains(PhotoDiscoveryWorker::class.java.name) }
                    val analysisInfo = workInfos.find { it.tags.contains(PhotoAnalysisWorker::class.java.name) }
                    val clusteringInfo = workInfos.find { it.tags.contains(ClusteringWorker::class.java.name) }
                    val memoryInfo = workInfos.find { it.tags.contains(MemoryEventWorker::class.java.name) }
                
                    val isDiscoveryRunning = isWorkRunning(listOfNotNull(discoveryInfo))
                    val isAnalysisRunning = isWorkRunning(listOfNotNull(analysisInfo))
                    val isClusteringRunning = isWorkRunning(listOfNotNull(clusteringInfo))
                    val isMemoryRunning = isWorkRunning(listOfNotNull(memoryInfo))
                    
                    val analysisInProgress = isDiscoveryRunning || isAnalysisRunning || isClusteringRunning || isMemoryRunning

                    if (memoryInfo?.state == WorkInfo.State.SUCCEEDED) {
                         if (_uiState.value.memoryEvent == null) {
                             loadMemoryEvent()
                         } else {
                             _uiState.update { it.copy(isMemoryPrepared = true) }
                         }
                    }

                    _uiState.update { 
                        it.copy(
                            isDiscoveryInProgress = isDiscoveryRunning,
                            isAnalysisInProgress = analysisInProgress
                        ) 
                    }
                    
                    if (!isDiscoveryRunning) {
                        loadScreenshotCount()
                    }
                    
                    if (!analysisInProgress) {
                        loadGalleryCounts()
                    }
                }
        }
    }
    
    private fun isWorkRunning(workInfoList: List<WorkInfo>): Boolean {
        return workInfoList.any { workInfo ->
            workInfo.state == WorkInfo.State.ENQUEUED ||
            workInfo.state == WorkInfo.State.RUNNING ||
            workInfo.state == WorkInfo.State.BLOCKED
        }
    }

    private fun monitorStats() {
        viewModelScope.launch {
            settingsRepository.dailyStats.collectLatest { stats ->
                _uiState.update { state ->
                    state.copy(
                        dailyKept = stats.keptCount,
                        dailyDeleted = stats.deletedCount,
                        dailyTotal = stats.keptCount + stats.deletedCount
                    )
                }
            }
        }
    }
    
    private fun monitorReports() {
        val now = LocalDate.now()
        val zoneId = ZoneId.systemDefault()
        
        val startOfMonth = now.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        
        viewModelScope.launch {
            combine(
                reviewItemDao.getKeptCountByDateRangeFlow(startOfMonth, endOfMonth),
                reviewItemDao.getDeletedCountByDateRangeFlow(startOfMonth, endOfMonth)
            ) { kept, deleted ->
                ReportStats(total = kept + deleted, kept = kept, deleted = deleted)
            }.collectLatest { stats ->
                _uiState.update { it.copy(monthlyReport = stats) }
            }
        }

        val startOfYear = now.withDayOfYear(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfYear = now.plusYears(1).withDayOfYear(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        
        viewModelScope.launch {
            combine(
                reviewItemDao.getKeptCountByDateRangeFlow(startOfYear, endOfYear),
                reviewItemDao.getDeletedCountByDateRangeFlow(startOfYear, endOfYear)
            ) { kept, deleted ->
                ReportStats(total = kept + deleted, kept = kept, deleted = deleted)
            }.collectLatest { stats ->
                _uiState.update { it.copy(yearlyReport = stats) }
            }
        }
    }

    private fun loadScreenshotCount() {
        viewModelScope.launch(Dispatchers.IO) {
            // [MODIFIED] Calculate total count by checking both tables
            val dbTrashItems = trashItemDao.getAllReadyTrashItems()
            val mediaStoreScreenshots = galleryRepository.getScreenshots()
            
            // Get all URIs that have been processed (KEPT, DELETED, etc) in Review table
            val processedUris = reviewItemDao.getAllProcessedUris().toSet()
            
            val allUris = mutableSetOf<String>()
            allUris.addAll(dbTrashItems.map { it.uri })
            
            // Filter MediaStore items: Exclude if already processed in Review table or exists in Trash DB
            for (item in mediaStoreScreenshots) {
                val uri = item.uri.toString()
                if (!processedUris.contains(uri) && !allUris.contains(uri)) {
                    // Check if deleted in TrashItemDao? TrashItemDao only keeps READY or KEPT(temp).
                    // If it was deleted via TrashItemDao, it shouldn't be in DB, but MediaStore file might still exist if delete failed?
                    // But we trust our DB state.
                    if (!trashItemDao.isUriProcessed(uri)) {
                         allUris.add(uri)
                    }
                }
            }
            
            val totalCount = allUris.size
            
            Timber.tag("COUNT_DEBUG").d(
                "DB(Trash): %d, MediaStore: %d, Processed(Review): %d, Final: %d", 
                dbTrashItems.size, mediaStoreScreenshots.size, processedUris.size, totalCount
            )
            
            _uiState.update { it.copy(screenshotCount = totalCount) }
        }
    }

    private fun monitorDietCount() {
        viewModelScope.launch {
            reviewItemDao.getClusteredDietCountFlow().collectLatest { count ->
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
                val count = reviewItemDao.getMemoryItemCount(bestEvent.date)
                val isProcessed = count > 0
                
                _uiState.update { it.copy(memoryEvent = bestEvent, isMemoryPrepared = isProcessed) }
            }
        }
    }

    fun triggerBackgroundScan() {
        val discoveryRequest = OneTimeWorkRequestBuilder<PhotoDiscoveryWorker>().build()
        val analysisRequest = OneTimeWorkRequestBuilder<PhotoAnalysisWorker>().build()
        val clusteringRequest = OneTimeWorkRequestBuilder<ClusteringWorker>().build()
        val memoryRequest = OneTimeWorkRequestBuilder<MemoryEventWorker>().build()

        workManager.beginUniqueWork(
            PIPELINE_WORK_NAME,
            ExistingWorkPolicy.KEEP, 
            discoveryRequest
        )
        .then(analysisRequest)
        .then(clusteringRequest)
        .then(memoryRequest)
        .enqueue()
        
        // [ADDED] Also refresh UI state immediately when resuming
        loadMemoryEvent()
        loadScreenshotCount()
        loadGalleryCounts()
    }
    
    companion object {
        private const val PIPELINE_WORK_NAME = "FullAnalysisPipeline"
    }
}
