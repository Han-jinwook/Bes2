package com.bes2.background.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bes2.background.notification.NotificationHelper
import com.bes2.core_common.provider.ResourceProvider
import com.bes2.data.dao.ImageItemDao
import com.bes2.ml.EyeClosedDetector
import com.bes2.ml.FaceEmbedder
import com.bes2.ml.ImageQualityAssessor
import com.bes2.ml.NimaQualityAnalyzer
import com.bes2.ml.SmileDetector
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileNotFoundException
import java.util.concurrent.CancellationException

@HiltWorker
class PhotoAnalysisWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val imageDao: ImageItemDao,
    private val workManager: WorkManager, // Restored for Hilt dependency graph
    private val nimaAnalyzer: NimaQualityAnalyzer,
    private val eyeClosedDetector: EyeClosedDetector,
    private val faceEmbedder: FaceEmbedder, // Restored for Hilt dependency graph
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
        // --- DEBUG LOG ---
        Timber.tag(WORK_NAME).d("--- PhotoAnalysisWorker TRIGGERED by WorkManager ---")
        
        Timber.tag(WORK_NAME).d("Worker started. Analyzing images with PENDING_ANALYSIS status.")

        try {
            // 1. Get images marked for analysis by the ClusteringWorker
            val imagesToAnalyze = imageDao.getImageItemsListByStatus("PENDING_ANALYSIS")
            if (imagesToAnalyze.isEmpty()) {
                Timber.tag(WORK_NAME).d("No images pending analysis.")
                NotificationHelper.dismissAllAppNotifications(appContext)
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

                        val areEyesClosed = eyeClosedDetector.areEyesClosed(bitmap)
                        val blurScore = ImageQualityAssessor.calculateBlurScore(bitmap)

                        if (areEyesClosed || blurScore < BLUR_THRESHOLD) {
                            val rejectedItem = imageItem.copy(
                                status = "STATUS_REJECTED",
                                blurScore = blurScore,
                                areEyesClosed = areEyesClosed
                            )
                            imageDao.updateImageItem(rejectedItem)
                            Timber.tag(WORK_NAME).d("Image #${imageItem.id} REJECTED: eyesClosed=$areEyesClosed, blurScore=$blurScore")
                            continue
                        }
                        
                        // --- Scoring for valid images ---
                        val nimaScoreDistribution = nimaAnalyzer.analyze(bitmap)
                        val smilingProbability = smileDetector.getSmilingProbability(bitmap)
                        val nimaMeanScore = nimaScoreDistribution?.mapIndexed { index, score -> (index + 1) * score }?.sum()

                        val updatedItem = imageItem.copy(
                            status = "ANALYZED",
                            nimaScore = nimaMeanScore,
                            blurScore = blurScore,
                            areEyesClosed = areEyesClosed,
                            smilingProbability = smilingProbability
                        )
                        imageDao.updateImageItem(updatedItem)
                        hasAnalyzedImages = true

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
                if (hasAnalyzedImages) {
                    clustersForReviewCount++
                }
            }
            
            // 3. Notify the user that the process is complete.
            if (clustersForReviewCount > 0) {
                 Timber.tag(WORK_NAME).d("Analysis complete. Notifying user about $clustersForReviewCount new clusters.")
                 NotificationHelper.showReviewNotification(appContext, resourceProvider.notificationIcon, clustersForReviewCount)
            } else {
                Timber.tag(WORK_NAME).d("Analysis complete, but no new clusters need review.")
                NotificationHelper.dismissAllAppNotifications(appContext)
            }

            return@withContext Result.success()
        } catch (e: Exception) {
            Timber.tag(WORK_NAME).e(e, "An error occurred in PhotoAnalysisWorker: ${e.message}")
            return@withContext Result.failure()
        }
    }

    private fun loadBitmap(uri: String): Bitmap {
        return appContext.contentResolver.openInputStream(uri.toUri())?.use {
            BitmapFactory.decodeStream(it)
        } ?: throw FileNotFoundException("ContentResolver returned null stream for $uri")
    }
}
