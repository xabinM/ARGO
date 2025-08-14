package com.example.bogoargo.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.bogoargo.domain.model.User
import com.example.bogoargo.domain.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 통합 저장소 - 토큰과 사용자 정보를 한 곳에서 관리
 * SharedPreferences를 사용한 간단한 구현
 */
@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext context: Context
) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "app_secure_prefs",
        Context.MODE_PRIVATE
    )
    
    companion object {
        // Token Keys
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        
        // User Info Keys
        private const val USER_ID_KEY = "user_id"
        private const val USER_NAME_KEY = "user_name"
        private const val USER_ROLE_KEY = "user_role"

        private const val USER_FCM_TOKEN_KEY = "user_fcm_token"
    }
    
    // StateFlow for reactive updates
    private val _currentUserId = MutableStateFlow<Long?>(getUserId())
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()
    
    private val _currentUserName = MutableStateFlow<String?>(getUserName())
    val currentUserName: StateFlow<String?> = _currentUserName.asStateFlow()
    
    private val _currentUserRole = MutableStateFlow<String?>(getUserRole())
    val currentUserRole: StateFlow<String?> = _currentUserRole.asStateFlow()
    
    // ===== Token Management =====
    
    fun saveTokens(accessToken: String, refreshToken: String) {
        with(sharedPreferences.edit()) {
            putString(ACCESS_TOKEN_KEY, accessToken)
            putString(REFRESH_TOKEN_KEY, refreshToken)
            apply()
        }
    }
    
    fun getAccessToken(): String? {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
    }
    
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
    }
    
    fun clearTokens() {
        with(sharedPreferences.edit()) {
            remove(ACCESS_TOKEN_KEY)
            remove(REFRESH_TOKEN_KEY)
            apply()
        }
    }
    
    fun hasTokens(): Boolean {
        return getAccessToken() != null && getRefreshToken() != null
    }
    
    fun getTokenInfo(): Pair<String, String>? {
        val accessToken = getAccessToken()
        val refreshToken = getRefreshToken()
        return if (accessToken != null && refreshToken != null) {
            Pair(accessToken, refreshToken)
        } else {
            null
        }
    }
    
    // ===== User Information Management =====
    
    fun saveUserInfo(userId: Long, userName: String, userRole: String) {
        with(sharedPreferences.edit()) {
            putLong(USER_ID_KEY, userId)
            putString(USER_NAME_KEY, userName)
            putString(USER_ROLE_KEY, userRole)
            apply()
        }
        
        // Update StateFlow
        _currentUserId.value = userId
        _currentUserName.value = userName
        _currentUserRole.value = userRole
    }
    
    fun saveUser(user: User) {
        // userId가 null이면 저장하지 않음 (재로그인 필요)
        if (user.userId == null) {
            return
        }
        
        saveUserInfo(
            userId = user.userId,
            userName = user.name,
            userRole = user.role.name
        )
    }
    
    fun getUserId(): Long? {
        val id = sharedPreferences.getLong(USER_ID_KEY, -1L)
        return if (id == -1L) null else id
    }
    
    fun getUserName(): String? {
        return sharedPreferences.getString(USER_NAME_KEY, null)
    }
    
    fun getUserRole(): String? {
        return sharedPreferences.getString(USER_ROLE_KEY, null)
    }
    
    fun getUser(): User? {
        val userId = getUserId()
        val userName = getUserName()
        val userRole = getUserRole()
        
        return if (userId != null && userName != null && userRole != null) {
            User(
                userId = userId,
                name = userName,
                role = UserRole.valueOf(userRole),
                team = null
            )
        } else {
            null
        }
    }
    
    fun clearUserInfo() {
        with(sharedPreferences.edit()) {
            remove(USER_ID_KEY)
            remove(USER_NAME_KEY)
            remove(USER_ROLE_KEY)
            apply()
        }
        
        // Update StateFlow
        _currentUserId.value = null
        _currentUserName.value = null
        _currentUserRole.value = null
    }
    
    // ===== Complete Clear =====
    
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
        
        // Update StateFlow
        _currentUserId.value = null
        _currentUserName.value = null
        _currentUserRole.value = null
    }
    
    // ===== Validation =====
    
    fun hasValidSession(): Boolean {
        val hasTokens = hasTokens()
        val user = getUser()
        
        // 토큰과 사용자 정보가 모두 있고, userId가 유효한 값인지 확인
        return hasTokens && user != null && user.userId != null && user.userId > 0
    }

    fun saveFcmToken(token: String?) {
        sharedPreferences.edit().apply {
            if (token == null) remove(USER_FCM_TOKEN_KEY) else putString(USER_FCM_TOKEN_KEY, token)
        }.apply()
    }

    fun getFcmToken(): String? {
        return sharedPreferences.getString(USER_FCM_TOKEN_KEY, null)
    }

    fun clearFcmToken() {
        sharedPreferences.edit().remove(USER_FCM_TOKEN_KEY).apply()
    }
}