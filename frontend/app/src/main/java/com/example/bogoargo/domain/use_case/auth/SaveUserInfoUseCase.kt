package com.example.bogoargo.domain.use_case.auth

import com.example.bogoargo.data.storage.SecureStorage
import com.example.bogoargo.domain.model.User
import javax.inject.Inject

class SaveUserInfoUseCase @Inject constructor(
    private val secureStorage: SecureStorage
) {
    operator fun invoke(user: User) {
        secureStorage.saveUser(user)
    }
}