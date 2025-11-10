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

/**
 * Represents the state of the Settings screen.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B#\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\t\u0010\u000e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000f\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0007H\u00c6\u0003J\'\u0010\u0011\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010\u0012\u001a\u00020\u00032\b\u0010\u0013\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0014\u001a\u00020\u0015H\u00d6\u0001J\t\u0010\u0016\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r\u00a8\u0006\u0017"}, d2 = {"Lcom/bes2/app/ui/settings/SettingsUiState;", "", "isLoggedIn", "", "selectedProvider", "", "syncTime", "Ljava/time/LocalTime;", "(ZLjava/lang/String;Ljava/time/LocalTime;)V", "()Z", "getSelectedProvider", "()Ljava/lang/String;", "getSyncTime", "()Ljava/time/LocalTime;", "component1", "component2", "component3", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
public final class SettingsUiState {
    private final boolean isLoggedIn = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String selectedProvider = null;
    @org.jetbrains.annotations.NotNull()
    private final java.time.LocalTime syncTime = null;
    
    public SettingsUiState(boolean isLoggedIn, @org.jetbrains.annotations.NotNull()
    java.lang.String selectedProvider, @org.jetbrains.annotations.NotNull()
    java.time.LocalTime syncTime) {
        super();
    }
    
    public final boolean isLoggedIn() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSelectedProvider() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.time.LocalTime getSyncTime() {
        return null;
    }
    
    public SettingsUiState() {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.time.LocalTime component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.bes2.app.ui.settings.SettingsUiState copy(boolean isLoggedIn, @org.jetbrains.annotations.NotNull()
    java.lang.String selectedProvider, @org.jetbrains.annotations.NotNull()
    java.time.LocalTime syncTime) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}