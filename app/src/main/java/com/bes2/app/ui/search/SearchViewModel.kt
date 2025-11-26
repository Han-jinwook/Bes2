package com.bes2.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.ImageItemEntity
import com.bes2.ml.SemanticSearchEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val totalIndexedCount: Int = 0
)

data class SearchResult(
    val image: ImageItemEntity,
    val score: Float
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val imageDao: ImageItemDao,
    private val searchEngine: SemanticSearchEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        checkIndexStatus()
    }

    private fun checkIndexStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            // Count images with embeddings
            // Since we don't have a direct query for non-null embeddings yet, 
            // we'll just show 0 or implement a query later.
            // For now, let's just ready the state.
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
    }

    fun performSearch() {
        val query = _uiState.value.query
        if (query.isBlank()) return

        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(isSearching = true) }
            
            try {
                // 1. Encode Query Text
                val textEmbedding = searchEngine.encodeText(query)
                if (textEmbedding == null) {
                    Timber.e("Search failed: Text encoding returned null")
                    _uiState.update { it.copy(isSearching = false) }
                    return@launch
                }

                // 2. Fetch All Images (In a real app, we should fetch only embeddings from DB)
                // Optimization: Add a DAO method to fetch only (id, uri, embedding)
                val allImages = imageDao.getAllImageItemsList() // Need to add this method to DAO
                
                val results = allImages.mapNotNull { image ->
                    val embeddingBytes = image.embedding
                    if (embeddingBytes != null) {
                        // Convert ByteArray back to FloatArray
                        val buffer = java.nio.ByteBuffer.wrap(embeddingBytes)
                        val imageEmbedding = FloatArray(embeddingBytes.size / 4)
                        buffer.asFloatBuffer().get(imageEmbedding)
                        
                        val score = searchEngine.calculateSimilarity(textEmbedding, imageEmbedding)
                        SearchResult(image, score)
                    } else {
                        null
                    }
                }
                .sortedByDescending { it.score }
                .take(50) // Top 50 results

                _uiState.update { it.copy(results = results, isSearching = false) }

            } catch (e: Exception) {
                Timber.e(e, "Error during search")
                _uiState.update { it.copy(isSearching = false) }
            }
        }
    }
}
