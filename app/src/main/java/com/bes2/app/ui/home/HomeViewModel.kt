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
import com.bes2.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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
    val yearlyReport: ReportStats = ReportStats()
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
        loadMemoryEvent()
        startBackgroundAnalysis()
        monitorStats() 
        monitorReports() 
        
        Timber.tag(TAG).d("init - END")
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
            val count = galleryRepository.getScreenshotCount()
            _uiState.update { it.copy(screenshotCount = count) }
        }
    }

    private fun monitorDietCount() {
        viewModelScope.launch {
            reviewItemDao.getClusteredDietCountFlow().collectLatest { count ->
                _uiState.update { it.copy(readyToCleanCount = count) }
            }
        }
        
        viewModelScope.launch {
            reviewItemDao.getActiveDietCountFlow().collectLatest { count ->
                if (count < 5) {
                    Timber.tag(TAG).d("Total Diet count low ($count). Triggering refill.")
                    startBackgroundAnalysis()
                }
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
        workManager.enqueueUniqueWork(
            PhotoDiscoveryWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP, // [FIX] Changed from REPLACE to KEEP to avoid thrashing
            discoveryRequest
        )
    }
    
    fun refreshGalleryCount() {
        startBackgroundAnalysis()
        loadScreenshotCount()
        loadGalleryCounts()
    }
}
