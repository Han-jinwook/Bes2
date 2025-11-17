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
        const val KEY_IS_ONE_TIME_SYNC = "is_one_time_sync" // 상수 추가
    }

    override suspend fun doWork(): Result {
        Timber.d("DailyCloudSyncWorker started.")
        val isOneTimeSync = inputData.getBoolean(KEY_IS_ONE_TIME_SYNC, false)

        try {
            val imagesToUpload = imageItemDao.getImagesByStatusAndUploadFlag("KEPT", false)
            var successfulUploadCount = 0

            if (imagesToUpload.isEmpty()) {
                Timber.d("No new images to upload.")
                if (!isOneTimeSync) rescheduleNextSync() // 수동 동기화가 아닐 때만 재스케줄
                val outputData = Data.Builder().putInt(KEY_SYNCED_COUNT, 0).build()
                return Result.success(outputData)
            }

            Timber.d("Found ${imagesToUpload.size} images to upload.")

            // TODO: 실제 클라우드 제공자를 선택하는 로직 필요
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
                // 실패한 것이 있어도, 성공한 것이 있다면 일단 성공으로 처리하고 다음 동기화 시 재시도
                if (successfulUploads.isNotEmpty()) {
                     val outputData = Data.Builder().putInt(KEY_SYNCED_COUNT, successfulUploadCount).build()
                     if (!isOneTimeSync) rescheduleNextSync()
                     return Result.success(outputData)
                }
                return Result.retry()
            }

            Timber.d("All images uploaded successfully.")
            if (!isOneTimeSync) rescheduleNextSync() // 수동 동기화가 아닐 때만 재스케줄
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
        val settings = settingsRepository.storedSettings.first()
        // 일일 동기화 옵션일 때만 재스케줄
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
            .build() // 제약 조건은 ViewModel에서 설정하므로 여기서는 추가하지 않음

        workManager.enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )

        Timber.d("DailyCloudSyncWorker finished. Next sync re-scheduled for ${nextSync.toLocalTime()}.")
    }
}
