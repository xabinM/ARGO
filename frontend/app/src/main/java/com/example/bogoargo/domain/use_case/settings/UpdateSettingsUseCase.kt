package com.example.bogoargo.domain.use_case.settings

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.ISettingsRepository
import javax.inject.Inject

class UpdateSettingsUseCase @Inject constructor(
    private val settingsRepository: ISettingsRepository
) {
    suspend fun updateNotificationsEnabled(enabled: Boolean): DataResult<Unit> {
        return settingsRepository.updateNotificationsEnabled(enabled)
    }
    
    suspend fun updateDarkModeEnabled(enabled: Boolean): DataResult<Unit> {
        return settingsRepository.updateDarkModeEnabled(enabled)
    }
    
    suspend fun updateLanguage(language: String): DataResult<Unit> {
        return settingsRepository.updateLanguage(language)
    }
    
    suspend fun clearAllSettings(): DataResult<Unit> {
        return settingsRepository.clearAllSettings()
    }
}