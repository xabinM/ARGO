package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.model.LoginRequest
import com.example.bogoargo.data.repository.AuthRepository
import com.example.bogoargo.data.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val id: String = "",
    val password: String = ""
)

class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    init {
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        val tokenInfo = authRepository.getTokenInfo()
        _uiState.value = _uiState.value.copy(
            isLoggedIn = tokenInfo != null
        )
    }
    
    fun updateId(id: String) {
        _uiState.value = _uiState.value.copy(id = id)
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }
    
    fun login() {
        val currentState = _uiState.value
        
        if (currentState.id.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "아이디와 비밀번호를 입력해주세요."
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                val response = loginRepository.login(currentState.id, currentState.password)
                
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        authRepository.saveTokens(
                            loginResponse.accessToken,
                            loginResponse.refreshToken
                        )
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            errorMessage = null
                        )
                    } else {
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            errorMessage = "로그인 응답을 처리할 수 없습니다."
                        )
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "아이디 또는 비밀번호가 잘못되었습니다."
                        500 -> "서버 오류가 발생했습니다."
                        else -> "로그인 실패: HTTP ${response.code()}"
                    }
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "네트워크 오류: ${e.message}"
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.clearTokens()
            _uiState.value = LoginUiState()
        }
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}