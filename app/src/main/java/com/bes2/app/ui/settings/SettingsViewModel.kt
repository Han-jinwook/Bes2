package com.bes2.app.ui.settings

import android.accounts.Account
import android.content.Context
import android.content.IntentSender
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bes2.app.R
import com.bes2.background.worker.DailyCloudSyncWorker
import com.bes2.data.repository.SettingsRepository
import com.bes2.photos_integration.auth.GooglePhotosAuthManager
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    val syncTime: LocalTime = LocalTime.of(2, 0),
    val isSyncing: Boolean = false,
    val uploadOnWifiOnly: Boolean = true
)

sealed interface SettingsEvent {
    data class SyncCompleted(val message: String) : SettingsEvent
    object SyncFailed : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authManager: GooglePhotosAuthManager,
    private val workManager: WorkManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events = _events.asSharedFlow()

    init {
        Timber.tag(DEBUG_TAG).d("[ViewModel] Initializing.")
        authManager.account
            .onEach { account ->
                val isLoggedIn = account != null
                Timber.tag(DEBUG_TAG).d("[ViewModel] AuthManager account state changed. Logged in: $isLoggedIn")
                _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
                if (isLoggedIn) {
                    scheduleDailySync()
                } else {
                    workManager.cancelUniqueWork(DailyCloudSyncWorker.WORK_NAME)
                }
            }
            .launchIn(viewModelScope)

        settingsRepository.storedSettings
            .onEach { settings ->
                _uiState.update { it.copy(
                    syncTime = settings.syncTime,
                    selectedProvider = settings.provider,
                    uploadOnWifiOnly = settings.uploadOnWifiOnly
                ) }
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
            scheduleDailySync() // Reschedule with the new time
        }
    }

    fun onUploadOnWifiOnlyChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveUploadOnWifiOnly(enabled)
            scheduleDailySync() // Reschedule with the new constraint
        }
    }

    private fun scheduleDailySync() {
        if (!_uiState.value.isLoggedIn) {
            Timber.d("User not logged in. Sync scheduling cancelled.")
            return
        }

        val constraints = if (_uiState.value.uploadOnWifiOnly) {
            Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()
        } else {
            Constraints.NONE
        }

        val syncTime = _uiState.value.syncTime
        val now = ZonedDateTime.now()
        var nextSync = now.withHour(syncTime.hour).withMinute(syncTime.minute).withSecond(0)

        if (nextSync.isBefore(now) || nextSync.isEqual(now)) {
            nextSync = nextSync.plusDays(1)
        }

        val initialDelay = Duration.between(now, nextSync).toMillis()

        val syncWorkRequest = OneTimeWorkRequestBuilder<DailyCloudSyncWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            DailyCloudSyncWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )

        Timber.d("Daily sync (one-time) scheduled for ${nextSync.toLocalTime()}. Will run in ${TimeUnit.MILLISECONDS.toMinutes(initialDelay)} minutes. Wi-Fi only: ${_uiState.value.uploadOnWifiOnly}")
    }

    fun onManualSyncClicked() {
        if (_uiState.value.isSyncing) return
        
        Timber.d("Manual sync triggered.")
        _uiState.update { it.copy(isSyncing = true) }
        
        val constraints = if (_uiState.value.uploadOnWifiOnly) {
            Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()
        } else {
            Constraints.NONE
        }

        val syncWorkRequest = OneTimeWorkRequestBuilder<DailyCloudSyncWorker>()
            .setConstraints(constraints)
            .build()
            
        val workId = syncWorkRequest.id
        workManager.enqueue(syncWorkRequest)

        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(workId).collect { workInfo ->
                if (workInfo.state.isFinished) {
                    _uiState.update { it.copy(isSyncing = false) }
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val syncedCount = workInfo.outputData.getInt(DailyCloudSyncWorker.KEY_SYNCED_COUNT, 0)
                            val message = if (syncedCount > 0) {
                                "베스트 사진 $syncedCount 장이 백업되었습니다."
                            } else {
                                "백업할 새로운 베스트 사진이 없습니다."
                            }
                            _events.emit(SettingsEvent.SyncCompleted(message))
                        }
                        else -> {
                             _events.emit(SettingsEvent.SyncFailed)
                        }
                    }
                }
            }
        }
    }
}
