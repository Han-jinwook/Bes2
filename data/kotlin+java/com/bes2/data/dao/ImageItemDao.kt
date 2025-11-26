package com.bes2.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bes2.data.model.ImageItemEntity
import com.bes2.data.model.StatusCount
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertImageItem(imageItem: ImageItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertImageItems(imageItems: List<ImageItemEntity>): List<Long>

    @Update
    suspend fun updateImageItem(imageItem: ImageItemEntity)

    @Delete
    suspend fun deleteImageItem(imageItem: ImageItemEntity)

    @Query("SELECT * FROM image_items WHERE id = :id")
    fun getImageItemById(id: Long): Flow<ImageItemEntity?>

    @Query("SELECT * FROM image_items WHERE id IN (:ids)")
    suspend fun getImagesByIds(ids: List<Long>): List<ImageItemEntity>

    @Query("SELECT * FROM image_items ORDER BY timestamp DESC")
    fun getAllImageItems(): Flow<List<ImageItemEntity>>

    // [New] For Search: Get all items as list (One-shot)
    @Query("SELECT * FROM image_items ORDER BY timestamp DESC")
    suspend fun getAllImageItemsList(): List<ImageItemEntity>

    @Query("SELECT * FROM image_items WHERE status = :status ORDER BY timestamp DESC")
    fun getImageItemsByStatusFlow(status: String): Flow<List<ImageItemEntity>>

    @Query("SELECT * FROM image_items WHERE status = :status ORDER BY timestamp DESC")
    suspend fun getImageItemsListByStatus(status: String): List<ImageItemEntity>

    @Query("SELECT * FROM image_items WHERE status = :status AND isUploaded = :isUploaded")
    suspend fun getImagesByStatusAndUploadFlag(status: String, isUploaded: Boolean): List<ImageItemEntity>

    @Query("SELECT * FROM image_items WHERE status = 'ANALYZED' AND cluster_id IS NULL ORDER BY timestamp ASC")
    suspend fun getAnalyzedImagesWithoutCluster(): List<ImageItemEntity>

    // DEFINITIVE FIX: Changed clusterId type from Long to String
    @Query("SELECT * FROM image_items WHERE cluster_id = :clusterId ORDER BY timestamp DESC")
    fun getImageItemsByClusterId(clusterId: String): Flow<List<ImageItemEntity>>

    @Query("UPDATE image_items SET status = :newStatus WHERE id = :id")
    suspend fun updateImageItemStatus(id: Long, newStatus: String): Int

    // DEFINITIVE FIX: Changed clusterId type from Long to String
    @Query("UPDATE image_items SET status = 'CLUSTERED', cluster_id = :clusterId WHERE id IN (:imageIds)")
    suspend fun updateImageClusterInfo(clusterId: String, imageIds: List<Long>): Int

    // DEFINITIVE FIX: Changed clusterId type from Long to String
    @Query("UPDATE image_items SET cluster_id = :clusterId WHERE id IN (:imageIds)")
    suspend fun setClusterIdForImages(clusterId: String, imageIds: List<Long>): Int

    @Query("UPDATE image_items SET status = :newStatus WHERE id IN (:ids)")
    suspend fun updateImageStatusesByIds(ids: List<Long>, newStatus: String): Int

    @Query("UPDATE image_items SET isSelectedByUser = 1 WHERE id IN (:ids)")
    suspend fun updateIsSelectedByIds(ids: List<Long>): Int

    @Query("DELETE FROM image_items WHERE id IN (:ids)")
    suspend fun deleteImageItemsByIds(ids: List<Long>): Int

    @Query("DELETE FROM image_items")
    suspend fun clearAllImageItems()

    @Query("SELECT EXISTS(SELECT 1 FROM image_items WHERE uri = :uri LIMIT 1)")
    suspend fun isUriProcessed(uri: String): Boolean

    @Query("SELECT * FROM image_items WHERE status = 'KEPT' AND isUploaded = 0")
    suspend fun getKeptAndNotUploadedImages(): List<ImageItemEntity>

    @Query("UPDATE image_items SET isUploaded = :uploaded WHERE uri IN (:uris)")
    suspend fun updateUploadedStatusByUris(uris: List<String>, uploaded: Boolean): Int

    // DEFINITIVE FIX: Add the missing function that caused the build error
    @Query("UPDATE image_items SET cluster_id = :clusterId WHERE uri IN (:uris)")
    suspend fun updateClusterIdByUris(uris: List<String>, clusterId: String)

    // New query for daily statistics
    @Query("SELECT status, COUNT(*) as count FROM image_items WHERE timestamp >= :startTime GROUP BY status")
    fun getDailyStatsFlow(startTime: Long): Flow<List<StatusCount>>

    // New query for range statistics (Monthly/Yearly)
    @Query("SELECT status, COUNT(*) as count FROM image_items WHERE timestamp >= :startTime AND timestamp <= :endTime GROUP BY status")
    suspend fun getStatsByDateRange(startTime: Long, endTime: Long): List<StatusCount>

    // New query to get status by URI
    @Query("SELECT status FROM image_items WHERE uri = :uri LIMIT 1")
    suspend fun getImageStatusByUri(uri: String): String?

    // New query to update status by URI list
    @Query("UPDATE image_items SET status = :newStatus WHERE uri IN (:uris)")
    suspend fun updateImageStatusesByUris(uris: List<String>, newStatus: String): Int

    @Query("SELECT COUNT(*) FROM image_items WHERE status = :status")
    suspend fun countImagesByStatus(status: String): Int
    
    // [New] Get images by category (e.g., DOCUMENT)
    @Query("SELECT * FROM image_items WHERE category = :category ORDER BY timestamp DESC")
    suspend fun getImageItemsByCategory(category: String): List<ImageItemEntity>

    // [New] Count images by category, excluding processed ones
    @Query("SELECT COUNT(*) FROM image_items WHERE category = :category AND status != 'KEPT' AND status != 'DELETED'")
    suspend fun countUnprocessedImagesByCategory(category: String): Int

    // [New] Get images by date range (for Memory Event) - Excluding documents
    @Query("SELECT * FROM image_items WHERE timestamp >= :startTime AND timestamp <= :endTime AND (category != 'DOCUMENT' OR category IS NULL) ORDER BY timestamp DESC")
    suspend fun getImagesByDateRange(startTime: Long, endTime: Long): List<ImageItemEntity>
}
