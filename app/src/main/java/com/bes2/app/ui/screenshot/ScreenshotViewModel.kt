package com.bes2.app.ui.screenshot

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bes2.background.worker.DailyCloudSyncWorker
import com.bes2.data.dao.ReviewItemDao
import com.bes2.data.dao.TrashItemDao
import com.bes2.data.model.ReviewItemEntity
import com.bes2.data.model.TrashItemEntity
import com.bes2.data.repository.GalleryRepository
import com.bes2.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ScreenshotUiState(
    val screenshots: List<TrashItemEntity> = emptyList(),
    val selectedUris: Set<String> = emptySet(), 
    val isLoading: Boolean = false,
    val pendingDeleteUris: List<Uri>? = null,
    val resultMessage: String? = null,
    val showAd: Boolean = false 
)

@HiltViewModel
class ScreenshotViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository,
    private val trashItemDao: TrashItemDao,
    private val reviewItemDao: ReviewItemDao,
    private val settingsRepository: SettingsRepository,
    private val workManager: WorkManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenshotUiState())
    val uiState: StateFlow<ScreenshotUiState> = _uiState.asStateFlow()

    private val PREF_KEY_TRASH_COUNT = "pref_trash_accumulated_count"
    private val AD_THRESHOLD = 30 

    init {
        loadScreenshots()
    }

    fun loadScreenshots() {
        viewModelScope.launch(Dispatchers.IO) {
            // [FIX] Removed faulty guard that caused infinite loading
            _uiState.update { it.copy(isLoading = true) }
            Timber.d("ScreenshotViewModel: Starting to load screenshots...")

            try {
                val combinedMap = mutableMapOf<String, TrashItemEntity>()

                // 1. Get ready items from DB
                val dbTrashItems = trashItemDao.getReadyTrashItems(limit = 5000) 
                for (item in dbTrashItems) {
                    combinedMap[item.uri] = item
                }

                // 2. Get real-time screenshots
                val realScreenshots = galleryRepository.getScreenshots()
                for (item in realScreenshots) {
                    val uriString = item.uri.toString()
                    if (!combinedMap.containsKey(uriString)) {
                        if (trashItemDao.isUriProcessed(uriString)) continue
                        if (reviewItemDao.isUriProcessed(uriString)) continue
                        
                        combinedMap[uriString] = TrashItemEntity(
                            id = item.id, uri = uriString, filePath = "",
                            timestamp = item.dateTaken, status = "READY"
                        )
                    }
                }

                val combinedList = combinedMap.values.sortedByDescending { it.timestamp }
                val allUris = combinedList.map { it.uri }.toSet()
                
                _uiState.update { 
                    it.copy(
                        screenshots = combinedList, 
                        selectedUris = allUris,
                        isLoading = false
                    ) 
                }
                Timber.d("ScreenshotViewModel: Loaded ${combinedList.size} items.")
            } catch (e: Exception) {
                Timber.e(e, "ScreenshotViewModel: Error loading screenshots")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun toggleSelection(item: TrashItemEntity) {
        _uiState.update { state ->
            val newSelection = state.selectedUris.toMutableSet()
            if (newSelection.contains(item.uri)) {
                newSelection.remove(item.uri)
            } else {
                newSelection.add(item.uri)
            }
            state.copy(selectedUris = newSelection)
        }
    }

    fun toggleAllSelection(selectAll: Boolean) {
        _uiState.update { state ->
            val newSelection = if (selectAll) {
                state.screenshots.map { it.uri }.toSet()
            } else {
                emptySet()
            }
            state.copy(selectedUris = newSelection)
        }
    }

    fun deleteSelected() {
        val selected = _uiState.value.selectedUris
        if (selected.isEmpty()) return
        
        val urisToDelete = selected.map { Uri.parse(it) }
        _uiState.update { it.copy(pendingDeleteUris = urisToDelete) }
    }

    fun keepSelected() {
        val selected = _uiState.value.selectedUris
        if (selected.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val currentList = _uiState.value.screenshots
            val selectedItems = currentList.filter { selected.contains(it.uri) }
            
            val reviewItems = selectedItems.map { 
                ReviewItemEntity(
                    uri = it.uri, filePath = it.filePath, timestamp = it.timestamp,
                    status = "KEPT", source_type = "TRASH"
                )
            }
            
            reviewItemDao.insertAll(reviewItems)
            
            val urisToRemove = selectedItems.map { it.uri }
            trashItemDao.deleteByUris(urisToRemove)
            
            settingsRepository.incrementDailyStats(keptDelta = selectedItems.size, deletedDelta = 0)
            updateAccumulatedCount(selectedItems.size)
            
            triggerSyncIfNeeded()
            onDeleteCompleted(true, isKeepAction = true)
        }
    }

    fun onDeleteCompleted(success: Boolean, isKeepAction: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (success) {
                if (!isKeepAction) {
                    val pendingUris = _uiState.value.pendingDeleteUris?.map { it.toString() } ?: emptyList()
                    if (pendingUris.isNotEmpty()) {
                        settingsRepository.incrementDailyStats(keptDelta = 0, deletedDelta = pendingUris.size)
                        updateAccumulatedCount(pendingUris.size)
                        trashItemDao.deleteByUris(pendingUris)
                    }
                }
                
                val showAd = checkAdCondition()
                
                _uiState.update { 
                    it.copy(
                        pendingDeleteUris = null, 
                        resultMessage = if (isKeepAction) "보관되었습니다." else "정리되었습니다.",
                        showAd = showAd,
                        selectedUris = emptySet(),
                        isLoading = !showAd // Set loading and immediately refresh if no ad
                    ) 
                }
                
                if (!showAd) {
                    loadScreenshots()
                }
            } else {
                 _uiState.update { it.copy(pendingDeleteUris = null, isLoading = false) }
            }
        }
    }
    
    private suspend fun triggerSyncIfNeeded() {
        val settings = settingsRepository.storedSettings.first()
        if (settings.syncOption == "IMMEDIATE") {
            val constraints = if (settings.uploadOnWifiOnly) Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build() else Constraints.NONE
            val inputData = Data.Builder().putBoolean(DailyCloudSyncWorker.KEY_IS_ONE_TIME_SYNC, true).build()
            val syncWorkRequest = OneTimeWorkRequestBuilder<DailyCloudSyncWorker>().setConstraints(constraints).setInputData(inputData).build()
            workManager.enqueue(syncWorkRequest)
        }
    }
    
    fun messageShown() {
        _uiState.update { it.copy(resultMessage = null) }
    }
    
    fun adShown() {
        _uiState.update { it.copy(showAd = false) }
        loadScreenshots()
    }
    
    private fun updateAccumulatedCount(count: Int) {
        val prefs = context.getSharedPreferences("bes2_prefs", Context.MODE_PRIVATE)
        val current = prefs.getInt(PREF_KEY_TRASH_COUNT, 0)
        prefs.edit().putInt(PREF_KEY_TRASH_COUNT, current + count).apply()
    }
    
    private fun checkAdCondition(): Boolean {
        val prefs = context.getSharedPreferences("bes2_prefs", Context.MODE_PRIVATE)
        val current = prefs.getInt(PREF_KEY_TRASH_COUNT, 0)
        return if (current >= AD_THRESHOLD) {
            prefs.edit().putInt(PREF_KEY_TRASH_COUNT, current - AD_THRESHOLD).apply()
            true
        } else {
            false
        }
    }
}
