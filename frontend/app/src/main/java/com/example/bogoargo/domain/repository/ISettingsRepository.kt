package com.example.bogoargo.domain.repository

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    fun getSettings(): Flow<Settings>
    suspend fun updateNotificationsEnabled(enabled: Boolean): DataResult<Unit>
    suspend fun updateDarkModeEnabled(enabled: Boolean): DataResult<Unit>
    suspend fun updateLanguage(language: String): DataResult<Unit>
    suspend fun clearAllSettings(): DataResult<Unit>
}