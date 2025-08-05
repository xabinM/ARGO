package com.example.bogoargo.ui.viewmodels.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.model.User
import com.example.bogoargo.data.model.UserRole
import com.example.bogoargo.data.repository.AuthRepository
import com.example.bogoargo.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeacherMainUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val isTeacher: Boolean = false,
    val errorMessage: String? = null
)

class TeacherMainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TeacherMainUiState())
    val uiState: StateFlow<TeacherMainUiState> = _uiState

    init {
        checkTeacherRole()
    }

    private fun checkTeacherRole() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = getCurrentUser()
                val isTeacher = currentUser?.role == UserRole.TEACHER
                
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
        return null
    }

    fun refreshUserInfo() {
        checkTeacherRole()
    }

    fun clearState() {
        _uiState.value = TeacherMainUiState()
    }
}