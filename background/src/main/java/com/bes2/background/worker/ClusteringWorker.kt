package com.bes2.background.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bes2.background.R
import com.bes2.background.notification.NotificationHelper
import com.bes2.background.util.ImageClusteringHelper
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ReviewItemDao
import com.bes2.data.model.ImageClusterEntity
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
    private val reviewItemDao: ReviewItemDao,
    private val imageClusterDao: ImageClusterDao,
    private val clusteringHelper: ImageClusteringHelper
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "ClusteringWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Timber.tag(WORK_NAME).d("--- ClusteringWorker Started ---")
        
        val sourceTypes = listOf("DIET", "INSTANT")
        var hasError = false

        for (sourceType in sourceTypes) {
            try {
                val candidates = reviewItemDao.getAnalyzedItemsWithoutCluster(sourceType)
                Timber.tag(WORK_NAME).d("[$sourceType] Query result: ${candidates.size} items found.")
                
                if (candidates.isEmpty()) continue

                val validCandidates = candidates.filter { it.status == "ANALYZED" }
                val rejectedCandidates = candidates.filter { it.status == "STATUS_REJECTED" }

                val validMapped = validCandidates.map { ImageItemEntity(id = it.id, uri = it.uri, timestamp = it.timestamp, filePath = it.filePath) }
                val validClusters = clusteringHelper.clusterImages(validMapped)

                val createdClusterIds = mutableListOf<String>()

                for (cluster in validClusters) {
                    val newClusterId = UUID.randomUUID().toString()
                    createdClusterIds.add(newClusterId)
                    
                    val newClusterEntity = ImageClusterEntity(
                        id = newClusterId,
                        creationTime = System.currentTimeMillis(),
                        reviewStatus = "PENDING_REVIEW"
                    )
                    imageClusterDao.insertImageCluster(newClusterEntity)
                    
                    val imageIdsInCluster = cluster.images.map { it.id }
                    reviewItemDao.updateClusterInfo(newClusterId, imageIdsInCluster)
                }
                
                if (rejectedCandidates.isNotEmpty()) {
                    val targetClusterId = if (createdClusterIds.isNotEmpty()) {
                        createdClusterIds.last()
                    } else {
                        val trashClusterId = UUID.randomUUID().toString()
                        val trashClusterEntity = ImageClusterEntity(
                            id = trashClusterId,
                            creationTime = System.currentTimeMillis(),
                            reviewStatus = "PENDING_REVIEW"
                        )
                        imageClusterDao.insertImageCluster(trashClusterEntity)
                        trashClusterId
                    }
                    
                    val rejectedIds = rejectedCandidates.map { it.id }
                    reviewItemDao.updateClusterIdOnly(targetClusterId, rejectedIds)
                }

                if (sourceType == "INSTANT" && candidates.isNotEmpty()) {
                    NotificationHelper.showReviewNotification(
                        appContext,
                        R.drawable.ic_notification,
                        validClusters.size + (if (rejectedCandidates.isNotEmpty() && validClusters.isEmpty()) 1 else 0),
                        candidates.size,
                        "INSTANT"
                    )
                }
                
            } catch (e: Exception) {
                Timber.tag(WORK_NAME).e(e, "Error processing source type: $sourceType")
                hasError = true
            }
        }
        
        Timber.tag(WORK_NAME).d("Clustering finished.")
        return@withContext if (hasError) Result.failure() else Result.success()
    }
}
