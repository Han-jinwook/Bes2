package com.bes2.background.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentUris
import android.content.Context
import android.content.pm.ServiceInfo
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
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
import com.bes2.background.R
import com.bes2.background.notification.NotificationHelper
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
import kotlinx.coroutines.CancellationException
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
        
        private const val INSERT_BATCH_SIZE = 100 
        private const val NOTIFICATION_ID = 2023
        private const val CHANNEL_ID = "bes2_analysis_channel"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val isInstantMode = inputData.getBoolean(KEY_IS_INSTANT_MODE, false)
        val sourceType = if (isInstantMode) "INSTANT" else "DIET"
        
        if (!isInstantMode) {
            setForeground(createForegroundInfo())
            settingsRepository.resetAnalysisProgress()
        }
        
        Timber.tag(WORK_NAME).d("--- PhotoDiscoveryWorker Started (Mode: $sourceType) ---")

        // [MODIFIED] Removed redundant initializer
        val newTrashCount = if (isInstantMode) {
            processInstantScan(sourceType)
        } else {
            processGalleryScanStream(sourceType)
        }
        
        // Send trash notification if new items were found
        if (newTrashCount > 0 && !isInstantMode) {
            if (settingsRepository.shouldShowNotification("TRASH")) {
                NotificationHelper.showReviewNotification(
                    appContext,
                    R.drawable.ic_notification,
                    0, // clusterCount is not relevant for trash
                    newTrashCount,
                    "TRASH"
                )
                settingsRepository.updateLastNotificationTime("TRASH")
            }
        }
        
        if (!isStopped) {
            triggerAnalysis()
        }
        return@withContext Result.success()
    }
    
    private suspend fun processInstantScan(sourceType: String): Int {
        val appStartTime = settingsRepository.getAppStartTime()
        val candidates = galleryRepository.getImagesSince(appStartTime)

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
                 newDietEntities.add(ReviewItemEntity(
                     uri = candidate.uri, filePath = candidate.filePath, timestamp = candidate.timestamp,
                     status = "NEW", source_type = sourceType 
                 ))
             }
        }
        
        if (newDietEntities.isNotEmpty()) reviewItemDao.insertAll(newDietEntities)
        if (newTrashEntities.isNotEmpty()) trashItemDao.insertAll(newTrashEntities)
        
        return newTrashEntities.size
    }

    private suspend fun processGalleryScanStream(sourceType: String): Int {
        Timber.tag(WORK_NAME).d("Starting Full Gallery Scan Stream...")
        
        val cursor = galleryRepository.getAllImagesCursor()
        if (cursor == null) {
            Timber.tag(WORK_NAME).e("Failed to open gallery cursor.")
            return 0
        }
        
        var scanCountTotal = 0
        var newTrashFound = 0
        val newDietBuffer = mutableListOf<ReviewItemEntity>()
        val newTrashBuffer = mutableListOf<TrashItemEntity>()
        
        try {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            
            while (cursor.moveToNext()) {
                if (isStopped) break
                
                val id = cursor.getLong(idColumn)
                val filePath = cursor.getString(dataColumn)
                val timestamp = cursor.getLong(dateColumn)
                val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id).toString()
                
                scanCountTotal++
                
                if (reviewItemDao.isUriProcessed(contentUri) || trashItemDao.isUriProcessed(contentUri)) {
                    continue
                }
                
                var isTrash = false
                val isScreenshotPath = filePath.contains("screenshot", ignoreCase = true) || 
                                       filePath.contains("capture", ignoreCase = true)
                
                if (isScreenshotPath) {
                    isTrash = true
                } else {
                    try {
                        val bitmap = loadBitmap(contentUri)
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
                    newTrashBuffer.add(TrashItemEntity(
                        uri = contentUri, filePath = filePath, timestamp = timestamp, status = "READY"
                    ))
                } else { 
                    newDietBuffer.add(ReviewItemEntity(
                        uri = contentUri, filePath = filePath, timestamp = timestamp,
                        status = "NEW", source_type = sourceType 
                    ))
                }
                
                if (newDietBuffer.size >= INSERT_BATCH_SIZE) {
                    reviewItemDao.insertAll(newDietBuffer)
                    newDietBuffer.clear()
                }
                if (newTrashBuffer.size >= INSERT_BATCH_SIZE) {
                    newTrashFound += newTrashBuffer.size
                    trashItemDao.insertAll(newTrashBuffer)
                    newTrashBuffer.clear()
                }
            }
            
            if (newDietBuffer.isNotEmpty()) reviewItemDao.insertAll(newDietBuffer)
            if (newTrashBuffer.isNotEmpty()) {
                newTrashFound += newTrashBuffer.size
                trashItemDao.insertAll(newTrashBuffer)
            }
            
            settingsRepository.setTotalScanCount(scanCountTotal)
            
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Timber.tag(WORK_NAME).e(e, "Error during gallery scan stream")
        } finally {
            cursor.close()
        }
        
        Timber.tag(WORK_NAME).d("Full Scan Complete. Scanned: $scanCountTotal items. New trash: $newTrashFound")
        return newTrashFound
    }
    
    private suspend fun triggerAnalysis() {
        val analysisWorkRequest = OneTimeWorkRequestBuilder<PhotoAnalysisWorker>().build()
        workManager.enqueueUniqueWork(
            PhotoAnalysisWorker.WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE, 
            analysisWorkRequest
        )
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
            .setContentTitle("갤러리를 분석하고 있습니다")
            .setContentText("사진을 정리할 준비를 하고 있어요...")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .setProgress(0, 0, true) 
            .build()

        return if (Build.VERSION.SDK_INT >= 34) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }
}
