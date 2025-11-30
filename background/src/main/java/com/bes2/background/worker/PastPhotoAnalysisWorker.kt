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
import com.bes2.data.dao.ReviewItemDao
import com.bes2.data.dao.TrashItemDao
import com.bes2.data.model.ReviewItemEntity
import com.bes2.data.model.TrashItemEntity
import com.bes2.data.repository.GalleryRepository
import com.bes2.ml.ImageCategory
import com.bes2.ml.ImageContentClassifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class PastPhotoAnalysisWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val galleryRepository: GalleryRepository,
    private val reviewItemDao: ReviewItemDao,
    private val trashItemDao: TrashItemDao,
    private val workManager: WorkManager,
    private val imageClassifier: ImageContentClassifier
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "PastPhotoAnalysisWorker"
        private const val TARGET_DIET_COUNT = 30 
        private const val TARGET_TRASH_COUNT = 30
        private const val BATCH_SIZE = 50
        private const val MAX_SCAN_LIMIT = 5000
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Timber.tag(WORK_NAME).d("--- DispatcherWorker Started ---")

        // 1. Check current status
        val currentDietCount = reviewItemDao.getNewDietItems().size // Only count NEW for now
        val currentTrashCount = trashItemDao.getReadyTrashCount()
        
        // 2. Determine needs
        val neededDietCount = (TARGET_DIET_COUNT - currentDietCount).coerceAtLeast(0)
        val neededTrashCount = (TARGET_TRASH_COUNT - currentTrashCount).coerceAtLeast(0)
        
        if (neededDietCount == 0 && neededTrashCount == 0) {
            triggerAnalysis()
            return@withContext Result.success()
        }

        var processedDietCount = 0
        var processedTrashCount = 0
        var offset = 0
        var scanCount = 0
        
        // 3. Scan Loop
        while ((processedDietCount < neededDietCount || processedTrashCount < neededTrashCount) && 
               scanCount < MAX_SCAN_LIMIT) {
                   
            val candidates = galleryRepository.getRecentImages(BATCH_SIZE, offset)
            if (candidates.isEmpty()) break

            val newDietEntities = mutableListOf<ReviewItemEntity>()
            val newTrashEntities = mutableListOf<TrashItemEntity>()
            
            for (candidate in candidates) {
                 // Check if already processed in EITHER db
                 val isProcessedInReview = reviewItemDao.isUriProcessed(candidate.uri)
                 val isProcessedInTrash = trashItemDao.isUriProcessed(candidate.uri)
                 
                 if (isProcessedInReview || isProcessedInTrash) continue
                 
                 // Classify
                 var isTrash = false
                 try {
                     val bitmap = loadBitmap(candidate.uri)
                     if (bitmap != null) {
                         val result = imageClassifier.classify(bitmap)
                         if (result == ImageCategory.DOCUMENT) isTrash = true
                         bitmap.recycle()
                     }
                 } catch (e: Exception) { }

                 // Dispatch
                 if (isTrash) {
                     if (processedTrashCount < neededTrashCount + 20) {
                         newTrashEntities.add(TrashItemEntity(
                             uri = candidate.uri,
                             filePath = candidate.filePath,
                             timestamp = candidate.timestamp,
                             status = "READY"
                         ))
                         processedTrashCount++
                     }
                 } else {
                     if (processedDietCount < neededDietCount) {
                         newDietEntities.add(ReviewItemEntity(
                             uri = candidate.uri,
                             filePath = candidate.filePath,
                             timestamp = candidate.timestamp,
                             status = "NEW",
                             source_type = "DIET" // Explicitly mark as DIET
                         ))
                         processedDietCount++
                     }
                 }
                 scanCount++
            }
            
            if (newDietEntities.isNotEmpty()) reviewItemDao.insertAll(newDietEntities)
            if (newTrashEntities.isNotEmpty()) trashItemDao.insertAll(newTrashEntities)
            
            offset += BATCH_SIZE
            
            if (processedDietCount >= neededDietCount && processedTrashCount >= neededTrashCount) break
        }
        
        triggerAnalysis()
        
        return@withContext Result.success()
    }
    
    private suspend fun triggerAnalysis() {
        val newDietItems = reviewItemDao.getNewDietItems()
        if (newDietItems.isNotEmpty()) {
            val analysisWorkRequest = OneTimeWorkRequestBuilder<PhotoAnalysisWorker>()
                .setInputData(Data.Builder().putBoolean(PhotoAnalysisWorker.KEY_IS_BACKGROUND_DIET, true).build())
                .build()
            workManager.enqueueUniqueWork(
                PhotoAnalysisWorker.WORK_NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                analysisWorkRequest
            )
        }
    }

    private fun loadBitmap(uri: String): Bitmap? {
        return try {
            appContext.contentResolver.openInputStream(uri.toUri())?.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) { null }
    }
}
