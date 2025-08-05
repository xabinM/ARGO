package com.example.bogoargo.data.api

import com.example.bogoargo.domain.model.RefreshTokenRequest
import com.example.bogoargo.data.storage.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenStorage: TokenStorage,
    private val authApiService: AuthApiService
) : Authenticator {
    
    override fun authenticate(route: Route?, response: Response): Request? {
        // 이미 토큰 갱신을 시도했다면 null 반환 (무한 루프 방지)
        if (response.request.header("Authorization") != null && 
            response.priorResponse?.code == 401) {
            return null
        }
        
        return runBlocking {
            val refreshToken = tokenStorage.getRefreshToken()
            if (refreshToken != null) {
                try {
                    val refreshResponse = authApiService.refreshToken(
                        RefreshTokenRequest(refreshToken)
                    )
                    
                    if (refreshResponse.isSuccessful) {
                        val tokenInfo = refreshResponse.body()
                        if (tokenInfo != null) {
                            // 새 토큰 저장
                            tokenStorage.saveTokens(
                                tokenInfo.accessToken,
                                tokenInfo.refreshToken
                            )
                            
                            // 새 토큰으로 요청 재시도
                            response.request.newBuilder()
                                .header("Authorization", "Bearer ${tokenInfo.accessToken}")
                                .build()
                        } else {
                            // 토큰 갱신 실패, 토큰 삭제
                            tokenStorage.clearTokens()
                            null
                        }
                    } else {
                        // 토큰 갱신 실패, 토큰 삭제
                        tokenStorage.clearTokens()
                        null
                    }
                } catch (e: Exception) {
                    // 네트워크 오류 등, 토큰 삭제
                    tokenStorage.clearTokens()
                    null
                }
            } else {
                null
            }
        }
    }
}