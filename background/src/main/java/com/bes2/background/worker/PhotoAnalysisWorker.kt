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
import com.bes2.ml.BacklightingDetector
import com.bes2.ml.EyeClosedDetector
import com.bes2.ml.FaceEmbedder
import com.bes2.ml.ImageContentClassifier
import com.bes2.ml.ImageQualityAssessor
import com.bes2.ml.MusiqQualityAnalyzer
import com.bes2.ml.NimaQualityAnalyzer
import com.bes2.ml.SemanticSearchEngine
import com.bes2.ml.SmileDetector
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class PhotoAnalysisWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val reviewItemDao: ReviewItemDao,
    private val workManager: WorkManager,
    private val nimaAnalyzer: NimaQualityAnalyzer,
    private val musiqAnalyzer: MusiqQualityAnalyzer,
    private val eyeClosedDetector: EyeClosedDetector,
    private val backlightingDetector: BacklightingDetector,
    private val faceEmbedder: FaceEmbedder,
    private val smileDetector: SmileDetector,
    private val semanticSearchEngine: SemanticSearchEngine,
    private val imageClassifier: ImageContentClassifier 
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "PhotoAnalysisWorker"
        const val BLUR_THRESHOLD = 20.0f
        const val KEY_IS_BACKGROUND_DIET = "is_background_diet" 
        private const val BATCH_SIZE = 50
        private const val NOTIFICATION_ID = 2024
        private const val CHANNEL_ID = "bes2_analysis_channel"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val isBackground = inputData.getBoolean(KEY_IS_BACKGROUND_DIET, false)
        if (isBackground) {
            setForeground(createForegroundInfo())
        }
        
        Timber.tag(WORK_NAME).d("--- PhotoAnalysisWorker Started ---")

        var totalAnalyzed = 0
        
        // Loop until no more NEW items
        while (true) {
            // [LOGIC] Fetch Batch (Limit 50)
            val newItems = reviewItemDao.getNewDietItemsBatch(BATCH_SIZE)
            // Note: For INSTANT items and REJECTED items, we process them in one go or separate logic?
            // Current logic mixed them. Let's simplify.
            // Priority: NEW Diet Items (Bulk)
            // But we should also process REJECTED ones for re-run.
            // Let's assume REJECTED re-run is done on-demand or separate worker.
            // Here we focus on Bulk Analysis of NEW items.
            
            // Also check INSTANT items
            val instantItems = reviewItemDao.getItemsBySourceAndStatus("INSTANT", "NEW")
            val itemsToAnalyze = newItems + instantItems
            
            if (itemsToAnalyze.isEmpty()) {
                break
            }
            
            Timber.tag(WORK_NAME).d("Analyzing batch of ${itemsToAnalyze.size} images.")

            for (imageItem in itemsToAnalyze) {
                var bitmap: Bitmap? = null
                try {
                    bitmap = loadBitmapSimple(imageItem.uri)
                    if (bitmap == null) {
                        reviewItemDao.updateStatusByIds(listOf(imageItem.id), "ERROR_LOAD")
                        continue
                    }

                    // 1. Basic Filters (Fail Fast)
                    var areEyesClosed = eyeClosedDetector.areEyesClosed(bitmap)
                    
                    if (areEyesClosed) {
                        val hasSunglasses = imageClassifier.hasSunglasses(bitmap)
                        if (hasSunglasses) {
                            areEyesClosed = false
                        }
                    }

                    val blurScore = ImageQualityAssessor.calculateBlurScore(bitmap)
                    val isBacklit = backlightingDetector.isBacklit(bitmap)

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
                } catch (e: Exception) {
                    Timber.tag(WORK_NAME).e(e, "Error analyzing image: ${imageItem.uri}")
                    reviewItemDao.updateStatusByIds(listOf(imageItem.id), "ERROR_ANALYSIS")
                } finally {
                    bitmap?.recycle()
                }
            }
            
            // Check if stopped
            if (isStopped) break
        }
        
        if (totalAnalyzed > 0) {
             Timber.tag(WORK_NAME).d("Analysis complete ($totalAnalyzed items). Triggering ClusteringWorker.")
             val clusteringRequest = OneTimeWorkRequestBuilder<ClusteringWorker>().build()
             workManager.enqueueUniqueWork(
                 ClusteringWorker.WORK_NAME,
                 ExistingWorkPolicy.APPEND_OR_REPLACE,
                 clusteringRequest
             )
        }
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
        } catch (e: Exception) { return null }
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
