package com.example.bogoargo.data.api

import com.example.bogoargo.data.repository.AuthRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val authRepository: AuthRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 로그인 엔드포인트는 토큰이 필요없음
        if (originalRequest.url.encodedPath.contains("/api/auth/login")) {
            return chain.proceed(originalRequest)
        }
        
        // 토큰 가져오기
        val token = runBlocking {
            authRepository.tokenFlow.first()?.accessToken
        }
        
        // 토큰이 있으면 헤더에 추가
        val request = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        return chain.proceed(request)
    }
}