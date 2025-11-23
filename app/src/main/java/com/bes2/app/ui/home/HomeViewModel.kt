package com.bes2.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bes2.background.worker.PastPhotoAnalysisWorker
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.repository.GalleryRepository
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

data class HomeUiState(
    val dailyTotal: Int = 0,
    val dailyKept: Int = 0,
    val dailyDeleted: Int = 0,
    val galleryTotalCount: Int = 0, // Represents unprocessed images count now
    val screenshotCount: Int = 0,
    val hasPendingReview: Boolean = false,
    val readyToCleanCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val imageItemDao: ImageItemDao,
    private val imageClusterDao: ImageClusterDao,
    private val galleryRepository: GalleryRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDailyStats()
        loadGalleryCounts()
        checkPendingReviews()
        monitorReadyToClean()
        startBackgroundAnalysis()
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
            
            // Calculate how many are already processed (KEPT or DELETED)
            // This is an approximation. A better way is to query DB for count of KEPT/DELETED.
            // However, syncing total MediaStore count with DB status for each item is expensive.
            // Let's use a simple approach: totalCount - processedCount (from DB stats).
            
            val processedCount = imageItemDao.countImagesByStatus("KEPT") + 
                                 imageItemDao.countImagesByStatus("DELETED") +
                                 imageItemDao.countImagesByStatus("STATUS_REJECTED")
            
            // Unprocessed count
            val unprocessedTotal = (totalCount - processedCount).coerceAtLeast(0)

            // Get all screenshots from MediaStore
            val allScreenshots = galleryRepository.getScreenshots()
            
            // Filter out processed screenshots
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
    
    fun refreshGalleryCount() {
        loadGalleryCounts()
    }
}
