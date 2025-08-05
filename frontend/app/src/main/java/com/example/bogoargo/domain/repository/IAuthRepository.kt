package com.example.bogoargo.domain.repository

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.TokenInfo

interface IAuthRepository {
    fun getTokenInfo(): TokenInfo?
    fun saveTokens(accessToken: String, refreshToken: String)
    fun clearTokens()
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun hasTokens(): Boolean
    suspend fun refreshToken(): DataResult<Boolean>
}