package com.bes2.app.ui.screenshot

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bes2.data.dao.TrashItemDao
import com.bes2.data.model.TrashItemEntity
import com.bes2.data.repository.GalleryRepository
import com.bes2.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScreenshotUiState(
    val screenshots: List<TrashItemEntity> = emptyList(),
    val selectedUris: Set<String> = emptySet(), 
    val isLoading: Boolean = true,
    val pendingDeleteUris: List<Uri>? = null,
    val resultMessage: String? = null,
    val showAd: Boolean = false 
)

@HiltViewModel
class ScreenshotViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository,
    private val trashItemDao: TrashItemDao,
    private val settingsRepository: SettingsRepository, 
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
            _uiState.update { it.copy(isLoading = true, showAd = false) }

            val combinedMap = mutableMapOf<String, TrashItemEntity>()

            // 1. Get ALL ready items from DB (using new DAO method or filter)
            // Ideally we should use getAllReadyTrashItems() if added to DAO.
            // Since DAO might be rolled back too, let's use existing getReadyTrashItems with large limit.
            val dbTrashItems = trashItemDao.getReadyTrashItems(limit = 10000) 
            for (item in dbTrashItems) {
                combinedMap[item.uri] = item
            }

            // 2. Get real-time screenshots
            val realScreenshots = galleryRepository.getScreenshots()
            
            for (item in realScreenshots) {
                val uriString = item.uri.toString()
                if (!combinedMap.containsKey(uriString)) {
                    // Check if processed (deleted/kept)
                    if (trashItemDao.isUriProcessed(uriString)) continue
                    
                    val trashItem = TrashItemEntity(
                        id = item.id,
                        uri = uriString,
                        filePath = "",
                        timestamp = item.dateTaken,
                        status = "READY"
                    )
                    combinedMap[uriString] = trashItem
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
            
            val itemsToSave = selectedItems.map { it.copy(status = "KEPT") }
            
            for (item in itemsToSave) {
                val id = trashItemDao.insert(item)
                if (id == -1L) { 
                    trashItemDao.updateStatusByUris(listOf(item.uri), "KEPT")
                }
            }
            
            settingsRepository.incrementDailyStats(keptDelta = selectedItems.size, deletedDelta = 0)
            updateAccumulatedCount(selectedItems.size)
            
            onDeleteCompleted(true)
        }
    }

    fun onDeleteCompleted(success: Boolean) {
        if (success) {
            viewModelScope.launch(Dispatchers.IO) {
                val deletedCount = _uiState.value.pendingDeleteUris?.size ?: 0
                if (deletedCount > 0) {
                    settingsRepository.incrementDailyStats(keptDelta = 0, deletedDelta = deletedCount)
                    updateAccumulatedCount(deletedCount)
                    
                    val pendingUris = _uiState.value.pendingDeleteUris?.map { it.toString() } ?: emptyList()
                    trashItemDao.deleteByUris(pendingUris)
                }
                
                val showAd = checkAdCondition()
                
                _uiState.update { 
                    it.copy(
                        pendingDeleteUris = null, 
                        resultMessage = "처리되었습니다.",
                        showAd = showAd,
                        // [FIX] Clear list and show loading immediately to prevent "frozen" UI
                        screenshots = emptyList(),
                        isLoading = true 
                    ) 
                }
                
                if (!showAd) {
                    loadScreenshots()
                }
            }
        } else {
             _uiState.update { it.copy(pendingDeleteUris = null) }
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
