package com.bes2.background.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bes2.background.R
import com.bes2.background.notification.NotificationHelper
import com.bes2.data.dao.ReviewItemDao
import com.bes2.data.model.ReviewItemEntity
import com.bes2.data.repository.GalleryRepository
import com.bes2.ml.ImagePhashGenerator
import com.bes2.ml.NimaQualityAnalyzer
import com.bes2.ml.SmileDetector
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class MemoryEventWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val galleryRepository: GalleryRepository,
    private val reviewItemDao: ReviewItemDao,
    private val nimaAnalyzer: NimaQualityAnalyzer,
    private val smileDetector: SmileDetector
    // [FIX] Removed NotificationHelper from constructor as it is an object
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "MemoryEventWorker"
        const val KEY_TARGET_DATE = "key_target_date"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val targetDate = inputData.getString(KEY_TARGET_DATE)
        if (targetDate == null) {
            Timber.e("No target date provided for MemoryEventWorker")
            return@withContext Result.failure()
        }

        Timber.d("Starting Memory Analysis for date: $targetDate")

        try {
            val imagesFromMediaStore = galleryRepository.getImagesForDateString(targetDate)
            if (imagesFromMediaStore.isEmpty()) {
                Timber.w("No images found for $targetDate")
                return@withContext Result.failure()
            }

            val entities = imagesFromMediaStore.mapNotNull { mediaImage ->
                val isProcessed = reviewItemDao.isUriProcessed(mediaImage.uri)
                if (isProcessed) {
                    return@mapNotNull null
                }

                try {
                    val bitmap = loadBitmap(mediaImage.uri) ?: return@mapNotNull null

                    val pHash = ImagePhashGenerator.generatePhash(bitmap)
                    val nimaScores = nimaAnalyzer.analyze(bitmap)
                    val nimaScore = nimaScores?.mapIndexed { i, s -> (i + 1) * s }?.sum()?.toDouble()
                    val smileProb = smileDetector.getSmilingProbability(bitmap)

                    bitmap.recycle()

                    ReviewItemEntity(
                        uri = mediaImage.uri,
                        filePath = mediaImage.filePath,
                        timestamp = mediaImage.timestamp,

                        source_type = "MEMORY",
                        status = "EVENT_MEMORY",

                        pHash = pHash,
                        nimaScore = nimaScore,
                        musiqScore = null,
                        blurScore = 100f,
                        exposureScore = 0f,
                        areEyesClosed = false,
                        smilingProbability = smileProb,
                        cluster_id = null
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Failed to analyze ${mediaImage.uri}")
                    null
                }
            }

            if (entities.isNotEmpty()) {
                reviewItemDao.insertAll(entities)
                Timber.d("Saved ${entities.size} memory images for $targetDate")

                // [FIX] Call NotificationHelper directly as static object
                NotificationHelper.showReviewNotification(
                    context = appContext,
                    notificationIcon = R.drawable.ic_notification,
                    clusterCount = 1, // A single memory event
                    photoCount = entities.size,
                    sourceType = "MEMORY",
                    eventDate = targetDate
                )
            }

            return@withContext Result.success()

        } catch (e: Exception) {
            Timber.e(e, "Error in MemoryEventWorker")
            return@withContext Result.retry()
        }
    }

    private fun loadBitmap(uri: String): Bitmap? {
        return try {
            appContext.contentResolver.openInputStream(uri.toUri())?.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }
    }
}
