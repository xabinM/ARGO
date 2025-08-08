package com.example.bogoargo.domain.use_case.auth

import com.example.bogoargo.domain.repository.IAuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    operator fun invoke() {
        authRepository.clearTokens()
    }
}