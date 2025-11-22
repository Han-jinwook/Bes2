package com.bes2.app.ui.screenshot

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.ImageItemEntity
import com.bes2.data.model.ScreenshotItem
import com.bes2.data.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScreenshotUiState(
    val screenshots: List<ScreenshotItem> = emptyList(),
    val isLoading: Boolean = true,
    val pendingDeleteUris: List<Uri>? = null,
    val resultMessage: String? = null // Added for reliable message display
)

@HiltViewModel
class ScreenshotViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository,
    private val imageItemDao: ImageItemDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenshotUiState())
    val uiState: StateFlow<ScreenshotUiState> = _uiState.asStateFlow()

    // Track selection for message
    private var lastSelectedCount = 0

    init {
        loadScreenshots()
    }

    fun loadScreenshots() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val allScreenshots = galleryRepository.getScreenshots()
            
            val filteredList = allScreenshots.filterNot { item ->
                val status = imageItemDao.getImageStatusByUri(item.uri.toString())
                status == "KEPT" || status == "DELETED"
            }
            
            _uiState.update { it.copy(screenshots = filteredList, isLoading = false) }
        }
    }

    fun toggleSelection(item: ScreenshotItem) {
        _uiState.update { state ->
            val updatedList = state.screenshots.map {
                if (it.id == item.id) it.copy(isSelected = !it.isSelected) else it
            }
            state.copy(screenshots = updatedList)
        }
    }

    fun toggleAllSelection(selectAll: Boolean) {
        _uiState.update { state ->
            val updatedList = state.screenshots.map { it.copy(isSelected = selectAll) }
            state.copy(screenshots = updatedList)
        }
    }

    fun deleteSelected() {
        val selectedUris = _uiState.value.screenshots.filter { it.isSelected }.map { it.uri }
        lastSelectedCount = selectedUris.size
        if (selectedUris.isNotEmpty()) {
            _uiState.update { it.copy(pendingDeleteUris = selectedUris) }
        }
    }

    fun keepSelected() {
        viewModelScope.launch(Dispatchers.IO) {
            val selectedItems = _uiState.value.screenshots.filter { it.isSelected }
            val count = selectedItems.size
            if (count > 0) {
                val entities = selectedItems.map { item ->
                    ImageItemEntity(
                        uri = item.uri.toString(),
                        filePath = "",
                        timestamp = item.dateTaken,
                        status = "KEPT",
                        pHash = null, nimaScore = null, blurScore = null, exposureScore = null,
                        areEyesClosed = null, smilingProbability = null, clusterId = null
                    )
                }
                imageItemDao.insertImageItems(entities)
                val uris = selectedItems.map { it.uri.toString() }
                imageItemDao.updateImageStatusesByUris(uris, "KEPT")

                _uiState.update { it.copy(resultMessage = "스크린샷 ${count}장을 보관했습니다.") }
                loadScreenshots()
            }
        }
    }

    fun onDeleteCompleted(success: Boolean) {
        _uiState.update { it.copy(pendingDeleteUris = null) }
        if (success) {
            _uiState.update { it.copy(resultMessage = "스크린샷 ${lastSelectedCount}장을 삭제했습니다.") }
            loadScreenshots()
        }
    }
    
    fun messageShown() {
        _uiState.update { it.copy(resultMessage = null) }
    }
}
