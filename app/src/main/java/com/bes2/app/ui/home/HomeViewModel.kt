package com.bes2.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bes2.background.worker.PastPhotoAnalysisWorker
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.repository.DateGroup
import com.bes2.data.repository.GalleryRepository
import com.bes2.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
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
    // 100장 중 20장 건짐 -> 80장 정리 -> 400% 효율 (사용자 정의)
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
    val monthlyReport: ReportStats = ReportStats(),
    val yearlyReport: ReportStats = ReportStats()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val imageItemDao: ImageItemDao,
    private val imageClusterDao: ImageClusterDao,
    private val galleryRepository: GalleryRepository,
    private val homeRepository: HomeRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDailyStats()
        loadGalleryCounts()
        loadMemoryEvent()
        loadReportData()
        checkPendingReviews()
        monitorReadyToClean()
        startBackgroundAnalysis()
    }

    private fun loadMemoryEvent() {
        viewModelScope.launch(Dispatchers.IO) {
            // Find events with at least 20 photos
            val events = galleryRepository.findLargePhotoGroups(20)
            if (events.isNotEmpty()) {
                // Pick the most recent one (or randomize)
                // For now, let's pick the first one (most recent)
                _uiState.update { it.copy(memoryEvent = events.first()) }
            }
        }
    }

    private fun startBackgroundAnalysis() {
        Timber.d("Triggering PastPhotoAnalysisWorker from HomeViewModel")
        val workRequest = OneTimeWorkRequestBuilder<PastPhotoAnalysisWorker>().build()
        workManager.enqueueUniqueWork(
            PastPhotoAnalysisWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun checkPendingReviews() {
        viewModelScope.launch {
            imageClusterDao.getImageClustersByReviewStatus("PENDING_REVIEW")
                .collectLatest { clusters ->
                    _uiState.update { it.copy(hasPendingReview = clusters.isNotEmpty()) }
                }
        }
    }

    private fun monitorReadyToClean() {
        viewModelScope.launch {
            imageItemDao.getImageItemsByStatusFlow("READY_TO_CLEAN")
                .map { it.size }
                .collectLatest { count ->
                    _uiState.update { it.copy(readyToCleanCount = count) }
                }
        }
    }

    private fun loadGalleryCounts() {
        viewModelScope.launch(Dispatchers.IO) {
            val totalCount = galleryRepository.getTotalImageCount()
            
            val processedCount = imageItemDao.countImagesByStatus("KEPT") + 
                                 imageItemDao.countImagesByStatus("DELETED") +
                                 imageItemDao.countImagesByStatus("STATUS_REJECTED")
            
            val unprocessedTotal = (totalCount - processedCount).coerceAtLeast(0)

            val allScreenshots = galleryRepository.getScreenshots()
            
            var unprocessedScreenshots = 0
            for (item in allScreenshots) {
                val status = imageItemDao.getImageStatusByUri(item.uri.toString())
                if (status != "KEPT" && status != "DELETED" && status != "STATUS_REJECTED") {
                    unprocessedScreenshots++
                }
            }

            _uiState.update { it.copy(
                galleryTotalCount = unprocessedTotal,
                screenshotCount = unprocessedScreenshots
            ) }
        }
    }

    private fun loadDailyStats() {
        val startOfDay = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        viewModelScope.launch {
            imageItemDao.getDailyStatsFlow(startOfDay).collectLatest { statsList ->
                var total = 0
                var kept = 0
                var deleted = 0

                val processedStatuses = setOf("ANALYZED", "KEPT", "DELETED", "STATUS_REJECTED", "READY_TO_CLEAN")

                statsList.forEach { statusCount ->
                    if (statusCount.status in processedStatuses) {
                        total += statusCount.count
                    }
                    
                    if (statusCount.status == "KEPT") {
                        kept = statusCount.count
                    } else if (statusCount.status == "DELETED") {
                        deleted = statusCount.count
                    }
                }
                
                _uiState.update { it.copy(
                    dailyTotal = total,
                    dailyKept = kept,
                    dailyDeleted = deleted
                ) }
            }
        }
    }
    
    private fun loadReportData() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = LocalDate.now()
            val processedStatuses = setOf("ANALYZED", "KEPT", "DELETED", "STATUS_REJECTED", "READY_TO_CLEAN")

            // Monthly Stats
            val monthlyStats = homeRepository.getMonthlyStats(now.year, now.monthValue)
            var mTotal = 0
            var mKept = 0
            var mDeleted = 0

            monthlyStats.forEach {
                if (it.status in processedStatuses) mTotal += it.count
                if (it.status == "KEPT") mKept = it.count
                if (it.status == "DELETED") mDeleted = it.count
            }

            // Yearly Stats
            val yearlyStats = homeRepository.getYearlyStats(now.year)
            var yTotal = 0
            var yKept = 0
            var yDeleted = 0

            yearlyStats.forEach {
                if (it.status in processedStatuses) yTotal += it.count
                if (it.status == "KEPT") yKept = it.count
                if (it.status == "DELETED") yDeleted = it.count
            }

            _uiState.update { it.copy(
                monthlyReport = ReportStats(mTotal, mKept, mDeleted),
                yearlyReport = ReportStats(yTotal, yKept, yDeleted)
            ) }
        }
    }

    fun refreshGalleryCount() {
        loadGalleryCounts()
    }
}
