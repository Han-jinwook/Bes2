package com.bes2.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
    val screenshotCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val imageItemDao: ImageItemDao,
    private val galleryRepository: GalleryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDailyStats()
        loadGalleryCounts()
    }

    private fun loadGalleryCounts() {
        viewModelScope.launch(Dispatchers.IO) {
            val totalCount = galleryRepository.getTotalImageCount()
            
            // Get all screenshots from MediaStore
            val allScreenshots = galleryRepository.getScreenshots()
            
            // Filter out processed ones (KEPT or DELETED) to get the count of 'Cleaning Targets'
            // We need to check status for each uri.
            // Optimization: Fetch all processed URIs? Or check one by one?
            // Checking one by one might be slow if there are many screenshots.
            // But let's stick to consistency for now.
            
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
