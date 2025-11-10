package com.bes2.app;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&J\b\u0010\u0006\u001a\u00020\u0007H&\u00a8\u0006\b"}, d2 = {"Lcom/bes2/app/WorkManagerInitializerEntryPoint;", "", "inject", "", "initializer", "Lcom/bes2/app/HiltWorkManagerInitializer;", "workerFactory", "Landroidx/hilt/work/HiltWorkerFactory;", "app_debug"})
@dagger.hilt.EntryPoint()
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract interface WorkManagerInitializerEntryPoint {
    
    @org.jetbrains.annotations.NotNull()
    public abstract androidx.hilt.work.HiltWorkerFactory workerFactory();
    
    public abstract void inject(@org.jetbrains.annotations.NotNull()
    com.bes2.app.HiltWorkManagerInitializer initializer);
}