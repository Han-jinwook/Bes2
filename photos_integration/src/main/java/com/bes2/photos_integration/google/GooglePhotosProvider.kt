package com.bes2.photos_integration.google

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.bes2.data.model.ImageItemEntity
import com.bes2.photos_integration.CloudStorageProvider
import com.bes2.photos_integration.UploadResult
import com.bes2.photos_integration.auth.ConsentRequiredException
import com.bes2.photos_integration.auth.GooglePhotosAuthManager
import com.bes2.photos_integration.model.BatchCreateMediaItemsRequest
import com.bes2.photos_integration.model.NewMediaItem
import com.bes2.photos_integration.model.SimpleMediaItem
import com.bes2.photos_integration.network.GooglePhotosApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GooglePhotosProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authManager: GooglePhotosAuthManager,
    private val apiService: GooglePhotosApiService
) : CloudStorageProvider {

    override val providerKey: String = "google_photos"

    override suspend fun uploadImages(images: List<ImageItemEntity>): List<UploadResult> {
        Timber.d("uploadImages: Attempting to get access token.")

        val accessToken = authManager.getAccessToken() ?: run {
            Timber.e("uploadImages: Access token is null. Throwing ConsentRequiredException.")
            val consentIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)!!
            throw ConsentRequiredException(consentIntent)
        }
        
        Timber.d("uploadImages: Successfully retrieved access token.")

        val authToken = "Bearer $accessToken"

        return withContext(Dispatchers.IO) {
            images.map { imageEntity ->
                try {
                    uploadSingleImage(authToken, imageEntity)
                } catch (e: Exception) {
                    Timber.e(e, "uploadImages: Uncaught exception while uploading ${imageEntity.uri}")
                    // DEFINITIVE FIX: Pass the exception to the UploadResult
                    UploadResult(imageEntity.uri, false, e.message ?: "Unknown error", cause = e)
                }
            }
        }
    }

    private suspend fun uploadSingleImage(authToken: String, image: ImageItemEntity): UploadResult {
        Timber.d("uploadSingleImage: Starting upload for URI: ${image.uri}")
        val imageUri = Uri.parse(image.uri)
        
        val (fileName, fileBytes) = try {
            getFileData(imageUri)
        } catch (e: Exception) {
            Timber.e(e, "uploadSingleImage: Failed to read file data for ${image.uri}")
            // DEFINITIVE FIX: Pass the exception to the UploadResult
            return UploadResult(image.uri, false, "Failed to read file from device: ${e.message}", cause = e)
        }
        
        val mimeType = context.contentResolver.getType(imageUri) ?: "application/octet-stream"
        Timber.d("uploadSingleImage: FileName: $fileName, MimeType: $mimeType, Size: ${fileBytes.size} bytes")

        // Step 1: Upload raw bytes to get an upload token
        Timber.d("uploadSingleImage: Step 1 - Uploading raw bytes to get upload token.")
        val mediaType = mimeType.toMediaTypeOrNull()
        val requestBody = fileBytes.toRequestBody(mediaType)

        val uploadResponse = try {
            apiService.uploadMedia(authToken, mimeType, "raw", requestBody)
        } catch(e: Exception) {
            Timber.e(e, "uploadSingleImage: Step 1 API call failed with exception.")
            // DEFINITIVE FIX: Pass the exception to the UploadResult
            return UploadResult(image.uri, false, "Network request for upload token failed: ${e.message}", cause = e)
        }

        val uploadToken = uploadResponse.body()
        if (!uploadResponse.isSuccessful || uploadToken.isNullOrBlank()) {
            val errorBody = uploadResponse.errorBody()?.string()
            Timber.e("uploadSingleImage: Step 1 FAILED. Response: ${uploadResponse.code()}, Error: $errorBody")
            val cause = Exception("Google Photos API Error (Upload Token) - Code: ${uploadResponse.code()}, Body: $errorBody")
            return UploadResult(image.uri, false, "Failed to get upload token: $errorBody", cause = cause)
        }
        Timber.d("uploadSingleImage: Step 1 SUCCESS. Received upload token: $uploadToken")

        // Step 2: Use the upload token to create a media item
        Timber.d("uploadSingleImage: Step 2 - Using upload token to create media item.")
        val newMediaItem = NewMediaItem(
            description = "Uploaded from Bes2 App",
            simpleMediaItem = SimpleMediaItem(uploadToken = uploadToken, fileName = fileName)
        )
        val createRequest = BatchCreateMediaItemsRequest(newMediaItems = listOf(newMediaItem))
        
        val createResponse = try {
            apiService.createMediaItem(authToken, createRequest)
        } catch(e: Exception) {
            Timber.e(e, "uploadSingleImage: Step 2 API call failed with exception.")
            // DEFINITIVE FIX: Pass the exception to the UploadResult
            return UploadResult(image.uri, false, "Network request for create media item failed: ${e.message}", cause = e)
        }

        val result = createResponse.body()?.newMediaItemResults?.firstOrNull()
        val statusCode = result?.status?.code

        return if (createResponse.isSuccessful && statusCode == 0) {
            Timber.d("uploadSingleImage: Step 2 SUCCESS. Image uploaded successfully to Google Photos.")
            UploadResult(image.uri, true)
        } else {
            val errorMessage = result?.status?.message ?: createResponse.errorBody()?.string() ?: "Unknown upload error"
            Timber.e("uploadSingleImage: Step 2 FAILED. Status code: $statusCode, Message: $errorMessage")
            // DEFINITIVE FIX: Create a generic exception to hold the message for logging purposes in the worker.
            val cause = Exception("Google Photos API Error (Create Item) - Code: $statusCode, Message: $errorMessage")
            UploadResult(image.uri, false, errorMessage, cause = cause)
        }
    }

    private fun getFileData(uri: Uri): Pair<String, ByteArray> {
        val contentResolver = context.contentResolver
        val fileName = contentResolver.query(uri, null, null, null, null)?.use {
            it.moveToFirst()
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.getString(nameIndex)
        } ?: "upload.jpg"

        val fileBytes = contentResolver.openInputStream(uri)!!.use { it.readBytes() }
        return fileName to fileBytes
    }
}
