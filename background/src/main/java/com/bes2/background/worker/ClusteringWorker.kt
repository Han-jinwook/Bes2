package com.bes2.background.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bes2.background.notification.NotificationHelper
import com.bes2.core_common.provider.ResourceProvider
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.ImageItemEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID

@HiltWorker
class ClusteringWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val imageDao: ImageItemDao,
    private val resourceProvider: ResourceProvider
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "ClusteringWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Timber.d("ClusteringWorker started.")
        try {
            val imagesToCluster = imageDao.getImageItemsListByStatus("ANALYZED")
            if (imagesToCluster.isEmpty()) {
                Timber.d("No new images to cluster.")
                NotificationHelper.dismissAllAppNotifications(appContext)
                return@withContext Result.success()
            }

            Timber.d("Found ${imagesToCluster.size} images to cluster.")

            val clusters = clusterImages(imagesToCluster)
            Timber.d("Clustered into ${clusters.size} groups.")

            val clusterInfos = clusters.map { cluster ->
                val clusterId = "CLUSTER_" + UUID.randomUUID().toString()
                // Restore the smile score bonus logic
                val bestImage = cluster.images.maxByOrNull { image ->
                    var score = image.nimaScore ?: 0.0f
                    if ((image.smilingProbability ?: 0.0f) > 0.7f) {
                        score *= 1.1f // 10% bonus
                    }
                    score
                }

                bestImage?.let {
                    imageDao.updateImageItem(it.copy(isBestInCluster = true, clusterId = clusterId))
                    val otherImageUris = cluster.images.filter { img -> img.id != it.id }.map { i -> i.uri }
                    if (otherImageUris.isNotEmpty()) {
                        imageDao.updateClusterIdByUris(otherImageUris, clusterId)
                    }
                }
                ClusterInfo(clusterId, cluster.images.size)
            }

            val validClusters = clusterInfos.filter { it.count > 0 }
            if (validClusters.isNotEmpty()) {
                NotificationHelper.showReviewNotification(appContext, resourceProvider.notificationIcon, validClusters.size)
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error in ClusteringWorker")
            Result.failure()
        }
    }

    private fun clusterImages(images: List<ImageItemEntity>): List<Cluster> {
        // Simple pHash-based clustering for now
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
        // This is a placeholder. A real implementation would use a more robust
        // image similarity metric (like pHash distance).
        return (image1.pHash != null && image1.pHash == image2.pHash)
    }

    data class Cluster(val images: MutableList<ImageItemEntity>)
    data class ClusterInfo(val clusterId: String, val count: Int)
}
