package com.bes2.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bes2.data.dao.ImageItemDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class HomeUiState(
    val dailyTotal: Int = 0,
    val dailyKept: Int = 0,
    val dailyDeleted: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val imageItemDao: ImageItemDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDailyStats()
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

                // Only count photos that have been processed or analyzed.
                // Exclude: NEW, IGNORED (Screenshots), PRE_CLUSTERING, PENDING_ANALYSIS
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
                
                _uiState.value = HomeUiState(
                    dailyTotal = total,
                    dailyKept = kept,
                    dailyDeleted = deleted
                )
            }
        }
    }
}
