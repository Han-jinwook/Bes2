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
import com.bes2.background.util.ImageClusteringHelper // [UPDATED] Import from background module
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.ImageClusterEntity
import com.bes2.data.model.ImageItemEntity
import com.bes2.ml.ImageCategory
import com.bes2.ml.ImageContentClassifier
import com.bes2.ml.ImagePhashGenerator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileNotFoundException
import java.util.UUID

@HiltWorker
class ClusteringWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val imageDao: ImageItemDao,
    private val imageClusterDao: ImageClusterDao,
    private val workManager: WorkManager,
    private val imageContentClassifier: ImageContentClassifier,
    private val clusteringHelper: ImageClusteringHelper
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "ClusteringWorker"
        const val KEY_IS_BACKGROUND_DIET = "is_background_diet"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val isBackgroundDiet = inputData.getBoolean(KEY_IS_BACKGROUND_DIET, false)
        Timber.tag(WORK_NAME).d("Worker started. Following PLAN.md: Classification -> Clustering -> Analysis. (isBackgroundDiet=$isBackgroundDiet)")
        try {
            val allNewImages = imageDao.getImageItemsListByStatus("NEW")
            
            if (allNewImages.isEmpty()) {
                Timber.tag(WORK_NAME).d("No new images to cluster.")
                checkAndTriggerAnalysis(isBackgroundDiet)
                return@withContext Result.success()
            }
            
            val (pathValidImages, pathInvalidImages) = allNewImages.partition { image ->
                !image.filePath.contains("Screenshot", ignoreCase = true) &&
                !image.filePath.contains("Capture", ignoreCase = true)
            }
            
            if (pathInvalidImages.isNotEmpty()) {
                val invalidIds = pathInvalidImages.map { it.id }
                imageDao.updateImageStatusesByIds(invalidIds, "IGNORED")
            }

            if (pathValidImages.isEmpty()) {
                Timber.tag(WORK_NAME).d("No valid images to process after path filtering.")
                checkAndTriggerAnalysis(isBackgroundDiet)
                return@withContext Result.success()
            }

            val imagesToCluster = mutableListOf<ImageItemEntity>()
            
            pathValidImages.forEach { image ->
                try {
                    val bitmap = loadBitmap(image.uri)
                    val category = imageContentClassifier.classify(bitmap)
                    
                    if (category == ImageCategory.DOCUMENT) {
                         Timber.tag(WORK_NAME).d("Image ${image.id} classified as DOCUMENT.")
                         imageDao.updateImageItem(image.copy(
                             category = "DOCUMENT",
                             status = "DETECTED_DOCUMENT",
                             pHash = null
                         ))
                    } else {
                        Timber.tag(WORK_NAME).d("Image ${image.id} classified as MEMORY.")
                        val pHash = ImagePhashGenerator.generatePhash(bitmap)
                        imagesToCluster.add(image.copy(
                            category = "MEMORY",
                            status = "PRE_CLUSTERING",
                            pHash = pHash
                        ))
                    }
                    bitmap.recycle()
                } catch (e: Exception) {
                    Timber.tag(WORK_NAME).e(e, "Failed to classify/process image ${image.uri}.")
                    imageDao.updateImageItem(image.copy(status = "ERROR_ANALYSIS"))
                }
            }

            imagesToCluster.forEach { image ->
                imageDao.updateImageItem(image)
            }

            Timber.tag(WORK_NAME).d("Proceeding to cluster ${imagesToCluster.size} MEMORY images.")

            val clusteringCandidates = imageDao.getImageItemsListByStatus("PRE_CLUSTERING")
            
            if (clusteringCandidates.isNotEmpty()) {
                val clusters = clusteringHelper.clusterImages(clusteringCandidates)
                Timber.tag(WORK_NAME).d("Clustered into ${clusters.size} groups.")

                for (cluster in clusters) {
                    val newClusterId = UUID.randomUUID().toString()
                    val newClusterEntity = ImageClusterEntity(
                        id = newClusterId,
                        creationTime = System.currentTimeMillis(),
                        reviewStatus = if (isBackgroundDiet) "BACKGROUND_PREPARING" else "PENDING_REVIEW"
                    )
                    imageClusterDao.insertImageCluster(newClusterEntity)
                    
                    val imageUrisInCluster = cluster.images.map { it.uri }
                    imageDao.updateClusterIdByUris(imageUrisInCluster, newClusterId)
                    
                    val imageIdsInCluster = cluster.images.map { it.id }
                    imageDao.updateImageStatusesByIds(imageIdsInCluster, "PENDING_ANALYSIS")
                }
            }

            triggerAnalysisWorker(isBackgroundDiet)

            return@withContext Result.success()
        } catch (e: Exception) {
            Timber.tag(WORK_NAME).e(e, "Error in ClusteringWorker")
            return@withContext Result.failure()
        }
    }
    
    private suspend fun checkAndTriggerAnalysis(isBackgroundDiet: Boolean) {
        val pendingCount = imageDao.countImagesByStatus("PENDING_ANALYSIS")
        if (pendingCount > 0) {
            Timber.tag(WORK_NAME).d("Found $pendingCount images pending analysis. Triggering PhotoAnalysisWorker.")
            triggerAnalysisWorker(isBackgroundDiet)
        }
    }

    private fun triggerAnalysisWorker(isBackgroundDiet: Boolean) {
        Timber.tag(WORK_NAME).d("Enqueuing PhotoAnalysisWorker.")
        val inputData = Data.Builder()
            .putBoolean(PhotoAnalysisWorker.KEY_IS_BACKGROUND_DIET, isBackgroundDiet)
            .build()
            
        val analysisWorkRequest = OneTimeWorkRequestBuilder<PhotoAnalysisWorker>()
            .setInputData(inputData)
            .build()
            
        workManager.enqueueUniqueWork(
            PhotoAnalysisWorker.WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            analysisWorkRequest
        )
    }

    private fun loadBitmap(uri: String): Bitmap {
        return appContext.contentResolver.openInputStream(uri.toUri())?.use {
            BitmapFactory.decodeStream(it)
        } ?: throw FileNotFoundException("ContentResolver returned null stream for $uri")
    }
}
