package com.example.bogoargo.domain.use_case.auth

import com.example.bogoargo.data.storage.SecureStorage
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val secureStorage: SecureStorage
) {
    operator fun invoke() {
        // 토큰과 사용자 정보 모두 삭제
        secureStorage.clearAll()
    }
}