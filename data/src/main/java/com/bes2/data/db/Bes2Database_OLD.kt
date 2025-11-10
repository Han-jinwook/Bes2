package com.bes2.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.ImageClusterEntity
import com.bes2.data.model.ImageItemEntity

@Database(
    entities = [ImageItemEntity::class, ImageClusterEntity::class],
    version = 1, // 초기 버전은 1로 시작, 스키마 변경 시 버전 증가
    exportSchema = true // 스키마를 파일로 내보낼지 여부 (true 권장)
)
abstract class Bes2Database : RoomDatabase() {

    abstract fun imageItemDao(): ImageItemDao
    abstract fun imageClusterDao(): ImageClusterDao

    // companion object {
    //     // Singleton instance (Hilt가 이를 관리하므로 직접 필요하지 않을 수 있음)
    //     @Volatile
    //     private var INSTANCE: Bes2Database? = null
    //
    //     fun getInstance(context: Context): Bes2Database {
    //         return INSTANCE ?: synchronized(this) {
    //             val instance = Room.databaseBuilder(
    //                 context.applicationContext,
    //                 Bes2Database::class.java,
    //                 "bes2_database" // 데이터베이스 파일명
    //             )
    //             // .addMigrations(MIGRATION_1_2) // 마이그레이션 필요시 추가
    //             .build()
    //             INSTANCE = instance
    //             instance
    //         }
    //     }
    // }
}
