package com.bes2.app.ui.settings

import android.content.IntentSender
import android.content.Context
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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

sealed class SettingsEvent {
    data class SyncCompleted(val message: String) : SettingsEvent()
    object SyncFailed : SettingsEvent()
}

data class SettingsUiState(
    val syncOption: String = "WIFI_ONLY",
    val uploadOnWifiOnly: Boolean = true,
    val syncDelayHours: Int = 2,
    val syncDelayMinutes: Int = 0,
    val syncTime: LocalTime = LocalTime.of(2, 0),
    val isSyncing: Boolean = false,
    val lastSyncTime: Long = 0,
    val syncedCount: Int = 0,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val workManager: WorkManager,
    private val googlePhotosAuthManager: GooglePhotosAuthManager, // [FIX] Inject AuthManager
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events

    init {
        loadSettings()
        monitorSyncStatus()
        checkLoginStatus()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.storedSettings.collectLatest { settings ->
                _uiState.update { 
                    it.copy(
                        syncOption = settings.syncOption,
                        uploadOnWifiOnly = settings.uploadOnWifiOnly,
                        syncDelayHours = settings.syncDelayHours,
                        syncDelayMinutes = settings.syncDelayMinutes
                    ) 
                }
            }
        }
    }
    
    // [FIX] Observe login status from AuthManager
    private fun checkLoginStatus() {
        viewModelScope.launch {
            googlePhotosAuthManager.account.collectLatest { account ->
                _uiState.update { it.copy(isLoggedIn = account != null) }
            }
        }
    }

    fun onSyncOptionChanged(option: String) {
        viewModelScope.launch { settingsRepository.saveSyncOption(option) } 
    }

    fun onUploadOnWifiOnlyChanged(wifiOnly: Boolean) {
        viewModelScope.launch { settingsRepository.saveUploadOnWifiOnly(wifiOnly) } 
    }
    
    fun setSyncTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(syncTime = LocalTime.of(hour, minute)) }
        viewModelScope.launch { settingsRepository.saveSyncTime(LocalTime.of(hour, minute)) }
    }
    
    fun setSyncDelay(hours: Int, minutes: Int) {
        viewModelScope.launch { settingsRepository.saveSyncDelay(hours, minutes) } 
    }

    fun onManualSyncClicked() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val workRequest = OneTimeWorkRequestBuilder<DailyCloudSyncWorker>()
            .setConstraints(constraints)
            .build()
            
        workManager.enqueueUniqueWork(DailyCloudSyncWorker.WORK_NAME, ExistingWorkPolicy.KEEP, workRequest)
    }
    
    // [FIX] Delegate to AuthManager
    suspend fun beginGoogleSignIn(): IntentSender? {
        return googlePhotosAuthManager.beginSignIn()
    }
    
    // [FIX] Delegate to AuthManager
    fun handleGoogleSignInResult(result: ActivityResult) {
        viewModelScope.launch {
            googlePhotosAuthManager.handleSignInResult(result)
        }
    }
    
    // [FIX] Delegate to AuthManager
    fun onLogoutClicked() {
        viewModelScope.launch {
            googlePhotosAuthManager.signOut()
        }
    }

    private fun monitorSyncStatus() {
        workManager.getWorkInfosForUniqueWorkLiveData(DailyCloudSyncWorker.WORK_NAME)
            .observeForever { workInfos ->
                val workInfo = workInfos?.firstOrNull()
                if (workInfo != null) {
                    val isRunning = workInfo.state == WorkInfo.State.RUNNING
                    
                    _uiState.update { it.copy(isSyncing = isRunning) }
                    
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                         viewModelScope.launch { _events.emit(SettingsEvent.SyncCompleted("동기화 완료")) }
                    }
                }
            }
    }
}
