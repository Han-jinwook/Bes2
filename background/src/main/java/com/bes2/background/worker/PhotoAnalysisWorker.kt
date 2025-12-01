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
import com.bes2.ml.BacklightingDetector
import com.bes2.ml.EyeClosedDetector
import com.bes2.ml.FaceEmbedder
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
    private val semanticSearchEngine: SemanticSearchEngine
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "PhotoAnalysisWorker"
        const val BLUR_THRESHOLD = 30.0f
        const val KEY_IS_BACKGROUND_DIET = "is_background_diet" 
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Timber.tag(WORK_NAME).d("--- PhotoAnalysisWorker Started ---")

        try {
            val imagesToAnalyze = reviewItemDao.getNewDietItems() + reviewItemDao.getItemsBySourceAndStatus("INSTANT", "NEW")
            
            if (imagesToAnalyze.isEmpty()) {
                Timber.tag(WORK_NAME).d("No new images to analyze.")
                return@withContext Result.success()
            }
            
            Timber.tag(WORK_NAME).d("Analyzing ${imagesToAnalyze.size} images.")
            var hasAnalyzedImages = false

            for (imageItem in imagesToAnalyze) {
                var bitmap: Bitmap? = null
                try {
                    bitmap = loadBitmapSimple(imageItem.uri)
                    if (bitmap == null) {
                        reviewItemDao.updateStatusByIds(listOf(imageItem.id), "ERROR_LOAD")
                        continue
                    }

                    // 1. Basic Filters (Fail Fast)
                    val areEyesClosed = eyeClosedDetector.areEyesClosed(bitmap)
                    val blurScore = ImageQualityAssessor.calculateBlurScore(bitmap)
                    val isBacklit = backlightingDetector.isBacklit(bitmap)

                    val nextStatus = if (areEyesClosed || blurScore < BLUR_THRESHOLD || isBacklit) "STATUS_REJECTED" else "ANALYZED"
                    
                    // 2. Score Calculation (ONLY if Passed)
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
                    } else {
                        Timber.d("Image ${imageItem.id} REJECTED (Eyes: $areEyesClosed, Blur: $blurScore, Backlit: $isBacklit). Skipping detailed scoring.")
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
                        exposureScore = if (isBacklit) -1.0f else 0.0f
                    )
                    
                    reviewItemDao.update(updatedItem)
                    hasAnalyzedImages = true
                } catch (e: Exception) {
                    Timber.tag(WORK_NAME).e(e, "Error analyzing image: ${imageItem.uri}")
                    reviewItemDao.updateStatusByIds(listOf(imageItem.id), "ERROR_ANALYSIS")
                } finally {
                    bitmap?.recycle()
                }
            }
            
            if (hasAnalyzedImages) {
                 Timber.tag(WORK_NAME).d("Analysis complete. Triggering ClusteringWorker.")
                 val clusteringRequest = OneTimeWorkRequestBuilder<ClusteringWorker>().build()
                 workManager.enqueueUniqueWork(
                     ClusteringWorker.WORK_NAME,
                     ExistingWorkPolicy.APPEND_OR_REPLACE,
                     clusteringRequest
                 )
            }
            return@withContext Result.success()
        } catch (e: Exception) {
            Timber.tag(WORK_NAME).e(e, "Error in PhotoAnalysisWorker")
            return@withContext Result.failure()
        }
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
            Timber.e(e, "Failed to load bitmap: $uri")
            null
        }
    }
}
