package com.bes2.app

import androidx.hilt.work.HiltWorkerFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// HiltWorkManagerInitializer에 의존성을 주입하기 위한 Hilt EntryPoint 입니다.
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkManagerInitializerEntryPoint {
    // HiltWorkerFactory 인스턴스를 제공합니다.
    fun workerFactory(): HiltWorkerFactory
    
    // Initializer에 Hilt 의존성을 주입하는 메서드입니다.
    fun inject(initializer: HiltWorkManagerInitializer)
}
