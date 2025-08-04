package com.example.bogoargo.data.repository

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bogoargo.data.api.ApiClient
import com.example.bogoargo.data.dto.UserLoginRequest
import com.example.bogoargo.data.dto.response.UserDataDto
import com.example.bogoargo.data.dto.response.UserLoginResponse
import com.example.bogoargo.data.mapper.toDomainModel
import com.example.bogoargo.data.model.RefreshTokenRequest
import com.example.bogoargo.data.model.TokenInfo
import com.example.bogoargo.data.model.User
import com.example.bogoargo.data.preferences.PreferencesManager
import com.example.bogoargo.data.storage.TokenStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

class AuthRepository(private val context: Context,
                     //private val preferencesManager: PreferencesManager
) {
    
    private val tokenStorage = TokenStorage(context)
    private val refreshMutex = Mutex()


//TODO: 유저 저장 기능
//    suspend fun saveAuthInfo(jwtToken: String, user: UserLoginResponse) {
//        preferencesManager.saveJwtToken(jwtToken)
//        preferencesManager.saveUserId(user.data?.userId ?: -1L)
//        preferencesManager.saveUserName(user.data?.name ?: "")
//        preferencesManager.saveUserRole(user.data?.role.toString())
//    }

    
    fun getTokenInfo(): TokenInfo? {
        val accessToken = tokenStorage.getAccessToken()
        val refreshToken = tokenStorage.getRefreshToken()
        
        return if (accessToken != null && refreshToken != null) {
            TokenInfo(accessToken, refreshToken)
        } else {
            null
        }
    }
    
    fun saveTokens(accessToken: String, refreshToken: String) {
        tokenStorage.saveTokens(accessToken, refreshToken)
    }
    
    fun clearTokens() {
        tokenStorage.clearTokens()
    }
    
    fun getAccessToken(): String? {
        return tokenStorage.getAccessToken()
    }
    
    fun getRefreshToken(): String? {
        return tokenStorage.getRefreshToken()
    }
    
    fun hasTokens(): Boolean {
        return tokenStorage.hasTokens()
    }
    
    suspend fun refreshToken(): Boolean {
        return refreshMutex.withLock {
            val refreshToken = getRefreshToken() ?: return@withLock false
            
            try {
                val response = ApiClient.authApiService.refreshToken(
                    RefreshTokenRequest(refreshToken)
                )
                
                if (response.isSuccessful) {
                    val refreshResponse = response.body()
                    if (refreshResponse != null) {
                        saveTokens(refreshResponse.accessToken, refreshResponse.refreshToken)
                        return@withLock true
                    }
                }
                
                // 갱신 실패 시 토큰 클리어
                clearTokens()
                return@withLock false
            } catch (e: Exception) {
                clearTokens()
                return@withLock false
            }
        }
    }
}