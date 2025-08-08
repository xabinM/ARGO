package com.example.bogoargo.ui.viewmodels.classRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class ClassMemberManagementUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val approveSuccess: Boolean = false,
    val classMembers: List<Any> = emptyList(), // TODO: Replace with proper User model
    val pendingApplications: List<Any> = emptyList() // TODO: Replace with proper Application model
)

@HiltViewModel
class ClassMemberManagementViewModel @Inject constructor(
    // TODO: Add appropriate Use Cases when API is ready
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassMemberManagementUiState())
    val uiState: StateFlow<ClassMemberManagementUiState> = _uiState

    // TODO: Implement methods when Use Cases are available
    fun loadClassMembers(classId: Long, status: String = "active", page: Int = 10, size: Int = 10) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // TODO: Implement with Use Case
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                classMembers = emptyList(), // Empty for now
                errorMessage = null
            )
        }
    }

    fun loadPendingApplications(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // TODO: Implement with Use Case
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                pendingApplications = emptyList(), // Empty for now
                errorMessage = null
            )
        }
    }

    fun approveApplication(classId: Long, applicationId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // TODO: Implement with Use Case
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Feature not implemented yet"
            )
        }
    }

    fun clearState() {
        _uiState.value = ClassMemberManagementUiState()
    }
}