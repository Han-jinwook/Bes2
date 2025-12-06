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
                
                    val isDiscoveryRunning = isWorkRunning(listOfNotNull(discoveryInfo))
                    val isAnalysisRunning = isWorkRunning(listOfNotNull(analysisInfo))
                    val isClusteringRunning = isWorkRunning(listOfNotNull(clusteringInfo))
                    
                    val analysisInProgress = isDiscoveryRunning || isAnalysisRunning || isClusteringRunning
                    
                    // [MODIFIED] 4단계(Clustering)가 성공적으로 끝났는지 확인하는 조건 추가
                    val isClusteringFinished = clusteringInfo?.state == WorkInfo.State.SUCCEEDED
                    
                    val needsMemoryEvent = _uiState.value.memoryEvent == null

                    Timber.tag("MEMORY_DEBUG").d(
                        "상태체크: 진행중=%s, 클러스터링완료=%s, 추억필요=%s", 
                        analysisInProgress, isClusteringFinished, needsMemoryEvent
                    )

                    // [MODIFIED] 분석 중이 아니고 + 4단계(Clustering)가 끝났고 + 추억이 아직 없으면 -> 5단계 실행
                    if (!analysisInProgress && isClusteringFinished && needsMemoryEvent) {
                        Timber.tag("MEMORY_DEBUG").d("조건 만족! (4단계 완료됨) -> 5단계 추억 소환 시작")
                        loadMemoryEvent()
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
            Timber.tag("MEMORY_DEBUG").d("찾은 추억 그룹 개수: %d", events.size)

            if (events.isNotEmpty()) {
                val bestEvent = events.first()
                _uiState.update { it.copy(memoryEvent = bestEvent, isMemoryPrepared = false) }
                
                Timber.tag("MEMORY_DEBUG").d("추억 분석 워커 시작 요청: %s", bestEvent.date)
                startMemoryAnalysis(bestEvent.date)
            } else {
                Timber.tag("MEMORY_DEBUG").d("추억으로 만들 만한 사진 그룹이 없음.")
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

    fun triggerBackgroundScan() {
        val discoveryRequest = OneTimeWorkRequestBuilder<PhotoDiscoveryWorker>().build()
        val analysisRequest = OneTimeWorkRequestBuilder<PhotoAnalysisWorker>().build()
        val clusteringRequest = OneTimeWorkRequestBuilder<ClusteringWorker>().build()

        workManager.beginUniqueWork(
            PIPELINE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            discoveryRequest
        )
        .then(analysisRequest)
        .then(clusteringRequest)
        .enqueue()
    }
    
    companion object {
        private const val PIPELINE_WORK_NAME = "FullAnalysisPipeline"
    }
}
