package com.example.bogoargo.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.repository.AuthRepository
import com.example.bogoargo.data.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val id: String = "",
    val password: String = ""
)

class LoginViewModel(
    private val context: Context
) : ViewModel() {
    
    private val loginRepository = LoginRepository()
    private val authRepository = AuthRepository(context)
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    init {
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        viewModelScope.launch {
            authRepository.tokenFlow.collect { tokenInfo ->
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = tokenInfo != null
                )
            }
        }
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
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = "로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "네트워크 오류가 발생했습니다: ${e.message}"
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