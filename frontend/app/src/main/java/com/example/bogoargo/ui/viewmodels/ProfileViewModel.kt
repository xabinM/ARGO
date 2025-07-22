package com.example.bogoargo.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val email: String = "",
    val profilePictureUrl: String? = null,
    val bio: String = "",
    val isEditing: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository(application)
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeUserData()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            userRepository.userFlow.collect { user ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userName = user.userName,
                    email = user.email,
                    bio = user.bio,
                    profilePictureUrl = user.profilePictureUrl
                )
            }
        }
    }

    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(isEditing = !_uiState.value.isEditing)
    }

    fun updateProfile(userName: String, bio: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Save to DataStore
                userRepository.updateUserProfile(userName, bio)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isEditing = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update profile: ${e.message}"
                )
            }
        }
    }
}