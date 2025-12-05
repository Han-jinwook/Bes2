package com.bes2.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class StoredSettings(
    val syncTime: LocalTime,
    val cloudStorageProvider: String,
    val uploadOnWifiOnly: Boolean,
    val syncOption: String,
    val syncDelayHours: Int,
    val syncDelayMinutes: Int
)

data class DailyActivityStats(
    val keptCount: Int,
    val deletedCount: Int
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val SYNC_HOUR = intPreferencesKey("sync_hour")
        val SYNC_MINUTE = intPreferencesKey("sync_minute")
        val CLOUD_PROVIDER = stringPreferencesKey("cloud_provider")
        val UPLOAD_ON_WIFI_ONLY = booleanPreferencesKey("upload_on_wifi_only")
        val SYNC_OPTION = stringPreferencesKey("sync_option")
        val SYNC_DELAY_HOURS = intPreferencesKey("sync_delay_hours")
        val SYNC_DELAY_MINUTES = intPreferencesKey("sync_delay_minutes")
        val APP_START_TIME = longPreferencesKey("app_start_time")
        
        val TODAY_KEPT_COUNT = intPreferencesKey("today_kept_count")
        val TODAY_DELETED_COUNT = intPreferencesKey("today_deleted_count")
        val LAST_STATS_DATE = stringPreferencesKey("last_stats_date") 
        val LAST_DIET_SCAN_TIME = longPreferencesKey("last_diet_scan_time")
        
        // [DELETED] Keys for notification throttling are no longer needed.
        // val LAST_NOTI_TIME_DIET = longPreferencesKey("last_noti_time_diet")
        // val LAST_NOTI_TIME_INSTANT = longPreferencesKey("last_noti_time_instant")
        // val LAST_NOTI_TIME_TRASH = longPreferencesKey("last_noti_time_trash")
        
        val ANALYSIS_PROGRESS_CURRENT = intPreferencesKey("analysis_progress_current")
        val ANALYSIS_PROGRESS_TOTAL = intPreferencesKey("analysis_progress_total")
    }

    val storedSettings: Flow<StoredSettings> = context.dataStore.data
        .map { preferences ->
            val hour = preferences[PreferencesKeys.SYNC_HOUR] ?: 2
            val minute = preferences[PreferencesKeys.SYNC_MINUTE] ?: 0
            val provider = preferences[PreferencesKeys.CLOUD_PROVIDER] ?: "google_photos"
            val uploadOnWifiOnly = preferences[PreferencesKeys.UPLOAD_ON_WIFI_ONLY] ?: true
            val syncOption = preferences[PreferencesKeys.SYNC_OPTION] ?: "IMMEDIATE"
            val syncDelayHours = preferences[PreferencesKeys.SYNC_DELAY_HOURS] ?: 0
            val syncDelayMinutes = preferences[PreferencesKeys.SYNC_DELAY_MINUTES] ?: 5
            
            StoredSettings(
                syncTime = LocalTime.of(hour, minute),
                cloudStorageProvider = provider,
                uploadOnWifiOnly = uploadOnWifiOnly,
                syncOption = syncOption,
                syncDelayHours = syncDelayHours,
                syncDelayMinutes = syncDelayMinutes
            )
        }
    
    val dailyStats: Flow<DailyActivityStats> = context.dataStore.data
        .map { prefs ->
            val lastDate = prefs[PreferencesKeys.LAST_STATS_DATE] ?: ""
            val today = LocalDate.now().toString()
            
            if (lastDate != today) {
                DailyActivityStats(0, 0)
            } else {
                DailyActivityStats(
                    keptCount = prefs[PreferencesKeys.TODAY_KEPT_COUNT] ?: 0,
                    deletedCount = prefs[PreferencesKeys.TODAY_DELETED_COUNT] ?: 0
                )
            }
        }
        
    val analysisProgress: Flow<Pair<Int, Int>> = context.dataStore.data
        .map { prefs ->
            val current = prefs[PreferencesKeys.ANALYSIS_PROGRESS_CURRENT] ?: 0
            val total = prefs[PreferencesKeys.ANALYSIS_PROGRESS_TOTAL] ?: 0
            Pair(current, total)
        }

    suspend fun incrementDailyStats(keptDelta: Int, deletedDelta: Int) {
        context.dataStore.edit { prefs ->
            val lastDate = prefs[PreferencesKeys.LAST_STATS_DATE] ?: ""
            val today = LocalDate.now().toString()
            
            if (lastDate != today) {
                prefs[PreferencesKeys.LAST_STATS_DATE] = today
                prefs[PreferencesKeys.TODAY_KEPT_COUNT] = keptDelta
                prefs[PreferencesKeys.TODAY_DELETED_COUNT] = deletedDelta
            } else {
                val currentKept = prefs[PreferencesKeys.TODAY_KEPT_COUNT] ?: 0
                val currentDeleted = prefs[PreferencesKeys.TODAY_DELETED_COUNT] ?: 0
                prefs[PreferencesKeys.TODAY_KEPT_COUNT] = currentKept + keptDelta
                prefs[PreferencesKeys.TODAY_DELETED_COUNT] = currentDeleted + deletedDelta
            }
        }
    }

    suspend fun saveSyncTime(time: LocalTime) {
        context.dataStore.edit { it[PreferencesKeys.SYNC_HOUR] = time.hour; it[PreferencesKeys.SYNC_MINUTE] = time.minute }
    }

    suspend fun saveCloudProvider(providerKey: String) {
        context.dataStore.edit { it[PreferencesKeys.CLOUD_PROVIDER] = providerKey }
    }
    
    suspend fun saveUploadOnWifiOnly(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.UPLOAD_ON_WIFI_ONLY] = enabled }
    }

    suspend fun saveSyncOption(option: String) {
        context.dataStore.edit { it[PreferencesKeys.SYNC_OPTION] = option }
    }

    suspend fun saveSyncDelay(hours: Int, minutes: Int) {
        context.dataStore.edit { it[PreferencesKeys.SYNC_DELAY_HOURS] = hours; it[PreferencesKeys.SYNC_DELAY_MINUTES] = minutes }
    }
    
    suspend fun saveAppStartTime(timestamp: Long) {
        context.dataStore.edit { it[PreferencesKeys.APP_START_TIME] = timestamp }
    }
    
    suspend fun getAppStartTime(): Long {
        val prefs = context.dataStore.data.first()
        return prefs[PreferencesKeys.APP_START_TIME] ?: System.currentTimeMillis()
    }
    
    suspend fun getLastDietScanTime(): Long? {
        val prefs = context.dataStore.data.first()
        return prefs[PreferencesKeys.LAST_DIET_SCAN_TIME]
    }
    
    suspend fun saveLastDietScanTime(timestamp: Long) {
        context.dataStore.edit { it[PreferencesKeys.LAST_DIET_SCAN_TIME] = timestamp }
    }
    
    // [MODIFIED] Always allow notifications, removing the time check.
    fun shouldShowNotification(): Boolean {
        return true
    }
    
    // [DELETED] No longer needed as we always show notifications.
    // suspend fun updateLastNotificationTime(sourceType: String) { ... }
    
    suspend fun setTotalScanCount(total: Int) {
        context.dataStore.edit { it[PreferencesKeys.ANALYSIS_PROGRESS_TOTAL] = total }
    }
    
    suspend fun updateCurrentAnalysisProgress(current: Int) {
        context.dataStore.edit { it[PreferencesKeys.ANALYSIS_PROGRESS_CURRENT] = current }
    }
    
    suspend fun resetAnalysisProgress() {
        context.dataStore.edit { 
            it[PreferencesKeys.ANALYSIS_PROGRESS_CURRENT] = 0
            it[PreferencesKeys.ANALYSIS_PROGRESS_TOTAL] = 0
        }
    }
}
