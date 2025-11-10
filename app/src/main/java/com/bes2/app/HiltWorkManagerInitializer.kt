package com.bes2.app

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

// HiltWorkerFactory를 WorkManager에 주입하기 위한 Initializer입니다.
class HiltWorkManagerInitializer : Initializer<WorkManager> {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HiltWorkManagerInitializerEntryPoint {
        fun hiltWorkerFactory(): HiltWorkerFactory
    }

    override fun create(context: Context): WorkManager {
        val workerFactory = EntryPointAccessors.fromApplication(
            context,
            HiltWorkManagerInitializerEntryPoint::class.java
        ).hiltWorkerFactory()

        val config = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

        WorkManager.initialize(context, config)

        return WorkManager.getInstance(context)
    }

    // WorkManager의 기본 Initializer가 실행되지 않도록 합니다.
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
