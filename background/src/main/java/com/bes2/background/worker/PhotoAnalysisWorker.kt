package com.bes2.background.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bes2.background.notification.NotificationHelper
import com.bes2.core_common.provider.ResourceProvider
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.repository.SettingsRepository
import com.bes2.ml.BacklightingDetector
import com.bes2.ml.EyeClosedDetector
import com.bes2.ml.FaceEmbedder
import com.bes2.ml.ImageCategory
import com.bes2.ml.ImageContentClassifier
import com.bes2.ml.ImageQualityAssessor
import com.bes2.ml.MusiqQualityAnalyzer
import com.bes2.ml.NimaQualityAnalyzer
import com.bes2.ml.SemanticSearchEngine
import com.bes2.ml.SmileDetector
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileNotFoundException
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit

@HiltWorker
class PhotoAnalysisWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val imageDao: ImageItemDao,
    private val workManager: WorkManager,
    private val nimaAnalyzer: NimaQualityAnalyzer,
    private val musiqAnalyzer: MusiqQualityAnalyzer,
    private val eyeClosedDetector: EyeClosedDetector,
    private val backlightingDetector: BacklightingDetector,
    private val faceEmbedder: FaceEmbedder,
    private val smileDetector: SmileDetector,
    private val imageClassifier: ImageContentClassifier,
    private val semanticSearchEngine: SemanticSearchEngine,
    private val resourceProvider: ResourceProvider,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "PhotoAnalysisWorker"
        const val BLUR_THRESHOLD = 30.0f
        const val KEY_IS_BACKGROUND_DIET = "is_background_diet"
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = NotificationHelper.createForegroundNotification(appContext, resourceProvider.notificationIcon)
        val notificationId = NotificationHelper.APP_STATUS_NOTIFICATION_ID
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Timber.tag(WORK_NAME).d("--- PhotoAnalysisWorker TRIGGERED by WorkManager ---")
        val isBackgroundDiet = inputData.getBoolean(KEY_IS_BACKGROUND_DIET, false)
        Timber.tag(WORK_NAME).d("Worker started. Analyzing images with PENDING_ANALYSIS status. (isBackgroundDiet=$isBackgroundDiet)")

        try {
            val imagesToAnalyze = imageDao.getImageItemsListByStatus("PENDING_ANALYSIS")
            if (imagesToAnalyze.isEmpty()) {
                Timber.tag(WORK_NAME).d("No images pending analysis.")
                if (!isBackgroundDiet) {
                    NotificationHelper.dismissAllAppNotifications(appContext)
                }
                return@withContext Result.success()
            }
            Timber.tag(WORK_NAME).d("Found ${imagesToAnalyze.size} images to analyze.")
            
            val imagesByCluster = imagesToAnalyze.groupBy { it.clusterId }
            var clustersForReviewCount = 0

            for ((clusterId, imagesInCluster) in imagesByCluster) {
                if (clusterId == null) continue

                var hasAnalyzedImages = false
                for (imageItem in imagesInCluster) {
                    var bitmap: Bitmap? = null
                    try {
                        bitmap = loadBitmap(imageItem.uri)

                        val categoryEnum = imageClassifier.classify(bitmap)
                        val categoryString = categoryEnum.name
                        
                        if (categoryEnum == ImageCategory.DOCUMENT) {
                            val targetStatus = if (isBackgroundDiet) "READY_TO_CLEAN" else "ANALYZED"
                            val updatedItem = imageItem.copy(
                                status = targetStatus,
                                category = categoryString,
                                nimaScore = null, musiqScore = null, blurScore = null,
                                areEyesClosed = null, smilingProbability = null
                            )
                            imageDao.updateImageItem(updatedItem)
                            hasAnalyzedImages = true 
                            continue
                        }

                        val embedding = semanticSearchEngine.encodeImage(bitmap)
                        val embeddingBytes = embedding?.let { floatArray ->
                             val buffer = java.nio.ByteBuffer.allocate(floatArray.size * 4)
                             buffer.asFloatBuffer().put(floatArray)
                             buffer.array()
                        }
                        
                        // [FIXED] Use correct function name: getFaceEmbedding
                        val faceEmbedding = faceEmbedder.getFaceEmbedding(bitmap)
                        val faceEmbeddingBytes = faceEmbedding?.let { floatArray ->
                            val buffer = java.nio.ByteBuffer.allocate(floatArray.size * 4)
                            buffer.asFloatBuffer().put(floatArray)
                            buffer.array()
                        }

                        val areEyesClosed = eyeClosedDetector.areEyesClosed(bitmap)
                        val blurScore = ImageQualityAssessor.calculateBlurScore(bitmap)
                        val isBacklit = backlightingDetector.isBacklit(bitmap)

                        if (areEyesClosed || blurScore < BLUR_THRESHOLD || isBacklit) {
                            val rejectedItem = imageItem.copy(
                                status = "STATUS_REJECTED",
                                category = categoryString,
                                blurScore = blurScore,
                                areEyesClosed = areEyesClosed,
                                exposureScore = if (isBacklit) -1.0f else 0.0f,
                                embedding = embeddingBytes,
                                faceEmbedding = faceEmbeddingBytes
                            )
                            imageDao.updateImageItem(rejectedItem)
                            continue
                        }
                        
                        val nimaScoreDistribution = nimaAnalyzer.analyze(bitmap)
                        val nimaMeanScore = nimaScoreDistribution?.mapIndexed { index, score -> (index + 1) * score }?.sum()
                        val musiqScore = musiqAnalyzer.analyze(bitmap)
                        val smilingProbability = smileDetector.getSmilingProbability(bitmap)
                        val targetStatus = if (isBackgroundDiet) "READY_TO_CLEAN" else "ANALYZED"

                        val updatedItem = imageItem.copy(
                            status = targetStatus,
                            category = categoryString,
                            nimaScore = nimaMeanScore,
                            musiqScore = musiqScore,
                            blurScore = blurScore,
                            areEyesClosed = areEyesClosed,
                            smilingProbability = smilingProbability,
                            embedding = embeddingBytes,
                            faceEmbedding = faceEmbeddingBytes
                        )
                        imageDao.updateImageItem(updatedItem)
                        hasAnalyzedImages = true

                    } catch (e: Exception) {
                        Timber.tag(WORK_NAME).e(e, "Error processing image: ${imageItem.uri}")
                        imageDao.updateImageItem(imageItem.copy(status = "ERROR_ANALYSIS"))
                    } finally {
                        bitmap?.recycle()
                    }
                }
                if (hasAnalyzedImages) {
                    clustersForReviewCount++
                }
            }
            
            if (clustersForReviewCount > 0) {
                 if (!isBackgroundDiet) {
                     NotificationHelper.showReviewNotification(appContext, resourceProvider.notificationIcon, clustersForReviewCount, imagesToAnalyze.size)
                     schedulePostAnalysisSync()
                 }
            } else {
                if (!isBackgroundDiet) {
                    NotificationHelper.dismissAllAppNotifications(appContext)
                }
            }

            return@withContext Result.success()
        } catch (e: Exception) {
            Timber.tag(WORK_NAME).e(e, "An error occurred in PhotoAnalysisWorker: ${e.message}")
            return@withContext Result.failure()
        }
    }

    private suspend fun schedulePostAnalysisSync() {
        val settings = settingsRepository.storedSettings.first()
        if (settings.syncOption == "NONE" || settings.syncOption == "DAILY") return
        val delayInMillis = if (settings.syncOption == "DELAYED") {
            TimeUnit.HOURS.toMillis(settings.syncDelayHours.toLong()) + TimeUnit.MINUTES.toMillis(settings.syncDelayMinutes.toLong())
        } else {
            0L
        }
        val constraints = if (settings.uploadOnWifiOnly) {
            Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()
        } else {
            Constraints.NONE
        }
        val inputData = Data.Builder().putBoolean(DailyCloudSyncWorker.KEY_IS_ONE_TIME_SYNC, true).build()
        val syncWorkRequest = OneTimeWorkRequestBuilder<DailyCloudSyncWorker>()
            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()
        workManager.enqueue(syncWorkRequest)
    }

    private fun loadBitmap(uri: String): Bitmap {
        val contentResolver = appContext.contentResolver
        val uriObject = uri.toUri()
        var inputStream = contentResolver.openInputStream(uriObject) ?: throw FileNotFoundException("ContentResolver returned null stream for $uri")
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (originalBitmap == null) throw FileNotFoundException("Failed to decode bitmap from $uri")
        inputStream = contentResolver.openInputStream(uriObject) ?: throw FileNotFoundException("ContentResolver returned null stream for $uri")
        val exifInterface = ExifInterface(inputStream)
        val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        inputStream.close()
        return rotateBitmap(originalBitmap, orientation)
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }
        return try {
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) bitmap.recycle()
            rotated
        } catch (e: OutOfMemoryError) {
            bitmap
        }
    }
}
