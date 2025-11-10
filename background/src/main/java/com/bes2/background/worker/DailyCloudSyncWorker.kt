package com.bes2.background.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bes2.background.notification.NotificationHelper
import com.bes2.data.dao.ImageItemDao
import com.bes2.photos_integration.auth.ConsentRequiredException
import com.bes2.photos_integration.google.GooglePhotosProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class DailyCloudSyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val imageItemDao: ImageItemDao,
    private val googlePhotosProvider: GooglePhotosProvider
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "DailyCloudSyncWorker"
    }

    override suspend fun doWork(): Result {
        Timber.d("DailyCloudSyncWorker started.")

        try {
            val imagesToUpload = imageItemDao.getImagesByStatusAndUploadFlag("KEPT", false)

            if (imagesToUpload.isEmpty()) {
                Timber.d("No new images to upload.")
                return Result.success()
            }

            Timber.d("Found ${imagesToUpload.size} images to upload.")

            val uploadResults = googlePhotosProvider.uploadImages(imagesToUpload)

            val successfulUploads = uploadResults.filter { it.isSuccess }
            val failedUploads = uploadResults.filter { !it.isSuccess }

            if (successfulUploads.isNotEmpty()) {
                val successfullyUploadedUris = successfulUploads.map { it.originalUri }
                imageItemDao.updateUploadedStatusByUris(successfullyUploadedUris, true)
                Timber.d("Successfully marked ${successfulUploads.size} images as uploaded.")
            }

            if (failedUploads.isNotEmpty()) {
                failedUploads.forEach { result ->
                    Timber.w(result.cause, "Upload failed for ${result.originalUri}")
                }
                // If any upload fails, we should retry the whole batch later.
                return Result.retry()
            }

            Timber.d("All images uploaded successfully.")
            return Result.success()

        } catch (e: ConsentRequiredException) {
            // DEFINITIVE FIX based on PLAN.md: Show a notification instead of retrying.
            Timber.w(e, "Consent required for sync. Passing intent to notification.")
            NotificationHelper.showConsentRequiredNotification(appContext, e.resolutionIntent)
            return Result.failure() // Stop the work; user action is required.
        } catch (e: Exception) {
            Timber.e(e, "Error in DailyCloudSyncWorker")
            return Result.retry()
        }
    }
}
