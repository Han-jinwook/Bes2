package com.bes2.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Define a data class for settings for better organization
data class StoredSettings(
    val syncTime: LocalTime,
    val provider: String,
    val uploadOnWifiOnly: Boolean
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
    }

    val storedSettings: Flow<StoredSettings> = context.dataStore.data
        .map { preferences ->
            val hour = preferences[PreferencesKeys.SYNC_HOUR] ?: 2 // Default to 2 AM
            val minute = preferences[PreferencesKeys.SYNC_MINUTE] ?: 0
            val provider = preferences[PreferencesKeys.CLOUD_PROVIDER] ?: "google_photos"
            val uploadOnWifiOnly = preferences[PreferencesKeys.UPLOAD_ON_WIFI_ONLY] ?: true // Default to true
            
            StoredSettings(
                syncTime = LocalTime.of(hour, minute),
                provider = provider,
                uploadOnWifiOnly = uploadOnWifiOnly
            )
        }

    suspend fun saveSyncTime(time: LocalTime) {
        context.dataStore.edit {
            it[PreferencesKeys.SYNC_HOUR] = time.hour
            it[PreferencesKeys.SYNC_MINUTE] = time.minute
        }
    }

    suspend fun saveCloudProvider(providerKey: String) {
        context.dataStore.edit {
            it[PreferencesKeys.CLOUD_PROVIDER] = providerKey
        }
    }
    
    suspend fun saveUploadOnWifiOnly(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.UPLOAD_ON_WIFI_ONLY] = enabled
        }
    }
}
