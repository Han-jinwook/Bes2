package com.bes2.app.ui.settings

import android.content.Context
import android.content.IntentSender
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo // Added missing import
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
    val isSyncing: Boolean = false,
    val syncOption: String = "DAILY", // DAILY, IMMEDIATE, DELAYED, NONE
    val syncDelayHours: Int = 0,
    val syncDelayMinutes: Int = 5
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
                    uploadOnWifiOnly = settings.uploadOnWifiOnly,
                    syncOption = settings.syncOption,
                    syncDelayHours = settings.syncDelayHours,
                    syncDelayMinutes = settings.syncDelayMinutes
                ) }
                // Schedule daily sync only if it's the selected option
                if (settings.syncOption == "DAILY") {
                    scheduleDailySync()
                }
            }
            .launchIn(viewModelScope)

        googleAuthManager.account.combine(naverAuthManager.account) { googleAcct, naverAcct ->
            googleAcct != null || naverAcct != null
        }.onEach { isLoggedIn ->
            _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
            if (!isLoggedIn) {
                workManager.cancelUniqueWork(DailyCloudSyncWorker.WORK_NAME)
            }
        }.launchIn(viewModelScope)
    }

    suspend fun beginGoogleSignIn(): IntentSender? = googleAuthManager.beginSignIn()
    fun handleGoogleSignInResult(result: ActivityResult) = googleAuthManager.handleSignInResult(result)
    fun getNaverLoginCallback(): OAuthLoginCallback = naverAuthManager.oauthLoginCallback

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
            if (_uiState.value.isLoggedIn) { onLogoutClicked() }
            settingsRepository.saveCloudProvider(providerKey)
        }
    }

    fun setSyncTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.saveSyncTime(LocalTime.of(hour, minute))
            scheduleDailySync() // Re-schedule with new time
        }
    }

    fun onUploadOnWifiOnlyChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveUploadOnWifiOnly(enabled)
            if (_uiState.value.syncOption == "DAILY") {
                scheduleDailySync() // Re-schedule with new constraints
            }
        }
    }
    
    fun onSyncOptionChanged(option: String) {
        viewModelScope.launch {
            settingsRepository.saveSyncOption(option)
            if (option == "DAILY") {
                scheduleDailySync()
            } else {
                workManager.cancelUniqueWork(DailyCloudSyncWorker.WORK_NAME)
                Timber.d("Daily sync cancelled as option changed to $option.")
            }
        }
    }

    fun setSyncDelay(hours: Int, minutes: Int) {
        viewModelScope.launch {
            settingsRepository.saveSyncDelay(hours, minutes)
        }
    }

    private fun scheduleDailySync() {
        if (!_uiState.value.isLoggedIn) return

        val constraints = if (_uiState.value.uploadOnWifiOnly) {
            Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()
        } else { Constraints.NONE }

        val syncTime = _uiState.value.syncTime
        val now = ZonedDateTime.now()
        var nextSync = now.withHour(syncTime.hour).withMinute(syncTime.minute).withSecond(0)
        if (nextSync.isBefore(now) || nextSync.isEqual(now)) {
            nextSync = nextSync.plusDays(1)
        }

        val initialDelay = Duration.between(now, nextSync).toMillis()
        val inputData = Data.Builder().putBoolean(DailyCloudSyncWorker.KEY_IS_ONE_TIME_SYNC, false).build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<DailyCloudSyncWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setInputData(inputData)
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
        } else { Constraints.NONE }
        
        val inputData = Data.Builder().putBoolean(DailyCloudSyncWorker.KEY_IS_ONE_TIME_SYNC, true).build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<DailyCloudSyncWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()
            
        workManager.enqueue(syncWorkRequest)

        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(syncWorkRequest.id).collect { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    _uiState.update { it.copy(isSyncing = false) }
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val count = workInfo.outputData.getInt(DailyCloudSyncWorker.KEY_SYNCED_COUNT, 0)
                            val message = if (count > 0) "베스트 사진 $count 장이 백업되었습니다." else "백업할 새로운 베스트 사진이 없습니다."
                            _events.emit(SettingsEvent.SyncCompleted(message))
                        }
                        else -> _events.emit(SettingsEvent.SyncFailed)
                    }
                }
            }
        }
    }
}
