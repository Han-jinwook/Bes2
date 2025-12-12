package com.bes2.background.worker

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
<<<<<<< HEAD
import androidx.work.ExistingWorkPolicy
=======
>>>>>>> cf90f718a77eba7f1db63ec598fbb17274d3c886
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bes2.data.dao.ReviewItemDao
import com.bes2.data.repository.SettingsRepository
import com.bes2.ml.BacklightingDetector
import com.bes2.ml.EyeClosedDetector
import com.bes2.ml.FaceEmbedder
import com.bes2.ml.ImageContentClassifier
import com.bes2.ml.ImagePhashGenerator
import com.bes2.ml.ImageQualityAssessor
import com.bes2.ml.MusiqQualityAnalyzer
import com.bes2.ml.NimaQualityAnalyzer
import com.bes2.ml.SemanticSearchEngine
import com.bes2.ml.SmileDetector
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class PhotoAnalysisWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val workManager: WorkManager,
    private val reviewItemDao: ReviewItemDao,
    private val nimaAnalyzer: NimaQualityAnalyzer,
    private val musiqAnalyzer: MusiqQualityAnalyzer,
    private val eyeClosedDetector: EyeClosedDetector,
    private val backlightingDetector: BacklightingDetector,
    private val faceEmbedder: FaceEmbedder,
    private val smileDetector: SmileDetector,
    private val semanticSearchEngine: SemanticSearchEngine,
    private val imageClassifier: ImageContentClassifier,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "PhotoAnalysisWorker"
        const val BLUR_THRESHOLD = 20.0f
<<<<<<< HEAD
        const val KEY_IS_BACKGROUND_DIET = "is_background_diet"
=======
        const val KEY_IS_BACKGROUND_DIET = "is_background_diet" 
>>>>>>> cf90f718a77eba7f1db63ec598fbb17274d3c886
        const val KEY_IS_INSTANT_PRIORITY = "is_instant_priority"
        private const val BATCH_SIZE = 50
        private const val NOTIFICATION_ID = 2024
        private const val CHANNEL_ID = "bes2_analysis_channel"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val isBackground = inputData.getBoolean(KEY_IS_BACKGROUND_DIET, false)
        val isInstantPriority = inputData.getBoolean(KEY_IS_INSTANT_PRIORITY, false)

        if (isBackground) {
            setForeground(createForegroundInfo())
        }
        
        Timber.tag(WORK_NAME).d("--- PhotoAnalysisWorker Started (Priority: $isInstantPriority) ---")

        var totalAnalyzed = 0
        var newItemsFound = false
        
        try {
            while (true) {
                val instantItems = reviewItemDao.getItemsBySourceAndStatus("INSTANT", "NEW")
                
                val newItems = if (isInstantPriority) {
                    emptyList()
                } else {
                    reviewItemDao.getNewDietItemsBatch(BATCH_SIZE)
                }
                
                val itemsToAnalyze = instantItems + newItems
                
                if (itemsToAnalyze.isEmpty()) {
<<<<<<< HEAD
                    Timber.tag(WORK_NAME).d("No new items to analyze.")
                    break
                }
                
                newItemsFound = true
=======
                    break
                }
                
>>>>>>> cf90f718a77eba7f1db63ec598fbb17274d3c886
                Timber.tag(WORK_NAME).d("Analyzing batch of ${itemsToAnalyze.size} images.")

                for (imageItem in itemsToAnalyze) {
                    var bitmap: Bitmap? = null
                    try {
                        bitmap = loadBitmapSimple(imageItem.uri)
                        if (bitmap == null) {
<<<<<<< HEAD
                            Timber.tag(WORK_NAME).e("Failed to load bitmap for URI: ${imageItem.uri}. Skipping analysis.")
=======
>>>>>>> cf90f718a77eba7f1db63ec598fbb17274d3c886
                            reviewItemDao.updateStatusByIds(listOf(imageItem.id), "ERROR_LOAD")
                            continue
                        }

                        var areEyesClosed = eyeClosedDetector.areEyesClosed(bitmap)
                        
                        if (areEyesClosed) {
                            val hasSunglasses = imageClassifier.hasSunglasses(bitmap)
                            if (hasSunglasses) {
                                areEyesClosed = false
                            }
                        }

                        val blurScore = ImageQualityAssessor.calculateBlurScore(bitmap)
                        val isBacklit = backlightingDetector.isBacklit(bitmap)
<<<<<<< HEAD
=======

>>>>>>> cf90f718a77eba7f1db63ec598fbb17274d3c886
                        val isBlurry = blurScore < BLUR_THRESHOLD
                        
                        val nextStatus = if (areEyesClosed || isBlurry) {
                            "STATUS_REJECTED"
                        } else {
                            "ANALYZED"
                        }
                        
                        var nimaMeanScore: Double? = null
                        var musiqScore: Float? = null
                        var smilingProbability: Float? = null
                        var embedding: ByteArray? = null
                        var faceEmbedding: ByteArray? = null
<<<<<<< HEAD
                        val pHash = ImagePhashGenerator.generatePhash(bitmap)
=======
>>>>>>> cf90f718a77eba7f1db63ec598fbb17274d3c886

                        if (nextStatus == "ANALYZED") {
                            embedding = semanticSearchEngine.encodeImage(bitmap)?.let { floatArrayToByteArray(it) }
                            faceEmbedding = faceEmbedder.getFaceEmbedding(bitmap)?.let { floatArrayToByteArray(it) }
                            
                            val nimaScoreDistribution = nimaAnalyzer.analyze(bitmap)
                            nimaMeanScore = nimaScoreDistribution?.mapIndexed { index, score -> (index + 1) * score }?.sum()?.toDouble()
                            musiqScore = musiqAnalyzer.analyze(bitmap)
                            smilingProbability = smileDetector.getSmilingProbability(bitmap)
                        } 

                        val updatedItem = imageItem.copy(
                            status = nextStatus,
<<<<<<< HEAD
                            pHash = pHash,
=======
>>>>>>> cf90f718a77eba7f1db63ec598fbb17274d3c886
                            nimaScore = nimaMeanScore,
                            musiqScore = musiqScore,
                            blurScore = blurScore,
                            areEyesClosed = areEyesClosed,
                            smilingProbability = smilingProbability,
                            embedding = embedding,
                            faceEmbedding = faceEmbedding,
                            exposureScore = if (isBacklit) -1.0f else 0.0f,
                            cluster_id = null 
                        )
                        
                        reviewItemDao.update(updatedItem)
                        totalAnalyzed++
                        
                        if (!isInstantPriority) {
                            settingsRepository.updateCurrentAnalysisProgress(totalAnalyzed)
                        }
                        
                    } catch (e: Exception) {
<<<<<<< HEAD
=======
                        // [FIXED] Rethrow CancellationException to allow worker to stop gracefully
>>>>>>> cf90f718a77eba7f1db63ec598fbb17274d3c886
                        if (e is CancellationException) throw e
                        Timber.tag(WORK_NAME).e(e, "Error analyzing image: ${imageItem.uri}")
                        reviewItemDao.updateStatusByIds(listOf(imageItem.id), "ERROR_ANALYSIS")
                    } finally {
                        bitmap?.recycle()
                    }
                }
                
                if (isStopped) break
            }
        } catch (e: CancellationException) {
            Timber.tag(WORK_NAME).w("Worker cancelled gracefully.")
<<<<<<< HEAD
            throw e
        }
        
        // If we analyzed new items, explicitly trigger the clustering worker
        if (newItemsFound) {
            Timber.tag(WORK_NAME).d("Analysis finished. Triggering ClusteringWorker explicitly.")
            val clusteringRequest = OneTimeWorkRequestBuilder<ClusteringWorker>().build()
            workManager
                .beginWith(clusteringRequest) // Use beginWith instead of chaining
                .enqueue()
        }

=======
            // Don't return failure, just let it end or rethrow if needed by WorkManager
            throw e
        }
        
>>>>>>> cf90f718a77eba7f1db63ec598fbb17274d3c886
        return@withContext Result.success()
    }

    private fun floatArrayToByteArray(floatArray: FloatArray): ByteArray {
        val buffer = java.nio.ByteBuffer.allocate(floatArray.size * 4)
        buffer.asFloatBuffer().put(floatArray)
        return buffer.array()
    }

    private fun loadBitmapSimple(uri: String): Bitmap? {
        return try {
            appContext.contentResolver.openInputStream(uri.toUri())?.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            Timber.tag(WORK_NAME).e(e, "Exception while loading bitmap for URI: $uri")
            return null
        }
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
            .setContentTitle("사진을 분석하고 있습니다")
            .setContentText("AI가 베스트 사진을 고르고 있어요...")
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
