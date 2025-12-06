package com.bes2.background.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bes2.background.notification.NotificationHelper
import com.bes2.data.dao.ReviewItemDao
import com.bes2.data.model.ImageItemEntity
import com.bes2.photos_integration.auth.ConsentRequiredException
import com.bes2.photos_integration.google.GooglePhotosProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class DailyCloudSyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val reviewItemDao: ReviewItemDao,
    private val googlePhotosProvider: GooglePhotosProvider
) : CoroutineWorker(appContext, workerParams) {
    
    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("Sync worker started.")
        
        try {
            val unsyncedItems = reviewItemDao.getKeptUnsyncedItems()
            if (unsyncedItems.isEmpty()) {
                Timber.tag(TAG).d("No items to sync.")
                return Result.success()
            }
            
            Timber.tag(TAG).d("Found ${unsyncedItems.size} items to sync.")
            
            val imagesToUpload = unsyncedItems.map { 
                ImageItemEntity(
                    id = it.id,
                    uri = it.uri,
                    filePath = it.filePath,
                    timestamp = it.timestamp
                )
            }
            
            val uploadResults = googlePhotosProvider.uploadImages(imagesToUpload)
            
            val successfulIds = mutableListOf<Long>()
            var failCount = 0
            
            uploadResults.forEachIndexed { index, result ->
                if (result.isSuccess) {
                    successfulIds.add(unsyncedItems[index].id)
                } else {
                    failCount++
                    Timber.tag(TAG).e("Upload failed for ${result.originalUri}: ${result.errorMessage}", result.cause)
                }
            }
            
            if (successfulIds.isNotEmpty()) {
                reviewItemDao.markAsUploaded(successfulIds)
                Timber.tag(TAG).d("Successfully uploaded and marked ${successfulIds.size} items.")
                
                // Show success notification
                // Using 1 for cluster count as a dummy value since we are syncing kept items
                NotificationHelper.showSyncSuccessNotification(appContext, successfulIds.size, 1)
            }
            
            if (failCount > 0) {
                Timber.tag(TAG).w("$failCount uploads failed.")
                if (successfulIds.isEmpty()) {
                     // If everything failed, retry later
                     return Result.retry()
                }
            }
            
            return Result.success()
            
        } catch (e: ConsentRequiredException) {
            Timber.tag(TAG).e("Consent required for Google Photos upload.")
            // Use existing helper
            NotificationHelper.showConsentRequiredNotification(appContext, e.resolutionIntent!!, "google_photos")
            return Result.failure()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Unexpected error during sync.")
            return Result.retry()
        }
    }
    
    companion object {
        const val WORK_NAME = "DailyCloudSyncWorker"
        const val KEY_IS_ONE_TIME_SYNC = "is_one_time_sync"
        const val KEY_SYNCED_COUNT = "key_synced_count"
        private const val TAG = "DailyCloudSyncWorker"
    }
}
