package com.example.bogoargo.ui.viewmodels.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.User
import com.example.bogoargo.domain.model.UserRole
import com.example.bogoargo.domain.repository.IAuthRepository
import com.example.bogoargo.domain.repository.IUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class TeacherHomeUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val isTeacher: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TeacherHomeViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    private val authRepository: IAuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TeacherHomeUiState())
    val uiState: StateFlow<TeacherHomeUiState> = _uiState

    init {
        checkTeacherRole()
    }

    private fun checkTeacherRole() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = getCurrentUser()
                val isTeacher = currentUser?.role == UserRole.ROLE_TEACHER
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentUser = currentUser,
                    isTeacher = isTeacher
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    private suspend fun getCurrentUser(): User? {
        return userRepository.getLoggedInUser()
    }

    fun refreshUserInfo() {
        checkTeacherRole()
    }

    fun clearState() {
        _uiState.value = TeacherHomeUiState()
    }
}