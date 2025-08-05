package com.example.bogoargo.ui.viewmodels.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.User
import com.example.bogoargo.domain.model.UserRole
import com.example.bogoargo.domain.use_case.auth.LoginUseCase
import com.example.bogoargo.domain.use_case.auth.SaveTokensUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val username: String = "",
    val password: String = "",
    val user: User? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val saveTokensUseCase: SaveTokensUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun login() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = loginUseCase(_uiState.value.username, _uiState.value.password)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        user = result.data
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // 이미 로딩 상태 설정됨
                }
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearState() {
        _uiState.value = LoginUiState()
    }

    // 더미 로그인 함수
    fun dummyLogin(isTeacher: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // 로딩 시뮬레이션
            delay(1000)
            
            try {
                // 더미 사용자 데이터 생성
                val dummyUser = if (isTeacher) {
                    User(
                        userId = 1L,
                        name = "김선생",
                        role = UserRole.TEACHER,
                        team = null
                    )
                } else {
                    User(
                        userId = 2L,
                        name = "이학생",
                        role = UserRole.STUDENT,
                        team = null
                    )
                }
                
                // 더미 토큰 저장
                saveTokensUseCase(
                    accessToken = "dummy_access_token_${if (isTeacher) "teacher" else "student"}",
                    refreshToken = "dummy_refresh_token_${if (isTeacher) "teacher" else "student"}"
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    user = dummyUser
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "더미 로그인 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }
}