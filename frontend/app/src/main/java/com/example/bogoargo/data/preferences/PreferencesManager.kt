package com.example.bogoargo.data.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PreferencesManager @Inject constructor(
    // Hilt를 사용한다면 @ApplicationContext 어노테이션을 사용하여 Context를 주입받습니다.
    @ApplicationContext private val context: Context
) {
    // SharedPreferences 파일 이름
    private val PREFS_NAME = "my_app_preferences"

    // SharedPreferences 인스턴스
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // StateFlow for reactive user info
    private val _currentUserId = MutableStateFlow<Long?>(getUserId())
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

    private val _currentUserName = MutableStateFlow<String?>(getUserName())
    val currentUserName: StateFlow<String?> = _currentUserName.asStateFlow()

    private val _currentUserRole = MutableStateFlow<String?>(getUserRole())
    val currentUserRole: StateFlow<String?> = _currentUserRole.asStateFlow()

    // --- JWT 토큰 관련 키와 메서드 ---
    private val KEY_JWT_TOKEN = "jwt_token"

    fun saveJwtToken(token: String) {
        prefs.edit().putString(KEY_JWT_TOKEN, token).apply()
    }

    fun getJwtToken(): String? {
        return prefs.getString(KEY_JWT_TOKEN, null)
    }

    // --- 사용자 ID 관련 키와 메서드 ---
    private val KEY_USER_ID = "user_id"

    fun saveUserId(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
        _currentUserId.value = userId
    }

    fun getUserId(): Long? {
        val id = prefs.getLong(KEY_USER_ID, -1L) // 기본값 -1L. 실제 ID가 -1일 가능성을 고려해야 함.
        return if (id == -1L) null else id
    }

    // --- 사용자 닉네임 관련 키와 메서드 (User 객체에 'name' 대신 'nickname'이 있다고 가정) ---
    private val KEY_USER_NAME = "user_nickname"

    fun saveUserName(userName: String) {
        prefs.edit().putString(KEY_USER_NAME, userName).apply()
        _currentUserName.value = userName
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    // --- 사용자 역할(Role) 관련 키와 메서드 ---
    private val KEY_USER_ROLE = "user_role"

    fun saveUserRole(role: String) { // Enum의 name 또는 toString() 값을 String으로 저장
        prefs.edit().putString(KEY_USER_ROLE, role).apply()
        _currentUserRole.value = role
    }

    fun getUserRole(): String? {
        return prefs.getString(KEY_USER_ROLE, null)
    }

    // --- 모든 인증/사용자 정보 삭제 ---
    fun clearAuthInfo() {
        prefs.edit()
            .remove(KEY_JWT_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_ROLE)
            .apply()
        
        // StateFlow도 초기화
        _currentUserId.value = null
        _currentUserName.value = null
        _currentUserRole.value = null
    }

    // (옵션) 모든 앱 설정 데이터 삭제 (주의해서 사용)
    fun clearAllPreferences() {
        prefs.edit().clear().apply()
        
        // StateFlow도 초기화
        _currentUserId.value = null
        _currentUserName.value = null
        _currentUserRole.value = null
    }
}