package com.example.bogoargo.domain.use_case.auth

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.IAuthRepository
import javax.inject.Inject

class RefreshTokenUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(): DataResult<Boolean> {
        return authRepository.refreshToken()
    }
}