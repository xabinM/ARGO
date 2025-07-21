package com.example.bogoargo.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val language: String = "English",
    val version: String = "1.0.0",
    val showLogoutDialog: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application)
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    notificationsEnabled = settings.notificationsEnabled,
                    darkModeEnabled = settings.darkModeEnabled,
                    language = settings.language
                )
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateNotificationsSetting(enabled)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateDarkModeSetting(enabled)
            // TODO: Apply theme change to the app
        }
    }

    fun changeLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.updateLanguageSetting(language)
            // TODO: Apply language change to the app
        }
    }

    fun showLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = true)
    }

    fun dismissLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = false)
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showLogoutDialog = false)
            // Clear settings on logout
            settingsRepository.clearSettings()
            // TODO: Clear user data and navigate to login
        }
    }
}