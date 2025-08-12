package com.example.bogoargo.data.api

import com.example.bogoargo.domain.model.RefreshTokenRequest
import com.example.bogoargo.data.storage.SecureStorage
import com.example.bogoargo.data.event.TokenExpiredEvent
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Call
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject

class TokenManagementInterceptor(
    private val secureStorage: SecureStorage,
    private val authApiService: AuthApiService,
    private val tokenExpiredEvent: TokenExpiredEvent
) : Interceptor {
    
    private val maxRetryCount = 1
    private val tokenExpiredErrorCodes = setOf("4006") // 서버에서 정의한 토큰 만료 에러 코드
    
    companion object {
        private const val TAG = "TokenManagementInterceptor"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        return handleRequest(chain, 0)
    }
    
    private fun handleRequest(chain: Interceptor.Chain, retryCount: Int): Response {
        val originalRequest = chain.request()
        
        // 토큰 헤더 추가
        val token = secureStorage.getAccessToken()
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
        
        // 응답 본문을 JSON으로 파싱하여 에러 코드 확인
        val responseBody = response.peekBody(1024).string()
        Log.d(TAG, "Response code: ${response.code}, body: $responseBody")
        
        return try {
            val jsonObject = Gson().fromJson(responseBody, JsonObject::class.java)
            val errorCode = jsonObject.get("code")?.asString
            val isTokenExpired = tokenExpiredErrorCodes.contains(errorCode)
            
            Log.d(TAG, "Parsed error code: $errorCode")
            Log.d(TAG, "Is token expired: $isTokenExpired")
            
            isTokenExpired
        } catch (e: Exception) {
            Log.d(TAG, "Failed to parse JSON response: ${e.message}")
            // JSON 파싱 실패 시 기존 방식으로 폴백
            val hasTokenExpiredInMessage = responseBody.contains("만료된 토큰", ignoreCase = true) ||
                    responseBody.contains("TOKEN_EXPIRED", ignoreCase = true) ||
                    responseBody.contains("EXPIRED_TOKEN", ignoreCase = true)
            
            Log.d(TAG, "Fallback to message-based detection: $hasTokenExpiredInMessage")
            hasTokenExpiredInMessage
        }
    }
    
    private fun refreshTokenIfPossible(): Boolean {
        val refreshToken = secureStorage.getRefreshToken() ?: return false
        
        return try {
            val refreshCall = authApiService.refreshToken(
                "Bearer $refreshToken"
            )
            val refreshResponse = refreshCall.execute()
            
            if (refreshResponse.isSuccessful) {
                val refreshResult = refreshResponse.body()
                if (refreshResult != null && refreshResult.success) {
                    secureStorage.saveTokens(
                        refreshResult.tokens.accessToken,
                        refreshResult.tokens.refreshToken
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
        Log.d(TAG, "Token failure detected - clearing tokens and notifying event")
        secureStorage.clearTokens()
        tokenExpiredEvent.notifyTokenExpired()
        Log.d(TAG, "Token expired event sent")
    }
}