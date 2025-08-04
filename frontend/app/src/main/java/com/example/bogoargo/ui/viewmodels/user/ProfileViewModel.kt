package com.example.bogoargo.ui.viewmodels.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.dto.UserUpdateRequest
import com.example.bogoargo.data.dto.UserWithdrawRequest
import com.example.bogoargo.data.dto.response.UserUpdateResponse
import com.example.bogoargo.data.dto.response.UserWithdrawResponse
import com.example.bogoargo.data.model.User
import com.example.bogoargo.data.repository.AuthRepository
import com.example.bogoargo.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val updateSuccess: Boolean = false,
    val withdrawSuccess: Boolean = false,
    val error: String? = null,
    val updateResponse: UserUpdateResponse? = null,
    val withdrawResponse: UserWithdrawResponse? = null,
    val username: String = "",
    val name: String = "",
    val password: String = "",
    val role: String = "",
    val user: User? = null
)

class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(isEditing = !_uiState.value.isEditing)
    }

    fun updateProfile(username: String, name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val request = UserUpdateRequest(name = name, password = _uiState.value.password)
            
            userRepository.updateUserInfo(request).fold(
                onSuccess = { updateResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        updateSuccess = true,
                        isEditing = false,
                        name = name,
                        username = username,
                        updateResponse = updateResponse
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    fun withdraw() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val request = UserWithdrawRequest(password = _uiState.value.password)
            
            userRepository.withdrawUser(request).fold(
                onSuccess = { withdrawResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        withdrawSuccess = true,
                        withdrawResponse = withdrawResponse
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    fun updateUserInfo(name: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val request = UserUpdateRequest(name = name, password = password)
            
            userRepository.updateUserInfo(request).fold(
                onSuccess = { updateResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        updateSuccess = true,
                        updateResponse = updateResponse
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    fun withdrawUser(password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val request = UserWithdrawRequest(password = password)
            
            userRepository.withdrawUser(request).fold(
                onSuccess = { withdrawResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        withdrawSuccess = true,
                        withdrawResponse = withdrawResponse
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }
/* //TODO: 프로픨 로드
    fun loadProfile() {
        viewModelScope.launch {
            userRepository.getCurrentUser().fold(
                onSuccess = { user: User? ->
                    if (user != null) {
                        _uiState.value = _uiState.value.copy(
                            user = user,
                            name = user.name,
                            role = user.role.toString()
                        )
                    } else {
                        // user가 null인 경우 (예: 저장된 사용자 정보 없음)
                        _uiState.value = _uiState.value.copy(
                            user = null,
                            name = "",
                            role = "",
                            error = "사용자 정보를 로드할 수 없습니다. 로그인 상태를 확인해주세요."
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        user = null, // 에러 발생 시 사용자 정보 초기화
                        name = "",
                        role = "",
                        error = exception.message
                    )
                }
            )
        }
    }
*/
    fun clearState() {
        _uiState.value = ProfileUiState()
    }

    fun clearSuccessFlags() {
        _uiState.value = _uiState.value.copy(
            updateSuccess = false,
            withdrawSuccess = false,
            updateResponse = null,
            withdrawResponse = null
        )
    }
}