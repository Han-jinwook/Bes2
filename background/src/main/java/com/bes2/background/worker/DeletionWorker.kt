package com.bes2.background.worker

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bes2.data.dao.ImageItemDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class DeletionWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val imageItemDao: ImageItemDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_IMAGE_IDS = "image_ids"
    }

    override suspend fun doWork(): Result {
        val imageIds = inputData.getLongArray(KEY_IMAGE_IDS)
        if (imageIds == null || imageIds.isEmpty()) {
            Timber.w("No image IDs to delete.")
            return Result.success()
        }

        Timber.d("Starting deletion for ${imageIds.size} images.")

        try {
            val imagesToDelete = imageItemDao.getImagesByIds(imageIds.toList())
            val urisToDelete = imagesToDelete.map { Uri.parse(it.uri) }
            val contentResolver: ContentResolver = context.contentResolver
            var successfullyDeletedUris = mutableListOf<Uri>()

            urisToDelete.forEach { uri ->
                try {
                    val rowsDeleted = contentResolver.delete(uri, null, null)
                    if (rowsDeleted > 0) {
                        successfullyDeletedUris.add(uri)
                    } else {
                        Timber.w("Failed to delete image from storage: %s (no rows deleted)", uri)
                    }
                } catch (e: SecurityException) {
                    Timber.e(e, "Security exception while deleting image: %s", uri)
                }
            }

            if (successfullyDeletedUris.isNotEmpty()) {
                val successfullyDeletedUriStrings = successfullyDeletedUris.map { it.toString() }
                val deletedImageIds = imagesToDelete.filter { it.uri in successfullyDeletedUriStrings }.map { it.id }
                if(deletedImageIds.isNotEmpty()){
                    imageItemDao.updateImageStatusesByIds(deletedImageIds, "DELETED")
                    Timber.i("Successfully marked ${deletedImageIds.size} images as DELETED in DB.")
                }
            }

            return Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error during deletion worker execution.")
            return Result.failure()
        }
    }
}
