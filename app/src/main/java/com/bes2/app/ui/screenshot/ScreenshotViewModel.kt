package com.bes2.app.ui.screenshot

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.ImageItemEntity
import com.bes2.data.model.ScreenshotItem
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
    val screenshots: List<ScreenshotItem> = emptyList(),
    val isLoading: Boolean = true,
    val pendingDeleteUris: List<Uri>? = null,
    val resultMessage: String? = null,
    val showAd: Boolean = false
)

@HiltViewModel
class ScreenshotViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository,
    private val imageItemDao: ImageItemDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenshotUiState())
    val uiState: StateFlow<ScreenshotUiState> = _uiState.asStateFlow()

    private var lastSelectedCount = 0

    private val PREF_KEY_SCREENSHOT_COUNT = "pref_screenshot_accumulated_count"
    private val AD_THRESHOLD = 40 

    init {
        loadScreenshots()
    }

    fun loadScreenshots() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            
            // 1. Get System Screenshots
            val systemScreenshots = galleryRepository.getScreenshots()
            
            // 2. [Reverted] Get Analyzed Documents from DB
            val documentImages = imageItemDao.getImageItemsByCategory("DOCUMENT")
            
            // 3. Convert Documents to ScreenshotItems
            val convertedDocuments = documentImages.map { entity ->
                ScreenshotItem(
                    id = entity.id,
                    uri = Uri.parse(entity.uri),
                    dateTaken = entity.timestamp,
                    size = 0
                )
            }
            
            // 4. Merge and Dedup (by URI)
            val combinedList = (systemScreenshots + convertedDocuments)
                .distinctBy { it.uri.toString() }
            
            // 5. Filter out already processed items (KEPT or DELETED)
            val finalFilteredList = combinedList.filterNot { item ->
                val status = imageItemDao.getImageStatusByUri(item.uri.toString())
                status == "KEPT" || status == "DELETED"
            }.sortedByDescending { it.dateTaken }
            
            _uiState.update { it.copy(screenshots = finalFilteredList, isLoading = false) }
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
                val uris = selectedItems.map { it.uri.toString() }
                
                imageItemDao.updateImageStatusesByUris(uris, "KEPT")
                
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

                updateAccumulatedCount(count) 
                
                val showAd = checkAdCondition()
                
                _uiState.update { it.copy(resultMessage = "사진 ${count}장을 보관했습니다.", showAd = showAd) }
                loadScreenshots()
            }
        }
    }

    fun onDeleteCompleted(success: Boolean) {
        _uiState.update { it.copy(pendingDeleteUris = null) }
        if (success) {
            updateAccumulatedCount(lastSelectedCount)
            val showAd = checkAdCondition()
            
            _uiState.update { it.copy(resultMessage = "사진 ${lastSelectedCount}장을 삭제했습니다.", showAd = showAd) }
            loadScreenshots()
        }
    }
    
    fun messageShown() {
        _uiState.update { it.copy(resultMessage = null) }
    }
    
    private fun updateAccumulatedCount(count: Int) {
        val prefs = context.getSharedPreferences("bes2_prefs", Context.MODE_PRIVATE)
        val current = prefs.getInt(PREF_KEY_SCREENSHOT_COUNT, 0)
        prefs.edit().putInt(PREF_KEY_SCREENSHOT_COUNT, current + count).apply()
    }
    
    private fun checkAdCondition(): Boolean {
        val prefs = context.getSharedPreferences("bes2_prefs", Context.MODE_PRIVATE)
        val current = prefs.getInt(PREF_KEY_SCREENSHOT_COUNT, 0)
        
        return if (current >= AD_THRESHOLD) {
            prefs.edit().putInt(PREF_KEY_SCREENSHOT_COUNT, current - AD_THRESHOLD).apply()
            true
        } else {
            false
        }
    }
}
