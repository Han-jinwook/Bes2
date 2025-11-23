package com.bes2.background.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.ImageItemEntity
import com.bes2.data.repository.GalleryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileNotFoundException

@HiltWorker
class PastPhotoAnalysisWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val galleryRepository: GalleryRepository,
    private val imageDao: ImageItemDao,
    private val workManager: WorkManager
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "PastPhotoAnalysisWorker"
        const val STATUS_READY_TO_CLEAN = "READY_TO_CLEAN"
        private const val TARGET_PREPARED_COUNT = 100
        private const val BATCH_SIZE = 50
        private const val MAX_SCAN_LIMIT = 5000
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Timber.tag(WORK_NAME).d("--- PastPhotoAnalysisWorker Started (Pipeline Mode) ---")

        // 1. Check if we need more photos
        val currentReadyCount = imageDao.countImagesByStatus(STATUS_READY_TO_CLEAN)
        val currentNewCount = imageDao.countImagesByStatus("NEW")
        val currentPendingCount = imageDao.countImagesByStatus("PENDING_ANALYSIS")
        
        val totalProcessing = currentReadyCount + currentNewCount + currentPendingCount
        
        if (totalProcessing >= TARGET_PREPARED_COUNT) {
             Timber.tag(WORK_NAME).d("Enough images in pipeline ($totalProcessing). Stopping.")
             return@withContext Result.success()
        }

        val neededCount = TARGET_PREPARED_COUNT - totalProcessing
        Timber.tag(WORK_NAME).d("Fetching $neededCount more images to pipeline.")

        var processedCount = 0
        var offset = 0
        
        // 2. Fetch images and save as NEW
        while (processedCount < neededCount && offset < MAX_SCAN_LIMIT) {
            val candidates = galleryRepository.getRecentImages(BATCH_SIZE, offset)
            if (candidates.isEmpty()) {
                Timber.tag(WORK_NAME).d("No more images in gallery.")
                break
            }

            val newEntities = mutableListOf<ImageItemEntity>()
            
            for (candidate in candidates) {
                 if (processedCount >= neededCount) break
                 
                 if (imageDao.isUriProcessed(candidate.uri)) {
                     continue
                 }

                 val entity = ImageItemEntity(
                     uri = candidate.uri,
                     filePath = candidate.filePath,
                     timestamp = candidate.timestamp,
                     status = "NEW", // Important: Save as NEW to trigger ClusteringWorker
                     pHash = null,
                     nimaScore = null,
                     blurScore = null,
                     exposureScore = null,
                     areEyesClosed = null,
                     smilingProbability = null,
                     faceEmbedding = null,
                     clusterId = null,
                     isUploaded = false
                 )
                 newEntities.add(entity)
                 processedCount++
            }
            
            if (newEntities.isNotEmpty()) {
                imageDao.insertImageItems(newEntities)
                Timber.tag(WORK_NAME).d("Saved ${newEntities.size} images as NEW.")
            }
            
            offset += BATCH_SIZE
        }
        
        // 3. Trigger ClusteringWorker if we added any new images
        if (processedCount > 0) {
            Timber.tag(WORK_NAME).d("Triggering ClusteringWorker for $processedCount new images.")
            
            val inputData = Data.Builder()
                .putBoolean(ClusteringWorker.KEY_IS_BACKGROUND_DIET, true)
                .build()
                
            val clusteringWorkRequest = OneTimeWorkRequestBuilder<ClusteringWorker>()
                .setInputData(inputData)
                .build()
                
            workManager.enqueueUniqueWork(
                ClusteringWorker.WORK_NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE, // Append to existing work or replace
                clusteringWorkRequest
            )
        }
        
        return@withContext Result.success()
    }
}
