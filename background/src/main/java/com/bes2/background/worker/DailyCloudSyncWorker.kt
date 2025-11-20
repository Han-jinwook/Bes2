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
import com.bes2.photos_integration.CloudStorageProvider
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
        const val KEY_IS_ONE_TIME_SYNC = "is_one_time_sync"
    }

    override suspend fun doWork(): Result {
        Timber.d("DailyCloudSyncWorker started.")
        val isOne_timeSync = inputData.getBoolean(KEY_IS_ONE_TIME_SYNC, false)
        lateinit var provider: CloudStorageProvider

        try {
            // DIAGNOSTIC LOGGING START
            val allKeptImages = imageItemDao.getImageItemsListByStatus("KEPT")
            val alreadyUploadedCount = allKeptImages.count { it.isUploaded }
            val notUploadedCount = allKeptImages.count { !it.isUploaded }
            Timber.d("DIAGNOSIS: Total KEPT images: ${allKeptImages.size}, Already Uploaded: $alreadyUploadedCount, Pending Upload: $notUploadedCount")
            // DIAGNOSTIC LOGGING END

            val imagesToUpload = imageItemDao.getImagesByStatusAndUploadFlag("KEPT", false)
            var successfulUploadCount = 0

            if (imagesToUpload.isEmpty()) {
                Timber.d("No new images to upload. (Checked ${allKeptImages.size} KEPT images)")
                if (!isOne_timeSync) rescheduleNextSync()
                val outputData = Data.Builder().putInt(KEY_SYNCED_COUNT, 0).build()
                return Result.success(outputData)
            }

            Timber.d("Found ${imagesToUpload.size} images to upload.")

            // Always use Google Photos
            provider = googlePhotosProvider
            Timber.d("Using cloud provider: ${provider.providerKey}")
            
            val uploadResults = provider.uploadImages(imagesToUpload)

            val successfulUploads = uploadResults.filter { it.isSuccess }
            val failedUploads = uploadResults.filter { !it.isSuccess }

            if (successfulUploads.isNotEmpty()) {
                val successfullyUploadedUris = successfulUploads.map { it.originalUri }
                imageItemDao.updateUploadedStatusByUris(successfullyUploadedUris, true)
                successfulUploadCount = successfulUploads.size
                Timber.d("Successfully marked $successfulUploadCount images as uploaded.")
                
                // Calculate distinct cluster count
                val uploadedImages = imagesToUpload.filter { it.uri in successfullyUploadedUris }
                val clusterCount = uploadedImages.mapNotNull { it.clusterId }.distinct().size
                
                // Notify user about success
                NotificationHelper.showSyncSuccessNotification(appContext, successfulUploadCount, clusterCount)
            }

            if (failedUploads.isNotEmpty()) {
                failedUploads.forEach { result ->
                    Timber.w(result.cause, "Upload failed for ${result.originalUri}: ${result.errorMessage}")
                }
                if (successfulUploads.isNotEmpty()) {
                     val outputData = Data.Builder().putInt(KEY_SYNCED_COUNT, successfulUploadCount).build()
                     if (!isOne_timeSync) rescheduleNextSync()
                     return Result.success(outputData)
                }
                
                return Result.retry()
            }

            Timber.d("All images uploaded successfully.")
            if (!isOne_timeSync) rescheduleNextSync()
            val outputData = Data.Builder().putInt(KEY_SYNCED_COUNT, successfulUploadCount).build()
            return Result.success(outputData)

        } catch (e: ConsentRequiredException) {
            Timber.w(e, "Consent required for sync.")
            val intent = e.resolutionIntent
            if (intent != null) {
                Timber.d("Showing notification with resolution intent.")
                NotificationHelper.showConsentRequiredNotification(appContext, intent, provider.providerKey)
            } else {
                Timber.d("Showing generic login required notification.")
                NotificationHelper.showLoginRequiredNotification(appContext, provider.providerKey)
            }
            return Result.failure()
        } catch (e: Exception) {
            Timber.e(e, "Error in DailyCloudSyncWorker")
            return Result.retry()
        }
    }

    private suspend fun rescheduleNextSync() {
        val settings = settingsRepository.storedSettings.first()
        if (settings.syncOption != "DAILY") {
            Timber.d("Rescheduling skipped as sync option is not DAILY.")
            return
        }
        val syncTime = settings.syncTime
        val now = ZonedDateTime.now()
        var nextSync = now.withHour(syncTime.hour).withMinute(syncTime.minute).withSecond(0)
        if (nextSync.isBefore(now) || nextSync.isEqual(now)) {
            nextSync = nextSync.plusDays(1)
        }

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
