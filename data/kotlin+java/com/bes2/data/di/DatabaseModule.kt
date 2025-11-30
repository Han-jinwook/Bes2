package com.bes2.data.di

import android.content.Context
import androidx.room.Room
import com.bes2.data.db.Bes2Database
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ReviewItemDao
import com.bes2.data.dao.TrashItemDao
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
}
