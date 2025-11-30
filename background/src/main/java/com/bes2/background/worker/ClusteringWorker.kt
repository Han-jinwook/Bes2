package com.bes2.background.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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
    @Assisted appContext: Context,
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
        try {
            // 1. Get DIET images that are analyzed but not clustered
            val candidates = reviewItemDao.getAnalyzedItemsWithoutCluster("DIET")
            Timber.tag(WORK_NAME).d("Query result: ${candidates.size} items found for clustering.") // [DEBUG] Added log
            
            if (candidates.isEmpty()) {
                Timber.tag(WORK_NAME).d("No diet images to cluster.")
                return@withContext Result.success()
            }

            // Map ReviewItemEntity to ImageItemEntity for helper (Temporary Bridge)
            val mappedCandidates = candidates.map { 
                ImageItemEntity(id = it.id, uri = it.uri, timestamp = it.timestamp, filePath = it.filePath)
            }

            // 2. Perform Clustering
            val clusters = clusteringHelper.clusterImages(mappedCandidates)
            Timber.tag(WORK_NAME).d("Clustered into ${clusters.size} groups.") // [DEBUG] Added log

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
                
                // Update ReviewItemEntity with cluster info
                reviewItemDao.updateClusterInfo(newClusterId, imageIdsInCluster)
            }
            
            Timber.tag(WORK_NAME).d("Clustering finished. Saved ${clusters.size} clusters.")

            return@withContext Result.success()

        } catch (e: Exception) {
            Timber.tag(WORK_NAME).e(e, "Error in ClusteringWorker")
            return@withContext Result.failure()
        }
    }
}
