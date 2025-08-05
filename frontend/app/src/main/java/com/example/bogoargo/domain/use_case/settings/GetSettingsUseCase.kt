package com.example.bogoargo.domain.use_case.settings

import com.example.bogoargo.domain.model.Settings
import com.example.bogoargo.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: ISettingsRepository
) {
    operator fun invoke(): Flow<Settings> {
        return settingsRepository.getSettings()
    }
}