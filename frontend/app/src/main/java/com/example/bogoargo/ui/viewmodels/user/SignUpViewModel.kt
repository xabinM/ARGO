package com.example.bogoargo.ui.viewmodels.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.dto.UserSignUpRequest
import com.example.bogoargo.data.dto.response.MessageResponseDto
import com.example.bogoargo.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val signUpResponse: MessageResponseDto? = null
)

class SignUpViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun signUp(
        username: String,
        password: String,
        name: String,
        role: String,
        agreeTerms: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val request = UserSignUpRequest(
                username = username,
                password = password,
                name = name,
                role = role,
                agreeTerms = agreeTerms
            )
            
            userRepository.signUp(request).fold(
                onSuccess = { signUpResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        signUpResponse = signUpResponse
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
            )
        }
    }

    fun clearState() {
        _uiState.value = SignUpUiState()
    }
}
