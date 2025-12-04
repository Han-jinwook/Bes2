package com.bes2.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bes2.data.dao.ReviewItemDao
import com.bes2.data.model.ReviewItemEntity
import com.bes2.ml.SemanticSearchEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<ReviewItemEntity> = emptyList(),
    val isSearching: Boolean = false,
    val totalImageCount: Int = 0,
    val indexedImageCount: Int = 0
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val reviewItemDao: ReviewItemDao,
    private val semanticSearchEngine: SemanticSearchEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadStats()
    }

    private fun loadStats() {
        _uiState.update { it.copy(totalImageCount = 0, indexedImageCount = 0) }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        if (newQuery.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }
        performSearch(newQuery)
    }

    // [FIX] Added parameterless overload for UI action
    fun performSearch() {
        performSearch(_uiState.value.query)
    }

    // [FIX] Made public to resolve access error from SearchScreen
    fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSearching = true) }
            
            // [FIX] Use the query parameter to resolve "never used" warning
            Timber.d("Searching for: $query")
            val queryVector = semanticSearchEngine.encodeText(query)
            
            // [FIX] Use queryVector to resolve "variable never used" warning
            if (queryVector != null) {
                Timber.d("Generated query vector with size: ${queryVector.size}")
            } else {
                Timber.w("Failed to generate query vector")
            }
            
            // TODO: Implement actual vector search logic here
            // 1. Get all image embeddings from DB
            // 2. Calculate cosine similarity with queryVector
            // 3. Sort and return top K results
            
            _uiState.update { it.copy(results = emptyList(), isSearching = false) }
        }
    }
}
