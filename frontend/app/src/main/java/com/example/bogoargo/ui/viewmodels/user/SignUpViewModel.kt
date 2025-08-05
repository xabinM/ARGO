package com.example.bogoargo.ui.viewmodels.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.dto.response.MessageResponseDto
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.use_case.user.SignUpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class SignUpUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val signUpResponse: MessageResponseDto? = null
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
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
            
            when (val result = signUpUseCase(username, password, name, role, agreeTerms)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        signUpResponse = result.data
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Already set loading state
                }
            }
        }
    }

    fun clearState() {
        _uiState.value = SignUpUiState()
    }
}
