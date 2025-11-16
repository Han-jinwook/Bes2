package com.bes2.app.di

import android.content.Context
import com.bes2.app.R
import com.bes2.photos_integration.auth.NaverAuthInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideNaverAuthInfo(@ApplicationContext context: Context): NaverAuthInfo {
        return NaverAuthInfo(
            clientId = context.getString(R.string.naver_client_id),
            clientSecret = context.getString(R.string.naver_client_secret),
            clientName = context.getString(R.string.naver_client_name)
        )
    }
}
