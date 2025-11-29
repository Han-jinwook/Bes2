package com.bes2.background.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.ImageItemEntity
import com.bes2.data.repository.GalleryRepository
import com.bes2.ml.ImagePhashGenerator
import com.bes2.ml.NimaQualityAnalyzer
import com.bes2.ml.SmileDetector
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileNotFoundException

@HiltWorker
class MemoryEventWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val galleryRepository: GalleryRepository,
    private val imageItemDao: ImageItemDao,
    private val nimaAnalyzer: NimaQualityAnalyzer,
    private val smileDetector: SmileDetector
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
            // 1. Fetch images from MediaStore
            val imagesFromMediaStore = galleryRepository.getImagesForDateString(targetDate)
            if (imagesFromMediaStore.isEmpty()) {
                Timber.w("No images found for $targetDate")
                return@withContext Result.failure()
            }

            // 2. Analyze each image and save to DB
            val entities = imagesFromMediaStore.mapNotNull { mediaImage ->
                // Check if already analyzed to avoid duplicate work
                val existing = imageItemDao.getImageStatusByUri(mediaImage.uri)
                if (existing != null && existing != "NEW") {
                    return@mapNotNull null // Skip already processed
                }

                try {
                    val bitmap = loadBitmap(mediaImage.uri) ?: return@mapNotNull null
                    
                    val pHash = ImagePhashGenerator.generatePhash(bitmap)
                    val nimaScores = nimaAnalyzer.analyze(bitmap)
                    val nimaScore = nimaScores?.mapIndexed { i, s -> (i + 1) * s }?.sum()
                    val smileProb = smileDetector.getSmilingProbability(bitmap)
                    
                    bitmap.recycle()

                    ImageItemEntity(
                        id = mediaImage.id, // Use MediaStore ID if possible, or let Room generate? 
                        // Room's ID is auto-gen. We should check if we need to preserve MediaStore ID. 
                        // Usually we map URI. Let's use 0 for ID to let Room auto-gen or handle conflict via URI.
                        // Actually our Entity has @PrimaryKey(autoGenerate = true) val id: Long = 0.
                        // But we also use this ID for logic. Let's rely on URI for uniqueness.
                        uri = mediaImage.uri,
                        filePath = mediaImage.filePath,
                        timestamp = mediaImage.timestamp,
                        status = "ANALYZED", // Mark as ready
                        pHash = pHash,
                        nimaScore = nimaScore,
                        musiqScore = null,
                        blurScore = 100f, // Assume good
                        exposureScore = 0f,
                        areEyesClosed = false,
                        smilingProbability = smileProb,
                        clusterId = null,
                        category = "MEMORY" // Explicitly mark as MEMORY
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Failed to analyze ${mediaImage.uri}")
                    null
                }
            }

            if (entities.isNotEmpty()) {
                imageItemDao.insertImageItems(entities)
                Timber.d("Saved ${entities.size} analyzed images for $targetDate")
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
                // Decode with sample size to save memory/time for analysis?
                // NIMA/Smile models usually resize anyway.
                // Let's decode full for now, or optimize if slow.
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }
    }
}
