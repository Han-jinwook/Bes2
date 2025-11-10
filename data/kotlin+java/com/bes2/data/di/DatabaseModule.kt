package com.bes2.data.di

import android.content.Context
import androidx.room.Room
import com.bes2.data.db.Bes2Database
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.dao.ImageClusterDao
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
            "bes2_database_v_kotlin_java"
        )
        .fallbackToDestructiveMigration() // 마이그레이션 실패 시 기존 DB를 삭제하고 재생성
        .build()
    }

    @Provides
    @Singleton
    fun provideImageItemDao(database: Bes2Database): ImageItemDao {
        return database.imageItemDao()
    }

    @Provides
    @Singleton
    fun provideImageClusterDao(database: Bes2Database): ImageClusterDao {
        return database.imageClusterDao()
    }
}
