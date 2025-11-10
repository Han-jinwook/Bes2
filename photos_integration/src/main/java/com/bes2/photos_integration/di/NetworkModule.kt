package com.bes2.photos_integration.di

import com.bes2.photos_integration.network.GooglePhotosApiService
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            // Add any interceptors for logging or auth here if needed in the future
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder().setLenient().create()

        return Retrofit.Builder()
            // The base URL is not strictly needed here as we use dynamic URLs in the service,
            // but it's a required component for Retrofit.
            .baseUrl("https://photoslibrary.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create()) // For plain text responses (upload token)
            .addConverterFactory(GsonConverterFactory.create(gson)) // For JSON responses
            .build()
    }

    @Provides
    @Singleton
    fun provideGooglePhotosApiService(retrofit: Retrofit): GooglePhotosApiService {
        return retrofit.create(GooglePhotosApiService::class.java)
    }
}
