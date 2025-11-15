package com.bes2.background.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.ImageClusterEntity
import com.bes2.data.model.ImageItemEntity
import com.bes2.ml.ImagePhashGenerator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileNotFoundException

@HiltWorker
class ClusteringWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val imageDao: ImageItemDao,
    private val imageClusterDao: ImageClusterDao,
    private val workManager: WorkManager
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "ClusteringWorker"
        const val HAMMING_DISTANCE_THRESHOLD = 15 // Increased threshold as per user feedback
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Timber.tag(WORK_NAME).d("Worker started. Following PLAN.md: Clustering -> Analysis.")
        try {
            // 1. Get NEW images
            val newImages = imageDao.getImageItemsListByStatus("NEW")
            if (newImages.isEmpty()) {
                Timber.tag(WORK_NAME).d("No new images to process.")
                return@withContext Result.success()
            }
            Timber.tag(WORK_NAME).d("Found ${newImages.size} new images.")

            // 2. Calculate pHash for each new image
            val imagesWithPhash = newImages.mapNotNull { image ->
                try {
                    val bitmap = loadBitmap(image.uri)
                    // Call the object method directly
                    val pHash = ImagePhashGenerator.generatePhash(bitmap)
                    bitmap.recycle()
                    image.copy(pHash = pHash, status = "PRE_CLUSTERING")
                } catch (e: Exception) {
                    Timber.tag(WORK_NAME).e(e, "Failed to calculate pHash for ${image.uri}. Marking as error.")
                    imageDao.updateImageItem(image.copy(status = "ERROR_ANALYSIS"))
                    null
                }
            }
            // Use a loop with the existing 'updateImageItem' function
            imagesWithPhash.forEach { image ->
                imageDao.updateImageItem(image)
            }
            Timber.tag(WORK_NAME).d("Calculated pHash for ${imagesWithPhash.size} images.")

            // 3. Cluster images that now have a pHash
            val imagesToCluster = imageDao.getImageItemsListByStatus("PRE_CLUSTERING")
            val clusters = clusterImages(imagesToCluster)
            Timber.tag(WORK_NAME).d("Clustered into ${clusters.size} groups.")

            // 4. Save clusters and prepare for next step (Analysis)
            for (cluster in clusters) {
                val newClusterEntity = ImageClusterEntity(creationTime = System.currentTimeMillis())
                val newClusterId = imageClusterDao.insertImageCluster(newClusterEntity)
                val imageUrisInCluster = cluster.images.map { it.uri }
                
                // Use existing functions to update clusterId and then status
                imageDao.updateClusterIdByUris(imageUrisInCluster, newClusterId.toString())
                val imageIdsInCluster = cluster.images.map { it.id }
                imageDao.updateImageStatusesByIds(imageIdsInCluster, "PENDING_ANALYSIS")
            }

            // 5. Trigger the next worker in the pipeline
            Timber.tag(WORK_NAME).d("Clustering complete. Enqueuing PhotoAnalysisWorker.")
            val analysisWorkRequest = OneTimeWorkRequestBuilder<PhotoAnalysisWorker>().build()
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
