package com.example.bogoargo.domain.use_case.auth

import com.example.bogoargo.data.preferences.PreferencesManager
import com.example.bogoargo.domain.model.User
import javax.inject.Inject

class SaveUserInfoUseCase @Inject constructor(
    private val preferencesManager: PreferencesManager
) {
    operator fun invoke(user: User) {
        preferencesManager.saveUserId(user.userId)
        preferencesManager.saveUserName(user.name)
        preferencesManager.saveUserRole(user.role.name)
    }
}