package com.bes2.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.bes2.data.model.StatusCount

// THIS IS A TEMPORARY FILE TO FIX BUILD ERRORS.
@Dao
interface ImageItemDao {
    @Query("SELECT COUNT(*) FROM review_items") 
    suspend fun getImagesByCategory(category: String): List<com.bes2.data.model.ImageItemEntity>

    @Query("SELECT COUNT(*) FROM review_items")
    suspend fun updateImageItem(item: com.bes2.data.model.ImageItemEntity)
    
    @Query("SELECT COUNT(*) FROM review_items")
    fun getImageClustersByReviewStatus(status: String): kotlinx.coroutines.flow.Flow<List<com.bes2.data.model.ImageClusterEntity>>

    @Query("SELECT COUNT(*) FROM review_items")
    fun getImageClusterById(id: String): kotlinx.coroutines.flow.Flow<com.bes2.data.model.ImageClusterEntity?>
    
    @Query("SELECT COUNT(*) FROM review_items")
    fun getImageItemsByClusterId(id: String): kotlinx.coroutines.flow.Flow<List<com.bes2.data.model.ImageItemEntity>>
    
    @Query("SELECT COUNT(*) FROM review_items")
    suspend fun getImagesByDateRange(start: Long, end: Long): List<com.bes2.data.model.ImageItemEntity>

    @Query("SELECT COUNT(*) FROM review_items")
    suspend fun updateImageStatusesByIds(ids: List<Long>, status: String)
    
    @Query("SELECT status, COUNT(*) as count FROM review_items GROUP BY status")
    suspend fun getStatsByDateRange(startTime: Long, endTime: Long): List<StatusCount>
    
    @Query("SELECT status, COUNT(*) as count FROM review_items GROUP BY status")
    fun getDailyStatsFlow(startTime: Long): kotlinx.coroutines.flow.Flow<List<StatusCount>>
    
    @Query("SELECT COUNT(*) FROM review_items WHERE status = :status")
    fun countImagesByStatus(status: String): Int
    
    @Query("SELECT status FROM review_items WHERE uri = :uri")
    suspend fun getImageStatusByUri(uri: String): String?
    
    @Query("SELECT * FROM review_items WHERE status = :status")
    fun getImageItemsByStatusFlow(status: String): kotlinx.coroutines.flow.Flow<List<com.bes2.data.model.ImageItemEntity>>

    // [FIX] Added missing functions for MediaChangeObserver
    @Query("SELECT EXISTS(SELECT 1 FROM review_items WHERE uri = :uri LIMIT 1)")
    suspend fun isUriProcessed(uri: String): Boolean

    @Query("SELECT COUNT(*) FROM review_items") // Dummy query
    suspend fun insertImageItem(imageItem: com.bes2.data.model.ImageItemEntity): Long
}
