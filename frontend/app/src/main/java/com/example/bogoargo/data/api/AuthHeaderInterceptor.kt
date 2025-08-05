package com.example.bogoargo.data.api

import com.example.bogoargo.data.storage.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthHeaderInterceptor(
    private val tokenStorage: TokenStorage
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 토큰 가져오기
        val token = tokenStorage.getAccessToken()
        
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