package com.bes2.app.ui.review

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.ImageClusterEntity
import com.bes2.data.model.ImageItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModel @Inject constructor(
    private val imageClusterDao: ImageClusterDao,
    private val imageItemDao: ImageItemDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReviewUiState>(ReviewUiState.Loading)
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            imageClusterDao.getImageClustersByReviewStatus("PENDING_REVIEW")
                .flatMapLatest { clusters ->
                    if (clusters.isEmpty()) {
                        viewModelScope.launch { _navigationEvent.emit(NavigationEvent.NavigateToSettings) }
                        kotlinx.coroutines.flow.flowOf(ReviewUiState.NoClustersToReview)
                    } else {
                        val currentCluster = clusters.first()
                        // DEFINITIVE FIX: Convert Long to String to match DAO function signature
                        imageItemDao.getImageItemsByClusterId(currentCluster.id.toString())
                            .map { images ->
                                if (images.isEmpty()) {
                                    imageClusterDao.updateImageClusterReviewStatus(currentCluster.id, "REVIEW_COMPLETED")
                                    ReviewUiState.Loading
                                } else {
                                    // **FIX:** Re-calculate the best images from scratch to ensure correctness.
                                    val sortedImages = images
                                        .filter { it.status != "STATUS_REJECTED" }
                                        .sortedWith(
                                            compareBy<ImageItemEntity> { it.areEyesClosed == true } // Penalize closed eyes first
                                            .thenByDescending { calculateFinalScore(it) }     // Then sort by score
                                        )

                                    val initialBest = sortedImages.getOrNull(0)
                                    val initialSecond = sortedImages.getOrNull(1)
                                    calculateReadyState(currentCluster, images, initialBest, initialSecond)
                                }
                            }
                    }
                }
                .collect { newState -> _uiState.value = newState }
        }
    }

    fun selectImage(imageToSelect: ImageItemEntity) {
        val currentState = _uiState.value
        if (currentState is ReviewUiState.Ready) {
            val allSelected = listOfNotNull(currentState.selectedBestImage, currentState.selectedSecondBestImage)
            val alreadySelected = allSelected.any { it.id == imageToSelect.id }

            val newSelection = if (alreadySelected) {
                allSelected.filterNot { it.id == imageToSelect.id }
            } else {
                if (allSelected.size < 2) {
                    allSelected + imageToSelect
                } else {
                    val sortedSelection = allSelected.sortedBy { calculateFinalScore(it) }
                    listOf(sortedSelection.last(), imageToSelect)
                }
            }
            
            val newBest = newSelection.getOrNull(0)
            val newSecond = newSelection.getOrNull(1)

            _uiState.value = calculateReadyState(currentState.cluster, currentState.allImages, newBest, newSecond)
        }
    }
    
    // Restored to original logic
    private fun calculateFinalScore(image: ImageItemEntity): Float {
        val nimaScore = (image.nimaScore ?: 0f) * 10
        val smileBonus = (image.smilingProbability ?: 0f) * 10
        return nimaScore + smileBonus
    }

    // Restored to original, robust sorting logic
    private fun calculateReadyState(cluster: ImageClusterEntity, allImages: List<ImageItemEntity>, newFirst: ImageItemEntity?, newSecond: ImageItemEntity?): ReviewUiState.Ready {
        val (analyzedImages, rejectedImages) = allImages.partition { it.status != "STATUS_REJECTED" }

        val selection = listOfNotNull(newFirst, newSecond)
            .sortedWith(
                compareBy<ImageItemEntity> { it.areEyesClosed == true } // 1. Penalize closed eyes
                .thenByDescending { calculateFinalScore(it) }      // 2. Sort by final score
            )
        val finalBest = selection.getOrNull(0)
        val finalSecond = selection.getOrNull(1)
        
        val selectedUris = setOfNotNull(finalBest?.uri, finalSecond?.uri)
        
        val otherImages = analyzedImages
            .filterNot { it.uri in selectedUris }
            .sortedByDescending { calculateFinalScore(it) }

        return ReviewUiState.Ready(
            cluster = cluster,
            allImages = allImages,
            otherImages = otherImages,
            rejectedImages = rejectedImages,
            selectedBestImage = finalBest,
            selectedSecondBestImage = finalSecond
        )
    }

    fun keepSelectedImages() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ReviewUiState.Ready) {
                val keptImageIds = listOfNotNull(currentState.selectedBestImage, currentState.selectedSecondBestImage).map { it.id }
                if (keptImageIds.isNotEmpty()) {
                    imageItemDao.updateImageStatusesByIds(keptImageIds, "KEPT")
                }
                val otherImageIds = currentState.otherImages.map { it.id }
                if (otherImageIds.isNotEmpty()) {
                    imageItemDao.updateImageStatusesByIds(otherImageIds, "REVIEWED")
                }
                imageClusterDao.updateImageClusterReviewStatus(currentState.cluster.id, "REVIEW_COMPLETED")
            }
        }
    }

    fun deleteOtherImages() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ReviewUiState.Ready) {
                val imagesToDelete = currentState.otherImages + currentState.rejectedImages
                if (imagesToDelete.isNotEmpty()) {
                    val urisToDelete = imagesToDelete.map { Uri.parse(it.uri) }
                     _uiState.value = currentState.copy(pendingDeleteRequest = urisToDelete)
                }
            }
        }
    }

    fun onDeletionRequestHandled(successfullyDeleted: Boolean) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ReviewUiState.Ready) {
                _uiState.value = currentState.copy(pendingDeleteRequest = null)

                if (successfullyDeleted) {
                    val imageIdsToDelete = (currentState.otherImages + currentState.rejectedImages).map { it.id }
                    if (imageIdsToDelete.isNotEmpty()) {
                        imageItemDao.updateImageStatusesByIds(imageIdsToDelete, "DELETED")
                    }
                    val keptImageIds = listOfNotNull(currentState.selectedBestImage, currentState.selectedSecondBestImage).map { it.id }
                    if (keptImageIds.isNotEmpty()) {
                        imageItemDao.updateImageStatusesByIds(keptImageIds, "KEPT")
                    }
                    imageClusterDao.updateImageClusterReviewStatus(currentState.cluster.id, "REVIEW_COMPLETED")
                }
            }
        }
    }
}

sealed interface ReviewUiState {
    object Loading : ReviewUiState
    object NoClustersToReview : ReviewUiState
    data class Ready(
        val cluster: ImageClusterEntity,
        val allImages: List<ImageItemEntity>,
        val otherImages: List<ImageItemEntity>,
        val rejectedImages: List<ImageItemEntity>,
        val selectedBestImage: ImageItemEntity?,
        val selectedSecondBestImage: ImageItemEntity?,
        val pendingDeleteRequest: List<Uri>? = null
    ) : ReviewUiState
}

sealed interface NavigationEvent {
    object NavigateToSettings : NavigationEvent
}
