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
                // 1. Get images that are analyzed but not clustered for this source type
                val candidates = reviewItemDao.getAnalyzedItemsWithoutCluster(sourceType)
                Timber.tag(WORK_NAME).d("[$sourceType] Query result: ${candidates.size} items found.")
                
                if (candidates.isEmpty()) continue

                // Map ReviewItemEntity to ImageItemEntity for helper
                val mappedCandidates = candidates.map { 
                    ImageItemEntity(id = it.id, uri = it.uri, timestamp = it.timestamp, filePath = it.filePath)
                }

                // 2. Perform Clustering
                val clusters = clusteringHelper.clusterImages(mappedCandidates)
                Timber.tag(WORK_NAME).d("[$sourceType] Clustered into ${clusters.size} groups.")

                // 3. Save Clusters
                for (cluster in clusters) {
                    val newClusterId = UUID.randomUUID().toString()
                    
                    val newClusterEntity = ImageClusterEntity(
                        id = newClusterId,
                        creationTime = System.currentTimeMillis(),
                        reviewStatus = "PENDING_REVIEW"
                    )
                    imageClusterDao.insertImageCluster(newClusterEntity)
                    
                    val imageIdsInCluster = cluster.images.map { it.id }
                    
                    reviewItemDao.updateClusterInfo(newClusterId, imageIdsInCluster)
                }
                
                // 4. Send Notification for INSTANT source
                if (sourceType == "INSTANT" && clusters.isNotEmpty()) {
                    Timber.tag(WORK_NAME).i("Sending notification for INSTANT clusters.")
                    NotificationHelper.showReviewNotification(
                        appContext,
                        R.drawable.ic_notification,
                        clusters.size,
                        candidates.size, // Total items processed
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
