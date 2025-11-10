package com.bes2.app.di

import android.content.Context
import com.bes2.ml.NimaQualityAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyzerModule {

    @Provides
    @Singleton
    fun provideNimaQualityAnalyzer(@ApplicationContext context: Context): NimaQualityAnalyzer {
        return NimaQualityAnalyzer(context)
    }
}
