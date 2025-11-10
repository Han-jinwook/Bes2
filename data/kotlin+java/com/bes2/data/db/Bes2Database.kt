package com.bes2.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.ImageClusterEntity
import com.bes2.data.model.ImageItemEntity

@Database(
    entities = [ImageItemEntity::class, ImageClusterEntity::class],
    version = 4, // 버전 4 유지
    exportSchema = true, // 스키마 내보내기는 유지하여 다음 마이그레이션을 준비
    autoMigrations = [] // 자동 마이그레이션 설정 제거
)
abstract class Bes2Database : RoomDatabase() {
    abstract fun imageItemDao(): ImageItemDao
    abstract fun imageClusterDao(): ImageClusterDao
}
