package com.bes2.app.ui.settings

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
import com.bes2.background.worker.DailyCloudSyncWorker
import com.bes2.data.repository.SettingsRepository
import com.bes2.photos_integration.auth.GooglePhotosAuthManager
import com.bes2.photos_integration.auth.NaverMyBoxAuthManager
import com.navercorp.nid.oauth.OAuthLoginCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class SettingsUiState(
    val isLoggedIn: Boolean = false,
    val selectedProvider: String = "google_photos",
    val syncTime: LocalTime = LocalTime.of(2, 0),
    val uploadOnWifiOnly: Boolean = false,
    val isSyncing: Boolean = false
)

sealed interface SettingsEvent {
    data class SyncCompleted(val message: String) : SettingsEvent
    object SyncFailed : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val googleAuthManager: GooglePhotosAuthManager,
    private val naverAuthManager: NaverMyBoxAuthManager,
    private val workManager: WorkManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events = _events.asSharedFlow()

    init {
        settingsRepository.storedSettings
            .onEach { settings ->
                _uiState.update { it.copy(
                    syncTime = settings.syncTime,
                    selectedProvider = settings.provider,
                    uploadOnWifiOnly = settings.uploadOnWifiOnly
                ) }
            }
            .launchIn(viewModelScope)

        googleAuthManager.account.combine(naverAuthManager.account) { googleAcct, naverAcct ->
            googleAcct != null || naverAcct != null
        }.onEach { isLoggedIn ->
            _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
            if (isLoggedIn) {
                scheduleDailySync()
            } else {
                workManager.cancelUniqueWork(DailyCloudSyncWorker.WORK_NAME)
            }
        }.launchIn(viewModelScope)
    }

    suspend fun beginGoogleSignIn(): IntentSender? {
        return googleAuthManager.beginSignIn()
    }

    fun handleGoogleSignInResult(result: ActivityResult) {
        googleAuthManager.handleSignInResult(result)
    }

    fun getNaverLoginCallback(): OAuthLoginCallback {
        return naverAuthManager.oauthLoginCallback
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            when (_uiState.value.selectedProvider) {
                "google_photos" -> googleAuthManager.signOut()
                "naver_mybox" -> naverAuthManager.signOut()
            }
        }
    }

    fun onProviderSelected(providerKey: String) {
        viewModelScope.launch {
            if (_uiState.value.isLoggedIn) {
                onLogoutClicked()
            }
            settingsRepository.saveCloudProvider(providerKey)
        }
    }

    fun setSyncTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val newTime = LocalTime.of(hour, minute)
            settingsRepository.saveSyncTime(newTime)
            scheduleDailySync()
        }
    }

    fun onUploadOnWifiOnlyChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveUploadOnWifiOnly(enabled)
            scheduleDailySync()
        }
    }

    private fun scheduleDailySync() {
        if (!_uiState.value.isLoggedIn) return

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
        Timber.d("Daily sync scheduled for ${nextSync.toLocalTime()}. Wi-Fi only: ${_uiState.value.uploadOnWifiOnly}")
    }

    fun onManualSyncClicked() {
        if (_uiState.value.isSyncing) return
        
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
                            val message = if (syncedCount > 0) "베스트 사진 $syncedCount 장이 백업되었습니다." else "백업할 새로운 베스트 사진이 없습니다."
                            _events.emit(SettingsEvent.SyncCompleted(message))
                        }
                        else -> _events.emit(SettingsEvent.SyncFailed)
                    }
                }
            }
        }
    }
}
