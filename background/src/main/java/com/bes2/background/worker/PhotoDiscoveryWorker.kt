package com.bes2.background.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bes2.data.dao.ReviewItemDao
import com.bes2.data.dao.TrashItemDao
import com.bes2.data.model.ReviewItemEntity
import com.bes2.data.model.TrashItemEntity
import com.bes2.data.repository.GalleryRepository
import com.bes2.data.repository.SettingsRepository
import com.bes2.ml.ImageCategory
import com.bes2.ml.ImageContentClassifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class PhotoDiscoveryWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val galleryRepository: GalleryRepository,
    private val reviewItemDao: ReviewItemDao,
    private val trashItemDao: TrashItemDao,
    private val workManager: WorkManager,
    private val imageClassifier: ImageContentClassifier,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "PhotoDiscoveryWorker"
        const val KEY_IS_INSTANT_MODE = "is_instant_mode"
        const val KEY_IMAGE_URI = "key_image_uri"
        
        private const val TARGET_DIET_COUNT = 30 
        private const val TARGET_TRASH_COUNT = 30
        private const val BATCH_SIZE = 50
        private const val MAX_SCAN_LIMIT = 5000
        private const val NOTIFICATION_ID = 2023
        private const val CHANNEL_ID = "bes2_analysis_channel"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        //setForeground(createForegroundInfo())

        val isInstantMode = inputData.getBoolean(KEY_IS_INSTANT_MODE, false)
        val sourceType = if (isInstantMode) "INSTANT" else "DIET"
        
        Timber.tag(WORK_NAME).d("--- PhotoDiscoveryWorker Started (Mode: $sourceType) ---")

        if (isInstantMode) {
            // [LOGIC] Instant Mode: Recent photos ONLY (No AI filtering)
            processInstantScan(sourceType)
        } else {
            // [LOGIC] Diet Mode: Past photos ONLY (Strict Time Barrier + AI filtering)
            processGalleryScan(sourceType)
        }
        
        triggerAnalysis()
        return@withContext Result.success()
    }
    
    private suspend fun processInstantScan(sourceType: String) {
        val appStartTime = settingsRepository.getAppStartTime()
        Timber.tag(WORK_NAME).d("Instant Scan Baseline: $appStartTime")
        
        val candidates = galleryRepository.getImagesSince(appStartTime)
        Timber.tag(WORK_NAME).d("Instant Scan: Found ${candidates.size} images since app start.")

        val newDietEntities = mutableListOf<ReviewItemEntity>()
        val newTrashEntities = mutableListOf<TrashItemEntity>()
        
        for (candidate in candidates) {
             if (reviewItemDao.isUriProcessed(candidate.uri) || trashItemDao.isUriProcessed(candidate.uri)) continue
             
             val isScreenshotPath = candidate.filePath.contains("screenshot", ignoreCase = true) || 
                                    candidate.filePath.contains("capture", ignoreCase = true)
             
             if (isScreenshotPath) {
                 newTrashEntities.add(TrashItemEntity(
                     uri = candidate.uri, filePath = candidate.filePath, timestamp = candidate.timestamp, status = "READY"
                 ))
             } else { 
                 // Force to ReviewItem (INSTANT) without AI check
                 newDietEntities.add(ReviewItemEntity(
                     uri = candidate.uri, filePath = candidate.filePath, timestamp = candidate.timestamp,
                     status = "NEW", source_type = sourceType 
                 ))
             }
        }
        
        if (newDietEntities.isNotEmpty()) reviewItemDao.insertAll(newDietEntities)
        if (newTrashEntities.isNotEmpty()) trashItemDao.insertAll(newTrashEntities)
        
        Timber.tag(WORK_NAME).d("Instant Scan Complete: Added ${newDietEntities.size} normal (INSTANT), ${newTrashEntities.size} trash.")
    }

    private suspend fun processGalleryScan(sourceType: String) {
        val appStartTime = settingsRepository.getAppStartTime()
        Timber.tag(WORK_NAME).d("Using appStartTime: $appStartTime")
        var offset = 0
        var scanCount = 0
        
        while (scanCount < MAX_SCAN_LIMIT) {
            val currentDietCount = reviewItemDao.getActiveDietCount()
            val currentTrashCount = trashItemDao.getReadyTrashCount()
            
            if (currentDietCount >= TARGET_DIET_COUNT && currentTrashCount >= TARGET_TRASH_COUNT) {
                Timber.tag(WORK_NAME).d("Diet/Trash Targets Met. Stopping scan.")
                break
            }

            // [MODIFIED] Fetch ALL past images (No filters in repo, filtering happens here)
            // appStartTime is passed but ignored by the repo now, we use it here for strict filtering.
            val candidates = galleryRepository.getPastImages(appStartTime, BATCH_SIZE, offset)
            
            if (candidates.isEmpty()) {
                Timber.tag(WORK_NAME).d("No more past images in gallery to scan.")
                break
            }

            val newDietEntities = mutableListOf<ReviewItemEntity>()
            val newTrashEntities = mutableListOf<TrashItemEntity>()
            
            for (candidate in candidates) {
                 if (reviewItemDao.isUriProcessed(candidate.uri) || trashItemDao.isUriProcessed(candidate.uri)) continue
                 
                 // [STRICT FILTER] Ignore images taken AFTER app start (handled by Instant Scan)
                 if (candidate.timestamp >= appStartTime) continue

                 var isTrash = false
                 // [PATH FILTER] Check for screenshots/captures
                 val isScreenshotPath = candidate.filePath.contains("screenshot", ignoreCase = true) || 
                                        candidate.filePath.contains("capture", ignoreCase = true)
                 
                 if (isScreenshotPath) {
                     isTrash = true
                 } else {
                     // [AI LOGIC] Only for Past photos
                     try {
                         val bitmap = loadBitmap(candidate.uri)
                         if (bitmap != null) {
                             val result = imageClassifier.classify(bitmap)
                             if (result == ImageCategory.DOCUMENT || result == ImageCategory.OBJECT) {
                                 isTrash = true
                             }
                             bitmap.recycle()
                         }
                     } catch (e: Exception) { }
                 }

                 if (isTrash) {
                     if (trashItemDao.getReadyTrashCount() < (TARGET_TRASH_COUNT + 20)) {
                         newTrashEntities.add(TrashItemEntity(
                             uri = candidate.uri, filePath = candidate.filePath, timestamp = candidate.timestamp, status = "READY"
                         ))
                     }
                 } else { 
                     if (reviewItemDao.getActiveDietCount() < TARGET_DIET_COUNT) {
                         newDietEntities.add(ReviewItemEntity(
                             uri = candidate.uri, filePath = candidate.filePath, timestamp = candidate.timestamp,
                             status = "NEW", source_type = sourceType 
                         ))
                     }
                 }
                 scanCount++
            }
            
            if (newDietEntities.isNotEmpty()) reviewItemDao.insertAll(newDietEntities)
            if (newTrashEntities.isNotEmpty()) trashItemDao.insertAll(newTrashEntities)
            
            offset += BATCH_SIZE
        }
        
        Timber.tag(WORK_NAME).d("Gallery Scan Loop Finished. Scanned: $scanCount items.")
    }
    
    private suspend fun triggerAnalysis() {
        val newInstantCount = reviewItemDao.getItemsBySourceAndStatus("INSTANT", "NEW").size
        val newDietCount = reviewItemDao.getNewDietItems().size
        
        if (newInstantCount > 0 || newDietCount > 0) {
            val analysisWorkRequest = OneTimeWorkRequestBuilder<PhotoAnalysisWorker>().build()
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

    private fun createForegroundInfo(): ForegroundInfo {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bes2 Photo Analysis",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setContentTitle("사진을 감지하고 있습니다")
            .setContentText("Bes2가 사진을 정리중입니다...")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= 34) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }
}
