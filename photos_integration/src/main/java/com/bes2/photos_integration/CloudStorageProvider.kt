package com.bes2.photos_integration

import com.bes2.data.model.ImageItemEntity

/**
 * Represents the result of a single image upload operation.
 */
data class UploadResult(
    val originalUri: String,
    val isSuccess: Boolean,
    val remoteUrl: String? = null, // The URL of the uploaded image if successful
    val errorMessage: String? = null,
    // DEFINITIVE FIX: Add a field to hold the actual exception for better logging.
    val cause: Throwable? = null
)

/**
 * A common interface for all cloud storage services to be supported by the app.
 * This ensures a flexible and extensible architecture for adding new providers in the future.
 */
interface CloudStorageProvider {

    /**
     * A unique key to identify the provider (e.g., "google_photos", "naver_mybox").
     */
    val providerKey: String

    /**
     * Uploads a list of user-selected images to the cloud storage.
     *
     * @param images The list of [ImageItemEntity] marked as 'KEPT' to be uploaded.
     * @return A list of [UploadResult], one for each image, indicating the outcome.
     */
    suspend fun uploadImages(images: List<ImageItemEntity>): List<UploadResult>

    // Note: Login/logout functions will be handled by a separate AuthManager
    // to keep responsibilities clean. This provider will assume it receives a valid
    // authentication token when it's time to upload.
}
