package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.bogoargo.data.storage.SecureStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val secureStorage: SecureStorage
) : ViewModel() {
    
    // validateToken 메서드는 제거됨 - 불필요한 API 호출 방지
    // 토큰 유효성은 실제 API 호출 시점에 TokenManagementInterceptor가 처리
    
    fun clearUserData() {
        secureStorage.clearAll()
    }
    
    fun hasValidSession(): Boolean {
        return secureStorage.hasValidSession()
    }
    
    fun getTokenInfo(): Pair<String, String>? {
        return secureStorage.getTokenInfo()
    }
}