package com.bes2.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ReviewItemDao
import com.bes2.data.dao.TrashItemDao
import com.bes2.data.model.ImageClusterEntity
import com.bes2.data.model.ReviewItemEntity
import com.bes2.data.model.TrashItemEntity

@Database(
    entities = [
        ReviewItemEntity::class, // Replaces ImageItemEntity
        TrashItemEntity::class,  // New trash table
        ImageClusterEntity::class
    ],
    version = 2, // Bump version for migration
    exportSchema = true
)
abstract class Bes2Database : RoomDatabase() {
    abstract fun reviewItemDao(): ReviewItemDao // Replaces imageItemDao
    abstract fun trashItemDao(): TrashItemDao   // New trash dao
    abstract fun imageClusterDao(): ImageClusterDao
}
