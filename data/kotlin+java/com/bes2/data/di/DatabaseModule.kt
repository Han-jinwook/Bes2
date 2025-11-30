package com.bes2.data.di

import android.content.Context
import androidx.room.Room
import com.bes2.data.db.Bes2Database
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.dao.ReviewItemDao
import com.bes2.data.dao.TrashItemDao
import com.bes2.data.model.StatusCount
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBes2Database(@ApplicationContext appContext: Context): Bes2Database {
        return Room.databaseBuilder(
            appContext,
            Bes2Database::class.java,
            "bes2_database_v2_split" 
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideReviewItemDao(database: Bes2Database): ReviewItemDao {
        return database.reviewItemDao()
    }

    @Provides
    @Singleton
    fun provideTrashItemDao(database: Bes2Database): TrashItemDao {
        return database.trashItemDao()
    }

    @Provides
    @Singleton
    fun provideImageClusterDao(database: Bes2Database): ImageClusterDao {
        return database.imageClusterDao()
    }
    
    // [TEMP] Provide a dummy ImageItemDao to fix build errors during refactoring.
    @Provides
    @Singleton
    fun provideImageItemDao(): ImageItemDao {
        return object : ImageItemDao {
            override suspend fun getImagesByCategory(category: String): List<com.bes2.data.model.ImageItemEntity> = emptyList()
            override suspend fun updateImageItem(item: com.bes2.data.model.ImageItemEntity) {}
            override fun getImageClustersByReviewStatus(status: String): kotlinx.coroutines.flow.Flow<List<com.bes2.data.model.ImageClusterEntity>> = kotlinx.coroutines.flow.flowOf(emptyList())
            override fun getImageClusterById(id: String): kotlinx.coroutines.flow.Flow<com.bes2.data.model.ImageClusterEntity?> = kotlinx.coroutines.flow.flowOf(null)
            override fun getImageItemsByClusterId(id: String): kotlinx.coroutines.flow.Flow<List<com.bes2.data.model.ImageItemEntity>> = kotlinx.coroutines.flow.flowOf(emptyList())
            override suspend fun getImagesByDateRange(start: Long, end: Long): List<com.bes2.data.model.ImageItemEntity> = emptyList()
            override suspend fun updateImageStatusesByIds(ids: List<Long>, status: String) {}
            
            override suspend fun getStatsByDateRange(startTime: Long, endTime: Long): List<StatusCount> = emptyList()
            override fun getDailyStatsFlow(startTime: Long): kotlinx.coroutines.flow.Flow<List<StatusCount>> = kotlinx.coroutines.flow.flowOf(emptyList())
            override fun countImagesByStatus(status: String): Int = 0
            override suspend fun getImageStatusByUri(uri: String): String? = null
            override fun getImageItemsByStatusFlow(status: String): kotlinx.coroutines.flow.Flow<List<com.bes2.data.model.ImageItemEntity>> = kotlinx.coroutines.flow.flowOf(emptyList())

            // [FIX] Implement missing members
            override suspend fun isUriProcessed(uri: String): Boolean = false
            override suspend fun insertImageItem(imageItem: com.bes2.data.model.ImageItemEntity): Long = 0
        }
    }
}
