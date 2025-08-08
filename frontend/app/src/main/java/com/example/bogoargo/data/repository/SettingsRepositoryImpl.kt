package com.example.bogoargo.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.DataException
import com.example.bogoargo.domain.model.Settings
import com.example.bogoargo.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

// Extension property to get DataStore instance
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_preferences")

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ISettingsRepository {
    
    // Preference keys
    private companion object {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val LANGUAGE = stringPreferencesKey("language")
    }
    
    override fun getSettings(): Flow<Settings> = context.settingsDataStore.data.map { preferences ->
        Settings(
            notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
            darkModeEnabled = preferences[DARK_MODE_ENABLED] ?: false,
            language = preferences[LANGUAGE] ?: "English"
        )
    }
    
    override suspend fun updateNotificationsEnabled(enabled: Boolean): DataResult<Unit> {
        return try {
            context.settingsDataStore.edit { preferences ->
                preferences[NOTIFICATIONS_ENABLED] = enabled
            }
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Failed to update notifications setting"))
        }
    }
    
    override suspend fun updateDarkModeEnabled(enabled: Boolean): DataResult<Unit> {
        return try {
            context.settingsDataStore.edit { preferences ->
                preferences[DARK_MODE_ENABLED] = enabled
            }
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Failed to update dark mode setting"))
        }
    }
    
    override suspend fun updateLanguage(language: String): DataResult<Unit> {
        return try {
            context.settingsDataStore.edit { preferences ->
                preferences[LANGUAGE] = language
            }
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Failed to update language setting"))
        }
    }
    
    override suspend fun clearAllSettings(): DataResult<Unit> {
        return try {
            context.settingsDataStore.edit { preferences ->
                preferences.clear()
            }
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Failed to clear settings"))
        }
    }
}