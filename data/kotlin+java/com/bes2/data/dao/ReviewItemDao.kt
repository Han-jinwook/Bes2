package com.bes2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bes2.data.model.ReviewItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ReviewItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<ReviewItemEntity>)

    @Update
    suspend fun update(item: ReviewItemEntity)

    @Query("SELECT * FROM review_items WHERE source_type = :sourceType ORDER BY timestamp DESC")
    fun getItemsBySourceTypeFlow(sourceType: String): Flow<List<ReviewItemEntity>>
    
    @Query("SELECT * FROM review_items WHERE source_type = :sourceType AND status = :status ORDER BY timestamp DESC")
    suspend fun getItemsBySourceAndStatus(sourceType: String, status: String): List<ReviewItemEntity>

    @Query("SELECT * FROM review_items WHERE cluster_id = :clusterId")
    suspend fun getItemsByClusterId(clusterId: String): List<ReviewItemEntity>

    @Query("SELECT * FROM review_items WHERE status = 'NEW' AND source_type = 'DIET'")
    suspend fun getNewDietItems(): List<ReviewItemEntity>
    
    @Query("SELECT EXISTS(SELECT 1 FROM review_items WHERE uri = :uri LIMIT 1)")
    suspend fun isUriProcessed(uri: String): Boolean

    @Query("UPDATE review_items SET status = :newStatus WHERE id IN (:ids)")
    suspend fun updateStatusByIds(ids: List<Long>, newStatus: String)
    
    @Query("UPDATE review_items SET cluster_id = :clusterId, status = 'CLUSTERED' WHERE id IN (:ids)")
    suspend fun updateClusterInfo(clusterId: String, ids: List<Long>)

    @Query("UPDATE review_items SET cluster_id = :clusterId WHERE id IN (:ids)")
    suspend fun updateClusterIdOnly(clusterId: String, ids: List<Long>)
    
    @Query("SELECT COUNT(*) FROM review_items WHERE status = 'KEPT' OR status = 'DELETED'")
    fun getProcessedCountFlow(): Flow<Int>
    
    @Query("SELECT * FROM review_items WHERE (status = 'ANALYZED' OR status = 'STATUS_REJECTED') AND cluster_id IS NULL AND source_type = :sourceType")
    suspend fun getAnalyzedItemsWithoutCluster(sourceType: String): List<ReviewItemEntity>

    @Query("SELECT * FROM review_items WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp DESC")
    suspend fun getImagesByDateRange(start: Long, end: Long): List<ReviewItemEntity>

    @Query("SELECT COUNT(*) FROM review_items WHERE source_type = 'DIET' AND status != 'KEPT' AND status != 'DELETED'")
    suspend fun getActiveDietCount(): Int

    @Query("SELECT COUNT(*) FROM review_items WHERE source_type = 'DIET' AND status != 'KEPT' AND status != 'DELETED'")
    fun getActiveDietCountFlow(): Flow<Int>

    // [FIX] Daily stats queries (based on photo timestamp)
    @Query("SELECT COUNT(*) FROM review_items WHERE status = 'KEPT' AND timestamp >= :startOfDay")
    fun getDailyKeptCountFlow(startOfDay: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM review_items WHERE status = 'DELETED' AND timestamp >= :startOfDay")
    fun getDailyDeletedCountFlow(startOfDay: Long): Flow<Int>
}
