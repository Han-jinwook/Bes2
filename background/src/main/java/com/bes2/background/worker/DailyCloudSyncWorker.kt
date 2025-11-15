package com.bes2.background.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bes2.background.notification.NotificationHelper
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.repository.SettingsRepository
import com.bes2.photos_integration.auth.ConsentRequiredException
import com.bes2.photos_integration.google.GooglePhotosProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

@HiltWorker
class DailyCloudSyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val imageItemDao: ImageItemDao,
    private val googlePhotosProvider: GooglePhotosProvider,
    private val workManager: WorkManager,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "DailyCloudSyncWorker"
        const val KEY_SYNCED_COUNT = "synced_count"
    }

    override suspend fun doWork(): Result {
        Timber.d("DailyCloudSyncWorker started.")

        try {
            val imagesToUpload = imageItemDao.getImagesByStatusAndUploadFlag("KEPT", false)
            var successfulUploadCount = 0

            if (imagesToUpload.isEmpty()) {
                Timber.d("No new images to upload.")
                rescheduleNextSync()
                val outputData = Data.Builder().putInt(KEY_SYNCED_COUNT, 0).build()
                return Result.success(outputData)
            }

            Timber.d("Found ${imagesToUpload.size} images to upload.")

            val uploadResults = googlePhotosProvider.uploadImages(imagesToUpload)

            val successfulUploads = uploadResults.filter { it.isSuccess }
            val failedUploads = uploadResults.filter { !it.isSuccess }

            if (successfulUploads.isNotEmpty()) {
                val successfullyUploadedUris = successfulUploads.map { it.originalUri }
                imageItemDao.updateUploadedStatusByUris(successfullyUploadedUris, true)
                successfulUploadCount = successfulUploads.size
                Timber.d("Successfully marked $successfulUploadCount images as uploaded.")
            }

            if (failedUploads.isNotEmpty()) {
                failedUploads.forEach { result ->
                    Timber.w(result.cause, "Upload failed for ${result.originalUri}")
                }
                return Result.retry()
            }

            Timber.d("All images uploaded successfully.")
            rescheduleNextSync()
            val outputData = Data.Builder().putInt(KEY_SYNCED_COUNT, successfulUploadCount).build()
            return Result.success(outputData)

        } catch (e: ConsentRequiredException) {
            Timber.w(e, "Consent required for sync. Passing intent to notification.")
            NotificationHelper.showConsentRequiredNotification(appContext, e.resolutionIntent)
            return Result.failure()
        } catch (e: Exception) {
            Timber.e(e, "Error in DailyCloudSyncWorker")
            return Result.retry()
        }
    }

    private suspend fun rescheduleNextSync() {
        val (syncTime, _) = settingsRepository.storedSettings.first()
        val now = ZonedDateTime.now()
        // Always schedule for the next day to ensure a 24-hour cycle.
        var nextSync = now.withHour(syncTime.hour).withMinute(syncTime.minute).withSecond(0).plusDays(1)

        val initialDelay = Duration.between(now, nextSync).toMillis()

        val syncWorkRequest = OneTimeWorkRequestBuilder<DailyCloudSyncWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )

        Timber.d("DailyCloudSyncWorker finished. Next sync re-scheduled for ${nextSync.toLocalTime()}.")
    }
}
