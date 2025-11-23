package com.bes2.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.ImageClusterEntity
import com.bes2.data.model.ImageItemEntity

@Database(
    entities = [ImageItemEntity::class, ImageClusterEntity::class],
    version = 5, // 버전 5로 증가 (Schema Change: ImageClusterEntity.id Long -> String)
    exportSchema = true,
    autoMigrations = []
)
abstract class Bes2Database : RoomDatabase() {
    abstract fun imageItemDao(): ImageItemDao
    abstract fun imageClusterDao(): ImageClusterDao
}
