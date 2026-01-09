package com.bes2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bes2.data.model.TrashItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrashItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: TrashItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<TrashItemEntity>)

    @Query("SELECT * FROM trash_items ORDER BY timestamp DESC")
    fun getAllTrashItems(): Flow<List<TrashItemEntity>>

    @Query("SELECT COUNT(*) FROM trash_items WHERE status = 'READY'")
    fun getReadyTrashCountFlow(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM trash_items WHERE status = 'READY'")
    suspend fun getReadyTrashCount(): Int

    @Query("SELECT * FROM trash_items WHERE status = 'READY' ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getReadyTrashItems(limit: Int): List<TrashItemEntity>

    @Query("SELECT * FROM trash_items WHERE status = 'READY' ORDER BY timestamp DESC")
    suspend fun getAllReadyTrashItems(): List<TrashItemEntity>

    @Query("UPDATE trash_items SET status = :status WHERE uri IN (:uris)")
    suspend fun updateStatusByUris(uris: List<String>, status: String)

    @Query("DELETE FROM trash_items WHERE uri IN (:uris)")
    suspend fun deleteByUris(uris: List<String>)
    
    // [ADDED] To allow re-scanning, we delete previous results
    @Query("DELETE FROM trash_items WHERE status = 'READY'")
    suspend fun deleteAllReadyTrashItems()

    @Query("SELECT EXISTS(SELECT 1 FROM trash_items WHERE uri = :uri LIMIT 1)")
    suspend fun isUriProcessed(uri: String): Boolean
}
