package com.bes2.photos_integration.di

import com.bes2.photos_integration.network.GooglePhotosApiService
import com.bes2.photos_integration.network.NaverMyBoxApiService
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Named
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
    @Named("GoogleRetrofit")
    fun provideGoogleRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder().setLenient().create()

        return Retrofit.Builder()
            .baseUrl("https://photoslibrary.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create()) 
            .addConverterFactory(GsonConverterFactory.create(gson)) 
            .build()
    }
    
    @Provides
    @Singleton
    @Named("NaverRetrofit")
    fun provideNaverRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder().setLenient().create()

        // Placeholder Base URL. Naver MyBox API documentation is required.
        return Retrofit.Builder()
            .baseUrl("https://files.cloud.naver.com/") 
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create()) 
            .addConverterFactory(GsonConverterFactory.create(gson)) 
            .build()
    }

    @Provides
    @Singleton
    fun provideGooglePhotosApiService(@Named("GoogleRetrofit") retrofit: Retrofit): GooglePhotosApiService {
        return retrofit.create(GooglePhotosApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNaverMyBoxApiService(@Named("NaverRetrofit") retrofit: Retrofit): NaverMyBoxApiService {
        return retrofit.create(NaverMyBoxApiService::class.java)
    }
}
