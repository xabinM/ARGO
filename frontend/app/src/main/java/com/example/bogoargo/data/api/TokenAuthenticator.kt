package com.example.bogoargo.data.api

import com.example.bogoargo.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val authRepository: AuthRepository
) : Authenticator {
    
    override fun authenticate(route: Route?, response: Response): Request? {
        // 이미 토큰 갱신을 시도했다면 null 반환 (무한 루프 방지)
        if (response.request.header("Authorization") != null && 
            response.priorResponse?.code == 401) {
            return null
        }
        
        return runBlocking {
            if (authRepository.refreshToken()) {
                val newToken = authRepository.getAccessToken()
                if (newToken != null) {
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                } else {
                    null
                }
            } else {
                null
            }
        }
    }
}