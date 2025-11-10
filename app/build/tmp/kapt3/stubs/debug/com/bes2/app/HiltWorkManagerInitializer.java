package com.bes2.app;

import android.content.Context;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.startup.Initializer;
import androidx.work.Configuration;
import androidx.work.WorkManager;
import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001\nB\u0005\u00a2\u0006\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00022\u0006\u0010\u0005\u001a\u00020\u0006H\u0016J\u001a\u0010\u0007\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\b\u0001\u0012\u0006\u0012\u0002\b\u00030\u00010\t0\bH\u0016\u00a8\u0006\u000b"}, d2 = {"Lcom/bes2/app/HiltWorkManagerInitializer;", "Landroidx/startup/Initializer;", "Landroidx/work/WorkManager;", "()V", "create", "context", "Landroid/content/Context;", "dependencies", "", "Ljava/lang/Class;", "HiltWorkManagerInitializerEntryPoint", "app_debug"})
public final class HiltWorkManagerInitializer implements androidx.startup.Initializer<androidx.work.WorkManager> {
    
    public HiltWorkManagerInitializer() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public androidx.work.WorkManager create(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.util.List<java.lang.Class<? extends androidx.startup.Initializer<?>>> dependencies() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0010\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\bg\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&\u00a8\u0006\u0004"}, d2 = {"Lcom/bes2/app/HiltWorkManagerInitializer$HiltWorkManagerInitializerEntryPoint;", "", "hiltWorkerFactory", "Landroidx/hilt/work/HiltWorkerFactory;", "app_debug"})
    @dagger.hilt.EntryPoint()
    @dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
    public static abstract interface HiltWorkManagerInitializerEntryPoint {
        
        @org.jetbrains.annotations.NotNull()
        public abstract androidx.hilt.work.HiltWorkerFactory hiltWorkerFactory();
    }
}