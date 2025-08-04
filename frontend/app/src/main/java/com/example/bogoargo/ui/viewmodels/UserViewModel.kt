package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.dto.UserSignUpRequest
import com.example.bogoargo.data.dto.UserLoginRequest
import com.example.bogoargo.data.dto.UserUpdateRequest
import com.example.bogoargo.data.dto.UserWithdrawRequest
import com.example.bogoargo.data.dto.response.MessageResponseDto
import com.example.bogoargo.data.dto.response.UserUpdateResponse
import com.example.bogoargo.data.dto.response.UserWithdrawResponse
import com.example.bogoargo.data.repository.UserRepository
import com.example.bogoargo.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _signUpResult = MutableStateFlow<MessageResponseDto?>(null)
    val signUpResult: StateFlow<MessageResponseDto?> = _signUpResult.asStateFlow()

    private val _updateResult = MutableStateFlow<UserUpdateResponse?>(null)
    val updateResult: StateFlow<UserUpdateResponse?> = _updateResult.asStateFlow()

    private val _withdrawResult = MutableStateFlow<UserWithdrawResponse?>(null)
    val withdrawResult: StateFlow<UserWithdrawResponse?> = _withdrawResult.asStateFlow()

    fun clearUiState() {
        _uiState.value = UiState.Idle
    }

    fun signUp(
        username: String,
        password: String,
        name: String,
        role: String
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val request = UserSignUpRequest(
                    username = username,
                    password = password,
                    name = name,
                    role = role
                )
                val result = userRepository.signUp(request)
                result.fold(
                    onSuccess = { messageResponse ->
                        _signUpResult.value = messageResponse
                        _uiState.value = UiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "회원가입 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val request = UserLoginRequest(
                    username = username,
                    password = password
                )
                val result = userRepository.login(request)
                result.fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                        _uiState.value = UiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "로그인 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun updateUserInfo(
        name: String? = null,
        password: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val request = UserUpdateRequest(
                    name = name,
                    password = password
                )
                val result = userRepository.updateUserInfo(request)
                result.fold(
                    onSuccess = { updateResponse ->
                        _updateResult.value = updateResponse
                        _uiState.value = UiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "회원 정보 수정 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun withdrawUser(password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val request = UserWithdrawRequest(password = password)
                val result = userRepository.withdrawUser(request)
                result.fold(
                    onSuccess = { withdrawResponse ->
                        _withdrawResult.value = withdrawResponse
                        _currentUser.value = null
                        _uiState.value = UiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "회원 탈퇴 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _uiState.value = UiState.Idle
    }

    fun clearSignUpResult() {
        _signUpResult.value = null
    }

    fun clearUpdateResult() {
        _updateResult.value = null
    }

    fun clearWithdrawResult() {
        _withdrawResult.value = null
    }
}