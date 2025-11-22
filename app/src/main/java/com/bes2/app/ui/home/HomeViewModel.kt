package com.bes2.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class HomeUiState(
    val dailyTotal: Int = 0,
    val dailyKept: Int = 0,
    val dailyDeleted: Int = 0,
    val galleryTotalCount: Int = 0,
    val screenshotCount: Int = 0,
    val hasPendingReview: Boolean = false // New field
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val imageItemDao: ImageItemDao,
    private val imageClusterDao: ImageClusterDao, // Injected
    private val galleryRepository: GalleryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDailyStats()
        loadGalleryCounts()
        checkPendingReviews()
    }

    private fun checkPendingReviews() {
        viewModelScope.launch {
            imageClusterDao.getImageClustersByReviewStatus("PENDING_REVIEW")
                .collectLatest { clusters ->
                    _uiState.update { it.copy(hasPendingReview = clusters.isNotEmpty()) }
                }
        }
    }

    private fun loadGalleryCounts() {
        viewModelScope.launch(Dispatchers.IO) {
            val totalCount = galleryRepository.getTotalImageCount()
            
            // Get all screenshots from MediaStore
            val allScreenshots = galleryRepository.getScreenshots()
            
            // Filter out processed ones
            var unprocessedCount = 0
            for (item in allScreenshots) {
                val status = imageItemDao.getImageStatusByUri(item.uri.toString())
                if (status != "KEPT" && status != "DELETED") {
                    unprocessedCount++
                }
            }

            _uiState.update { it.copy(
                galleryTotalCount = totalCount,
                screenshotCount = unprocessedCount
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

                val processedStatuses = setOf("ANALYZED", "KEPT", "DELETED", "STATUS_REJECTED")

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
