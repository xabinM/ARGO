package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.model.TokenInfo
import com.example.bogoargo.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _tokenInfo = MutableStateFlow<TokenInfo?>(null)
    val tokenInfo: StateFlow<TokenInfo?> = _tokenInfo.asStateFlow()

    init {
        checkAuthenticationStatus()
    }

    fun clearUiState() {
        _uiState.value = UiState.Idle
    }

    private fun checkAuthenticationStatus() {
        val hasTokens = authRepository.hasTokens()
        _isAuthenticated.value = hasTokens
        if (hasTokens) {
            _tokenInfo.value = authRepository.getTokenInfo()
        }
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        authRepository.saveTokens(accessToken, refreshToken)
        _tokenInfo.value = TokenInfo(accessToken, refreshToken)
        _isAuthenticated.value = true
    }

    fun refreshToken() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val success = authRepository.refreshToken()
                if (success) {
                    _tokenInfo.value = authRepository.getTokenInfo()
                    _isAuthenticated.value = true
                    _uiState.value = UiState.Success
                } else {
                    logout()
                    _uiState.value = UiState.Error("토큰 갱신 실패")
                }
            } catch (e: Exception) {
                logout()
                _uiState.value = UiState.Error(e.message ?: "토큰 갱신 중 오류 발생")
            }
        }
    }

    fun logout() {
        authRepository.clearTokens()
        _tokenInfo.value = null
        _isAuthenticated.value = false
        _uiState.value = UiState.Idle
    }

    fun getAccessToken(): String? {
        return authRepository.getAccessToken()
    }

    fun getRefreshToken(): String? {
        return authRepository.getRefreshToken()
    }

    fun hasValidTokens(): Boolean {
        return authRepository.hasTokens()
    }
}