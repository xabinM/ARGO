package com.example.bogoargo.data.api

import com.example.bogoargo.domain.model.RefreshTokenRequest
import com.example.bogoargo.data.storage.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Call

class TokenManagementInterceptor(
    private val tokenStorage: TokenStorage,
    private val authApiService: AuthApiService
) : Interceptor {
    
    private val maxRetryCount = 1
    private val tokenExpiredMessages = setOf("TOKEN_EXPIRED", "EXPIRED_TOKEN", "TOKEN_INVALID")
    
    override fun intercept(chain: Interceptor.Chain): Response {
        return handleRequest(chain, 0)
    }
    
    private fun handleRequest(chain: Interceptor.Chain, retryCount: Int): Response {
        val originalRequest = chain.request()
        
        // 토큰 헤더 추가
        val token = tokenStorage.getAccessToken()
        val requestWithToken = if (token != null) {
            originalRequest.newBuilder()
                .removeHeader("Authorization")
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        val response = chain.proceed(requestWithToken)
        
        // TOKEN_EXPIRED 감지 및 재시도 로직
        if (shouldRetryWithTokenRefresh(response) && retryCount < maxRetryCount) {
            response.close() // 기존 응답 리소스 해제
            
            if (refreshTokenIfPossible()) {
                return handleRequest(chain, retryCount + 1)
            } else {
                // 토큰 갱신 실패시 원본 응답 반환하기 위해 재요청
                return chain.proceed(requestWithToken)
            }
        }
        
        return response
    }
    
    private fun shouldRetryWithTokenRefresh(response: Response): Boolean {
        if (response.code != 401 && response.code != 403) {
            return false
        }
        
        // 응답 본문에서 TOKEN_EXPIRED 메시지 확인
        val responseBody = response.peekBody(1024).string()
        return tokenExpiredMessages.any { message ->
            responseBody.contains(message, ignoreCase = true)
        }
    }
    
    private fun refreshTokenIfPossible(): Boolean {
        val refreshToken = tokenStorage.getRefreshToken() ?: return false
        
        return try {
            val refreshCall = authApiService.refreshToken(
                RefreshTokenRequest(refreshToken)
            )
            val refreshResponse = refreshCall.execute()
            
            if (refreshResponse.isSuccessful) {
                val tokenInfo = refreshResponse.body()
                if (tokenInfo != null) {
                    tokenStorage.saveTokens(
                        tokenInfo.accessToken,
                        tokenInfo.refreshToken
                    )
                    true
                } else {
                    handleTokenFailure()
                    false
                }
            } else {
                handleTokenFailure()
                false
            }
        } catch (e: Exception) {
            handleTokenFailure()
            false
        }
    }
    
    private fun handleTokenFailure() {
        tokenStorage.clearTokens()
    }
}