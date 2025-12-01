package com.bes2.app.ui.screenshot

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bes2.data.dao.TrashItemDao
import com.bes2.data.model.TrashItemEntity
import com.bes2.data.repository.GalleryRepository
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
    val selectedUris: Set<String> = emptySet(), // [ADDED] Selection state
    val isLoading: Boolean = true,
    val pendingDeleteUris: List<Uri>? = null,
    val resultMessage: String? = null,
    val showAd: Boolean = false
)

@HiltViewModel
class ScreenshotViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository,
    private val trashItemDao: TrashItemDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenshotUiState())
    val uiState: StateFlow<ScreenshotUiState> = _uiState.asStateFlow()

    init {
        loadScreenshots()
    }

    // [MODIFIED] Prioritize Real Screenshots -> Then DB Trash Items
    fun loadScreenshots() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }

            val combinedList = mutableListOf<TrashItemEntity>()
            val urisToCheck = mutableSetOf<String>()

            // 1. Fetch Real Screenshots from MediaStore (Priority #1)
            val realScreenshots = galleryRepository.getScreenshots()
            
            for (item in realScreenshots) {
                // Filter out already processed items
                if (trashItemDao.isUriProcessed(item.uri.toString())) continue
                
                val trashItem = TrashItemEntity(
                    id = item.id,
                    uri = item.uri.toString(),
                    filePath = "",
                    timestamp = item.dateTaken,
                    status = "READY"
                )
                combinedList.add(trashItem)
                urisToCheck.add(item.uri.toString())
                
                if (combinedList.size >= 30) break
            }

            // 2. If we need more, fetch from DB (Worker results) (Priority #2)
            if (combinedList.size < 30) {
                val dbTrashItems = trashItemDao.getReadyTrashItems(limit = 30 - combinedList.size)
                for (item in dbTrashItems) {
                    if (!urisToCheck.contains(item.uri)) {
                        combinedList.add(item)
                    }
                }
            }

            // 3. Update UI & Select All by default
            val allUris = combinedList.map { it.uri }.toSet()
            _uiState.update { 
                it.copy(
                    screenshots = combinedList, 
                    selectedUris = allUris, // Default: Select All
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
        
        // Convert String URIs to Uri objects for MediaStore deletion
        val urisToDelete = selected.map { Uri.parse(it) }
        _uiState.update { it.copy(pendingDeleteUris = urisToDelete) }
    }

    fun keepSelected() {
        val selected = _uiState.value.selectedUris
        if (selected.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            // Mark as KEPT in DB (so they don't appear again)
            // Even if they are MediaStore items, we save them to DB as KEPT to ignore them next time.
            
            // We need to insert them first if they don't exist (for MediaStore items)
            // Or update if they exist.
            // Since we don't have full Entity info here easily, let's just update status by URI.
            // But wait, if it's not in DB, update won't work.
            
            // Strategy: 
            // 1. Get current list from UI state
            // 2. Filter selected items
            // 3. Insert/Update them with status = 'KEPT'
            
            val currentList = _uiState.value.screenshots
            val selectedItems = currentList.filter { selected.contains(it.uri) }
            
            val itemsToSave = selectedItems.map { it.copy(status = "KEPT") }
            trashItemDao.insertAll(itemsToSave) // Use insertAll with OnConflictStrategy.REPLACE or IGNORE -> then Update?
            // TrashItemDao.insert is IGNORE. 
            // So we should Insert then Update, or just Insert with Replace.
            // Current DAO has IGNORE. 
            // Let's rely on `isUriProcessed` check in `loadScreenshots`.
            // If we insert them with 'KEPT', `isUriProcessed` will return true?
            // `isUriProcessed` checks if exists. 
            // We need `isUriProcessed` to return true if status is KEPT or DELETED.
            // Actually `TrashItemDao` usually holds garbage.
            // If we Keep it, maybe we should move it to `ReviewItemDao`? 
            // Or just mark as KEPT in TrashItem table. Let's stick to TrashItem table for now.
            
            // To be safe: Insert (if new) -> Update status.
            for (item in itemsToSave) {
                val id = trashItemDao.insert(item)
                if (id == -1L) { // Already exists
                    trashItemDao.updateStatusByUris(listOf(item.uri), "KEPT")
                }
            }
            
            // Refresh
            onDeleteCompleted(true)
        }
    }

    // [MODIFIED] Refresh list after deletion/keeping
    fun onDeleteCompleted(success: Boolean) {
        if (success) {
            // Also mark deleted items in DB so we don't fetch them again (if they were MediaStore items)
            // Wait, if MediaStore delete succeeds, they are gone from MediaStore.
            // So `galleryRepository.getScreenshots()` won't return them.
            // But for DB items, we need to delete them from DB or mark as DELETED.
            
            viewModelScope.launch(Dispatchers.IO) {
                val pendingUris = _uiState.value.pendingDeleteUris?.map { it.toString() } ?: emptyList()
                if (pendingUris.isNotEmpty()) {
                    trashItemDao.deleteByUris(pendingUris) // Remove from Trash DB
                    // Or update to DELETED? "Trash Cleaning" implies removal.
                    // If file is gone, remove from DB too.
                }
                
                _uiState.update { it.copy(pendingDeleteUris = null, resultMessage = "처리되었습니다.") }
                loadScreenshots() // [KEY] Infinite Refill
            }
        } else {
             _uiState.update { it.copy(pendingDeleteUris = null) }
        }
    }
    
    fun messageShown() {
        _uiState.update { it.copy(resultMessage = null) }
    }
}
