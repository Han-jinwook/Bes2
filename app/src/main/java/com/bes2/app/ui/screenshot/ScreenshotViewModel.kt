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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScreenshotUiState(
    val screenshots: List<TrashItemEntity> = emptyList(),
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

    fun loadScreenshots() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            trashItemDao.getAllTrashItems().collectLatest { trashItems ->
                _uiState.update { it.copy(screenshots = trashItems, isLoading = false) }
            }
        }
    }

    // [FIX] Added stub functions for UI interactions
    fun toggleSelection(item: TrashItemEntity) {
        // Logic to toggle selection
    }

    fun toggleAllSelection(selectAll: Boolean) {
        // Logic to toggle all
    }

    fun deleteSelected() {
        // Logic to delete
    }

    fun keepSelected() {
        // Logic to keep
    }

    fun onDeleteCompleted(success: Boolean) {
        // Logic after deletion
    }
    
    fun messageShown() {
        _uiState.update { it.copy(resultMessage = null) }
    }
}
