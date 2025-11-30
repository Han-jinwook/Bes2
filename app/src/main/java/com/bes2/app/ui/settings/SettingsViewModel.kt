package com.bes2.app.ui.settings

import android.content.IntentSender // [FIX] Correct package
import android.content.Context
import android.content.Intent
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
    
    private fun checkLoginStatus() {
        _uiState.update { it.copy(isLoggedIn = false) }
    }

    fun onSyncOptionChanged(option: String) {
        // viewModelScope.launch { settingsRepository.setSyncOption(option) } 
    }

    fun onUploadOnWifiOnlyChanged(wifiOnly: Boolean) {
        // viewModelScope.launch { settingsRepository.setUploadOnWifiOnly(wifiOnly) } 
    }
    
    fun setSyncTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(syncTime = LocalTime.of(hour, minute)) }
    }
    
    fun setSyncDelay(hours: Int, minutes: Int) {
        // viewModelScope.launch { settingsRepository.setSyncDelay(hours, minutes) } 
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
    
    fun beginGoogleSignIn(): IntentSender? {
        return null 
    }
    
    fun handleGoogleSignInResult(result: ActivityResult) {
        _uiState.update { it.copy(isLoggedIn = true) }
    }
    
    fun onLogoutClicked() {
        _uiState.update { it.copy(isLoggedIn = false) }
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
