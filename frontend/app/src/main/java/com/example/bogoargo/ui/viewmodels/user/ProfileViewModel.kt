package com.example.bogoargo.ui.viewmodels.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.dto.response.UserUpdateResponse
import com.example.bogoargo.data.dto.response.UserWithdrawResponse
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.User
import com.example.bogoargo.domain.use_case.user.UpdateUserProfileUseCase
import com.example.bogoargo.domain.use_case.user.WithdrawUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val withdrawUserUseCase: WithdrawUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(isEditing = !_uiState.value.isEditing)
    }

    fun updateProfile(username: String, name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = updateUserProfileUseCase(name, _uiState.value.password)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        updateSuccess = true,
                        isEditing = false,
                        name = name,
                        username = username,
                        updateResponse = result.data
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Already set loading state
                }
            }
        }
    }

    fun withdraw() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = withdrawUserUseCase(_uiState.value.password)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        withdrawSuccess = true,
                        withdrawResponse = result.data
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Already set loading state
                }
            }
        }
    }


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