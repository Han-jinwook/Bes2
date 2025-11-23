package com.bes2.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bes2.data.model.ImageClusterEntity // kotlin+java 경로의 엔티티 참조
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageClusterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImageCluster(imageCluster: ImageClusterEntity): Long

    @Update
    suspend fun updateImageCluster(imageCluster: ImageClusterEntity)

    @Delete
    suspend fun deleteImageCluster(imageCluster: ImageClusterEntity)

    @Query("SELECT * FROM image_clusters WHERE id = :id")
    fun getImageClusterById(id: String): Flow<ImageClusterEntity?>

    @Query("SELECT * FROM image_clusters ORDER BY creation_time DESC")
    fun getAllImageClusters(): Flow<List<ImageClusterEntity>>

    @Query("SELECT * FROM image_clusters WHERE review_status = :reviewStatus ORDER BY creation_time DESC")
    fun getImageClustersByReviewStatus(reviewStatus: String): Flow<List<ImageClusterEntity>>

    @Query("UPDATE image_clusters SET review_status = :newStatus WHERE id = :id")
    suspend fun updateImageClusterReviewStatus(id: String, newStatus: String): Int

    @Query("DELETE FROM image_clusters")
    suspend fun clearAllImageClusters()
}
