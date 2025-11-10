package com.bes2.app.ui.settings;

import android.accounts.Account;
import android.content.Context;
import android.content.IntentSender;
import android.util.Log;
import androidx.activity.result.ActivityResult;
import androidx.lifecycle.ViewModel;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.WorkManager;
import com.bes2.app.R;
import com.bes2.background.worker.DailyCloudSyncWorker;
import com.bes2.data.repository.SettingsRepository;
import com.bes2.photos_integration.auth.GooglePhotosAuthManager;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.common.api.ApiException;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import timber.log.Timber;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B)\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0001\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u0010\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u0086@\u00a2\u0006\u0002\u0010\u0014J\u000e\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0018J\u0006\u0010\u0019\u001a\u00020\u0016J\u0006\u0010\u001a\u001a\u00020\u0016J\u0006\u0010\u001b\u001a\u00020\u0016J\b\u0010\u001c\u001a\u00020\u0016H\u0002J\u0016\u0010\u001d\u001a\u00020\u00162\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u001fR\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\r0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006!"}, d2 = {"Lcom/bes2/app/ui/settings/SettingsViewModel;", "Landroidx/lifecycle/ViewModel;", "settingsRepository", "Lcom/bes2/data/repository/SettingsRepository;", "authManager", "Lcom/bes2/photos_integration/auth/GooglePhotosAuthManager;", "workManager", "Landroidx/work/WorkManager;", "context", "Landroid/content/Context;", "(Lcom/bes2/data/repository/SettingsRepository;Lcom/bes2/photos_integration/auth/GooglePhotosAuthManager;Landroidx/work/WorkManager;Landroid/content/Context;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/bes2/app/ui/settings/SettingsUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "beginSignIn", "Landroid/content/IntentSender;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "handleSignInResult", "", "result", "Landroidx/activity/result/ActivityResult;", "onLogoutClicked", "onManualSyncClicked", "onTimeChangeClicked", "scheduleDailySync", "setSyncTime", "hour", "", "minute", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class SettingsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.bes2.data.repository.SettingsRepository settingsRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.bes2.photos_integration.auth.GooglePhotosAuthManager authManager = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.work.WorkManager workManager = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.bes2.app.ui.settings.SettingsUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.bes2.app.ui.settings.SettingsUiState> uiState = null;
    
    @javax.inject.Inject()
    public SettingsViewModel(@org.jetbrains.annotations.NotNull()
    com.bes2.data.repository.SettingsRepository settingsRepository, @org.jetbrains.annotations.NotNull()
    com.bes2.photos_integration.auth.GooglePhotosAuthManager authManager, @org.jetbrains.annotations.NotNull()
    androidx.work.WorkManager workManager, @dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.bes2.app.ui.settings.SettingsUiState> getUiState() {
        return null;
    }
    
    /**
     * Initiates the Google Sign-In flow using the One Tap API.
     * Returns an IntentSender to be launched by the UI.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object beginSignIn(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super android.content.IntentSender> $completion) {
        return null;
    }
    
    /**
     * Handles the result from the One Tap sign-in flow.
     */
    public final void handleSignInResult(@org.jetbrains.annotations.NotNull()
    androidx.activity.result.ActivityResult result) {
    }
    
    public final void onLogoutClicked() {
    }
    
    public final void onTimeChangeClicked() {
    }
    
    public final void setSyncTime(int hour, int minute) {
    }
    
    private final void scheduleDailySync() {
    }
    
    public final void onManualSyncClicked() {
    }
}