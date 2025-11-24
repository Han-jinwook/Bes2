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
    private val imageContentClassifier: ImageContentClassifier
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "ClusteringWorker"
        const val HAMMING_DISTANCE_THRESHOLD = 15 // Increased threshold as per user feedback
        const val KEY_IS_BACKGROUND_DIET = "is_background_diet"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val isBackgroundDiet = inputData.getBoolean(KEY_IS_BACKGROUND_DIET, false)
        Timber.tag(WORK_NAME).d("Worker started. Following PLAN.md: Classification -> Clustering -> Analysis. (isBackgroundDiet=$isBackgroundDiet)")
        try {
            // 1. Get NEW images
            val allNewImages = imageDao.getImageItemsListByStatus("NEW")
            if (allNewImages.isEmpty()) {
                Timber.tag(WORK_NAME).d("No new images to process.")
                return@withContext Result.success()
            }
            
            // Filter out screenshots based on path (Legacy check, but good for optimization)
            val (pathValidImages, pathInvalidImages) = allNewImages.partition { image ->
                !image.filePath.contains("Screenshot", ignoreCase = true) &&
                !image.filePath.contains("Capture", ignoreCase = true)
            }
            
            // Mark path-detected screenshots as IGNORED (Legacy)
            if (pathInvalidImages.isNotEmpty()) {
                Timber.tag(WORK_NAME).d("Filtering out ${pathInvalidImages.size} screenshots/captures by path.")
                val invalidIds = pathInvalidImages.map { it.id }
                imageDao.updateImageStatusesByIds(invalidIds, "IGNORED")
            }

            if (pathValidImages.isEmpty()) {
                Timber.tag(WORK_NAME).d("No valid images to process after path filtering.")
                return@withContext Result.success()
            }

            // 1.5 Smart Classification (MEMORY vs DOCUMENT)
            val imagesToCluster = mutableListOf<ImageItemEntity>()
            
            pathValidImages.forEach { image ->
                try {
                    val bitmap = loadBitmap(image.uri)
                    val category = imageContentClassifier.classify(bitmap)
                    
                    if (category == ImageCategory.DOCUMENT) {
                         // Save as DOCUMENT and skip clustering
                         Timber.tag(WORK_NAME).d("Image ${image.id} classified as DOCUMENT. Skipping clustering.")
                         imageDao.updateImageItem(image.copy(
                             category = "DOCUMENT",
                             status = "DETECTED_DOCUMENT", // Distinct status
                             pHash = null // No need for pHash
                         ))
                         // Recycle handled inside loadBitmap? No, loadBitmap creates it.
                         // classify uses it. We should recycle it here.
                    } else {
                        // MEMORY: Proceed to pHash calculation
                        Timber.tag(WORK_NAME).d("Image ${image.id} classified as MEMORY.")
                        // We need pHash for clustering
                        val pHash = ImagePhashGenerator.generatePhash(bitmap)
                        
                        // Add to list for clustering
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

            // Save MEMORY images with pHash to DB
            imagesToCluster.forEach { image ->
                imageDao.updateImageItem(image)
            }

            if (imagesToCluster.isEmpty()) {
                 Timber.tag(WORK_NAME).d("No MEMORY images found to cluster.")
                 // Even if no clusters, we might have processed DOCUMENTS, so we are done.
                 // But if there are NO clusters, PhotoAnalysisWorker might have nothing to do?
                 // PhotoAnalysisWorker works on PENDING_ANALYSIS. If we added none, it finishes early.
                 // So we should still trigger it in case there were previous pending items?
                 // Or just return success. Let's follow standard flow.
            }

            Timber.tag(WORK_NAME).d("Proceeding to cluster ${imagesToCluster.size} MEMORY images.")

            // 3. Cluster images that are marked PRE_CLUSTERING (Just processed ones + potentially older failed ones if we re-query?)
            // Ideally we only cluster what we just processed or query DB again.
            // To be safe and robust, let's query DB for PRE_CLUSTERING status.
            val clusteringCandidates = imageDao.getImageItemsListByStatus("PRE_CLUSTERING")
            
            if (clusteringCandidates.isNotEmpty()) {
                val clusters = clusterImages(clusteringCandidates)
                Timber.tag(WORK_NAME).d("Clustered into ${clusters.size} groups.")

                // 4. Save clusters and prepare for next step (Analysis)
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

            // 5. Trigger the next worker in the pipeline
            Timber.tag(WORK_NAME).d("Clustering complete. Enqueuing PhotoAnalysisWorker.")
            
            val inputData = Data.Builder()
                .putBoolean(PhotoAnalysisWorker.KEY_IS_BACKGROUND_DIET, isBackgroundDiet)
                .build()
                
            val analysisWorkRequest = OneTimeWorkRequestBuilder<PhotoAnalysisWorker>()
                .setInputData(inputData)
                .build()
                
            workManager.enqueueUniqueWork(
                PhotoAnalysisWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                analysisWorkRequest
            )

            return@withContext Result.success()
        } catch (e: Exception) {
            Timber.tag(WORK_NAME).e(e, "Error in ClusteringWorker")
            return@withContext Result.failure()
        }
    }

    private fun loadBitmap(uri: String): Bitmap {
        return appContext.contentResolver.openInputStream(uri.toUri())?.use {
            BitmapFactory.decodeStream(it)
        } ?: throw FileNotFoundException("ContentResolver returned null stream for $uri")
    }

    private fun clusterImages(images: List<ImageItemEntity>): List<Cluster> {
        val imageIds = images.joinToString(", ") { it.id.toString() }
        Timber.tag(WORK_NAME).d("[clusterImages] Starting with ${images.size} images: [$imageIds]")

        val clusters = mutableListOf<Cluster>()
        val unclusteredImages = images.toMutableList()

        while (unclusteredImages.isNotEmpty()) {
            val currentImage = unclusteredImages.removeAt(0)
            val newCluster = Cluster(mutableListOf(currentImage))
            val iterator = unclusteredImages.iterator()

            while (iterator.hasNext()) {
                val otherImage = iterator.next()
                if (areSimilar(currentImage, otherImage)) {
                    newCluster.images.add(otherImage)
                    iterator.remove()
                }
            }
            clusters.add(newCluster)
        }
        return clusters
    }

    private fun areSimilar(image1: ImageItemEntity, image2: ImageItemEntity): Boolean {
        val pHash1 = image1.pHash
        val pHash2 = image2.pHash
        if (pHash1 == null || pHash2 == null || pHash1.length != pHash2.length) {
            Timber.tag(WORK_NAME).w("[areSimilar] Cannot compare: pHash is null or lengths differ. Image1: ${image1.id}, Image2: ${image2.id}")
            return false
        }
        val distance = ImagePhashGenerator.calculateHammingDistance(pHash1, pHash2)
        Timber.tag(WORK_NAME).d("[areSimilar] Comparing #${image1.id} ('$pHash1') and #${image2.id} ('$pHash2'): Distance = $distance")
        return distance <= HAMMING_DISTANCE_THRESHOLD
    }

    data class Cluster(val images: MutableList<ImageItemEntity>)
}
