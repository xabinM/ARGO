package com.example.bogoargo.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bogoargo.domain.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to get DataStore instance
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_preferences")

class SettingsRepository(private val context: Context) {
    
    // Preference keys
    private companion object {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val LANGUAGE = stringPreferencesKey("language")
    }
    
    // Get settings data as Flow
    val settingsFlow: Flow<Settings> = context.settingsDataStore.data.map { preferences ->
        Settings(
            notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
            darkModeEnabled = preferences[DARK_MODE_ENABLED] ?: false,
            language = preferences[LANGUAGE] ?: "English"
        )
    }
    
    // Update notifications setting
    suspend fun updateNotificationsSetting(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }
    
    // Update dark mode setting
    suspend fun updateDarkModeSetting(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[DARK_MODE_ENABLED] = enabled
        }
    }
    
    // Update language setting
    suspend fun updateLanguageSetting(language: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[LANGUAGE] = language
        }
    }
    
    // Save all settings at once
    suspend fun saveSettings(settings: Settings) {
        context.settingsDataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = settings.notificationsEnabled
            preferences[DARK_MODE_ENABLED] = settings.darkModeEnabled
            preferences[LANGUAGE] = settings.language
        }
    }
    
    // Clear all settings (reset to defaults)
    suspend fun clearSettings() {
        context.settingsDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}