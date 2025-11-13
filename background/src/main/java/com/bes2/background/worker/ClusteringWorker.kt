package com.bes2.background.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bes2.background.notification.NotificationHelper
import com.bes2.core_common.provider.ResourceProvider
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.ImageClusterEntity
import com.bes2.data.model.ImageItemEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class ClusteringWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val imageDao: ImageItemDao,
    private val imageClusterDao: ImageClusterDao,
    private val resourceProvider: ResourceProvider
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "ClusteringWorker"
        const val HAMMING_DISTANCE_THRESHOLD = 10 // pHash 유사도 임계값 완화
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Timber.d("ClusteringWorker started.")
        try {
            // DEFINITIVE FIX: Cluster both ANALYZED and REJECTED images together.
            val analyzedImages = imageDao.getImageItemsListByStatus("ANALYZED")
            val rejectedImages = imageDao.getImageItemsListByStatus("STATUS_REJECTED")
            val imagesToCluster = (analyzedImages + rejectedImages).filter { it.clusterId == null }

            if (imagesToCluster.isEmpty()) {
                Timber.d("No new images to cluster.")
                NotificationHelper.dismissAllAppNotifications(appContext)
                return@withContext Result.success()
            }

            Timber.d("Found ${imagesToCluster.size} images to cluster.")

            val clusters = clusterImages(imagesToCluster)
            Timber.d("Clustered into ${clusters.size} groups.")

            var newClustersForReview = 0
            for (cluster in clusters) {
                if (cluster.images.isEmpty() || cluster.images.all { it.status == "STATUS_REJECTED" }) {
                    // 모든 사진이 실패한 클러스터는 리뷰할 필요가 없음
                    val allImageUris = cluster.images.map { it.uri }
                    if (allImageUris.isNotEmpty()) {
                        // 클러스터 ID는 할당해주되, 리뷰 대상에서는 제외
                        val clusterIdString = "CLUSTER_REJECTED_${System.currentTimeMillis()}"
                        imageDao.updateClusterIdByUris(allImageUris, clusterIdString)
                    }
                    continue
                }

                val newClusterEntity = ImageClusterEntity(creationTime = System.currentTimeMillis())
                val newClusterId = imageClusterDao.insertImageCluster(newClusterEntity)
                val clusterIdString = newClusterId.toString()

                // 점수 계산은 정상 사진(ANALYZED) 내에서만 수행
                val bestImage = cluster.images.filter { it.status == "ANALYZED" }.maxByOrNull { image ->
                    val nimaScore = (image.nimaScore ?: 0f) * 10
                    val smileBonus = if ((image.smilingProbability ?: 0f) > 0.7f) 10f else 0f
                    nimaScore + smileBonus
                }

                val allImageUris = cluster.images.map { it.uri }
                imageDao.updateClusterIdByUris(allImageUris, clusterIdString)

                bestImage?.let {
                    imageDao.updateImageItem(it.copy(isBestInCluster = true))
                }
                newClustersForReview++
            }

            if (newClustersForReview > 0) {
                Timber.d("Notifying user about $newClustersForReview new clusters.")
                NotificationHelper.showReviewNotification(appContext, resourceProvider.notificationIcon, newClustersForReview)
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error in ClusteringWorker")
            Result.failure()
        }
    }

    private fun clusterImages(images: List<ImageItemEntity>): List<Cluster> {
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
            return false
        }
        val distance = pHash1.zip(pHash2).count { (c1, c2) -> c1 != c2 }
        return distance <= HAMMING_DISTANCE_THRESHOLD
    }

    data class Cluster(val images: MutableList<ImageItemEntity>)
}
