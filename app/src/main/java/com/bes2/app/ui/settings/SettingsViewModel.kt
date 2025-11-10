package com.bes2.app.ui.settings

import android.accounts.Account
import android.content.Context
import android.content.IntentSender
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bes2.app.R
import com.bes2.background.worker.DailyCloudSyncWorker
import com.bes2.data.repository.SettingsRepository
import com.bes2.photos_integration.auth.GooglePhotosAuthManager
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val DEBUG_TAG = "AuthFlowDebug"

data class SettingsUiState(
    val isLoggedIn: Boolean = false,
    val selectedProvider: String = "google_photos",
    val syncTime: LocalTime = LocalTime.of(2, 0)
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authManager: GooglePhotosAuthManager,
    private val workManager: WorkManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        Timber.tag(DEBUG_TAG).d("[ViewModel] Initializing.")
        // Reactively observe the login state from the auth manager.
        authManager.account
            .onEach { account ->
                val isLoggedIn = account != null
                Timber.tag(DEBUG_TAG).d("[ViewModel] AuthManager account state changed. Logged in: $isLoggedIn")
                _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
                // Re-evaluate scheduling whenever login state changes
                if (isLoggedIn) {
                    scheduleDailySync()
                } else {
                    workManager.cancelUniqueWork(DailyCloudSyncWorker.WORK_NAME)
                }
            }
            .launchIn(viewModelScope)

        settingsRepository.storedSettings
            .onEach { (time, provider) ->
                _uiState.update { it.copy(syncTime = time, selectedProvider = provider) }
            }
            .launchIn(viewModelScope)
    }

    suspend fun beginSignIn(): IntentSender? {
        Timber.tag(DEBUG_TAG).d("[ViewModel] beginSignIn called.")
        val request = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()

        return try {
            authManager.oneTapClient.beginSignIn(request).await().pendingIntent.intentSender
        } catch (e: Exception) {
            Timber.tag(DEBUG_TAG).e(e, "[ViewModel] beginSignIn failed.")
            null
        }
    }

    fun handleSignInResult(result: ActivityResult) {
        Timber.tag(DEBUG_TAG).d("[ViewModel] handleSignInResult called. Result code: ${result.resultCode}")
        try {
            val credential = authManager.oneTapClient.getSignInCredentialFromIntent(result.data)
            val account = Account(credential.id, "com.google")
            Timber.tag(DEBUG_TAG).i("[ViewModel] Sign-in credential obtained. Setting account in AuthManager.")
            authManager.setAccount(account)
        } catch (e: ApiException) {
            Timber.tag(DEBUG_TAG).e(e, "[ViewModel] Sign-in failed with ApiException.")
            authManager.setAccount(null)
        } catch (e: Exception) {
            Timber.tag(DEBUG_TAG).e(e, "[ViewModel] An unexpected error occurred during sign-in.")
            authManager.setAccount(null)
        }
    }

    fun onLogoutClicked() {
        Timber.tag(DEBUG_TAG).d("[ViewModel] onLogoutClicked called.")
        viewModelScope.launch {
            authManager.signOut()
        }
    }

    fun setSyncTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val newTime = LocalTime.of(hour, minute)
            settingsRepository.saveSyncTime(newTime)
            scheduleDailySync()
        }
    }

    private fun scheduleDailySync() {
        if (!_uiState.value.isLoggedIn) {
            Timber.d("User not logged in. Sync scheduling cancelled.")
            return
        }

        val syncTime = _uiState.value.syncTime
        val now = ZonedDateTime.now()
        var nextSync = now.withHour(syncTime.hour).withMinute(syncTime.minute).withSecond(0)

        if (nextSync.isBefore(now) || nextSync.isEqual(now)) {
            nextSync = nextSync.plusDays(1)
        }

        val initialDelay = Duration.between(now, nextSync).toMillis()

        val syncWorkRequest = PeriodicWorkRequestBuilder<DailyCloudSyncWorker>(
            24, TimeUnit.HOURS
        ).setInitialDelay(initialDelay, TimeUnit.MILLISECONDS).build()

        workManager.enqueueUniquePeriodicWork(
            DailyCloudSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            syncWorkRequest
        )

        Timber.d("Daily sync scheduled for ${nextSync.toLocalTime()}. Will run in ${TimeUnit.MILLISECONDS.toMinutes(initialDelay)} minutes.")
    }

    fun onManualSyncClicked() {
        Timber.d("Manual sync triggered.")
        val syncWorkRequest = OneTimeWorkRequestBuilder<DailyCloudSyncWorker>().build()
        workManager.enqueue(syncWorkRequest)
    }
}
