package com.bes2.background.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bes2.background.notification.NotificationHelper
import com.bes2.core_common.provider.ResourceProvider
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.ImageItemEntity
import com.bes2.ml.EyeClosedDetector
import com.bes2.ml.FaceEmbedder
import com.bes2.ml.ImagePhashGenerator
import com.bes2.ml.ImageQualityAssessor
import com.bes2.ml.NimaQualityAnalyzer
import com.bes2.ml.SmileDetector
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.util.concurrent.CancellationException

@HiltWorker
class PhotoAnalysisWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val imageDao: ImageItemDao,
    private val workManager: WorkManager,
    private val nimaAnalyzer: NimaQualityAnalyzer,
    private val eyeClosedDetector: EyeClosedDetector,
    private val faceEmbedder: FaceEmbedder,
    private val smileDetector: SmileDetector,
    private val resourceProvider: ResourceProvider
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "PhotoAnalysisWorker"
        const val BLUR_THRESHOLD = 30.0f
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
        Timber.tag(WORK_NAME).d("Worker started.")

        try {
            val imagesToAnalyze = imageDao.getImageItemsListByStatus("NEW")
            if (imagesToAnalyze.isEmpty()) {
                Timber.tag(WORK_NAME).d("No new images to analyze. Dismissing notification and stopping.")
                NotificationHelper.dismissAllAppNotifications(appContext)
                return@withContext Result.success()
            }

            Timber.tag(WORK_NAME).d("Found ${imagesToAnalyze.size} images to analyze.")

            for (imageItem in imagesToAnalyze) {
                var bitmap: Bitmap? = null
                try {
                    appContext.contentResolver.openInputStream(imageItem.uri.toUri())?.use {
                        bitmap = BitmapFactory.decodeStream(it)
                    } ?: throw FileNotFoundException("ContentResolver returned null stream for ${imageItem.uri}")

                    // Perform all analysis first
                    val areEyesClosed = eyeClosedDetector.areEyesClosed(bitmap!!)
                    val blurScore = ImageQualityAssessor.calculateBlurScore(bitmap!!)
                    val faceEmbedding = faceEmbedder.getFaceEmbedding(bitmap!!)
                    val pHash = ImagePhashGenerator.generatePhash(bitmap!!)
                    val nimaScoreDistribution = nimaAnalyzer.analyze(bitmap!!)
                    val smilingProbability = smileDetector.getSmilingProbability(bitmap!!)

                    val nimaMeanScore = nimaScoreDistribution?.mapIndexed { index, score -> (index + 1) * score }?.sum()
                    val faceEmbeddingBytes = faceEmbedding?.let { floatArray ->
                        val byteBuffer = ByteBuffer.allocate(floatArray.size * 4)
                        floatArray.forEach { byteBuffer.putFloat(it) }
                        byteBuffer.array()
                    }

                    // DEFINITIVE FIX: NIMA failure should not cause a photo to be rejected.
                    val finalStatus = if (areEyesClosed || blurScore < BLUR_THRESHOLD) {
                        "STATUS_REJECTED"
                    } else {
                        "ANALYZED"
                    }

                    // Finally, update the item with all collected data
                    val updatedItem = imageItem.copy(
                        status = finalStatus,
                        pHash = pHash,
                        faceEmbedding = faceEmbeddingBytes,
                        nimaScore = nimaMeanScore, // This will be null if NIMA failed, which is ok
                        blurScore = blurScore,
                        areEyesClosed = areEyesClosed,
                        smilingProbability = smilingProbability
                    )
                    imageDao.updateImageItem(updatedItem)

                } catch (e: FileNotFoundException) {
                    Timber.tag(WORK_NAME).w(e, "File not found for image: ${imageItem.uri}. Marking as ERROR_DELETED.")
                    imageDao.updateImageItem(imageItem.copy(status = "ERROR_DELETED"))
                } catch (e: CancellationException) {
                    Timber.tag(WORK_NAME).w(e, "Job was cancelled for image #${imageItem.id}.")
                    throw e
                } catch (e: Exception) {
                    Timber.tag(WORK_NAME).e(e, "Error processing image: ${imageItem.uri}")
                    imageDao.updateImageItem(imageItem.copy(status = "ERROR_ANALYSIS"))
                } finally {
                    bitmap?.recycle()
                }
            }

            Timber.tag(WORK_NAME).d("Analysis phase completed. Enqueuing ClusteringWorker.")
            val clusteringWorkRequest = OneTimeWorkRequestBuilder<ClusteringWorker>().build()
            workManager.enqueueUniqueWork(
                ClusteringWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                clusteringWorkRequest
            )

            return@withContext Result.success()
        } catch (e: Exception) {
            Timber.tag(WORK_NAME).e(e, "Error in PhotoAnalysisWorker: ${e.message}")
            return@withContext Result.failure()
        }
    }
}
