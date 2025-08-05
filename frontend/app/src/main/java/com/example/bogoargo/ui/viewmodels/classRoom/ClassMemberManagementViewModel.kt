package com.example.bogoargo.ui.viewmodels.classRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.dto.response.ApplicationDataDto
import com.example.bogoargo.data.dto.response.ApplicationResponseDto
import com.example.bogoargo.data.dto.response.MessageResponseDto
import com.example.bogoargo.data.dto.response.UserDataDto
import com.example.bogoargo.data.repository.AuthRepository
import com.example.bogoargo.data.repository.ClassRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClassMemberManagementUiState(
    val isLoading: Boolean = false,
    val classMembers: List<UserDataDto> = emptyList(),
    val pendingApplications: List<ApplicationDataDto> = emptyList(),
    val errorMessage: String? = null,
    val approvalSuccess: Boolean = false,
    val approvalMessage: String? = null
)

class ClassMemberManagementViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassMemberManagementUiState())
    val uiState: StateFlow<ClassMemberManagementUiState> = _uiState

    fun loadClassMembers(classId: Long, status: String = "approved", page: Int = 1, size: Int = 10) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            classRepository.getClassMemberList(classId, status, page, size).fold(
                onSuccess = { members ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        classMembers = members ?: emptyList()
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

    fun loadPendingApplications(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            classRepository.getApplicationList(classId).fold(
                onSuccess = { applicationResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pendingApplications = applicationResponse?.applications ?: emptyList()
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

    fun approveApplication(classId: Long, applicationId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            classRepository.approveApplication(classId, applicationId).fold(
                onSuccess = { messageResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        approvalSuccess = true,
                        approvalMessage = messageResponse?.message
                    )
                    loadPendingApplications(classId)
                    loadClassMembers(classId)
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

    fun refreshData(classId: Long) {
        loadClassMembers(classId)
        loadPendingApplications(classId)
    }

    fun clearState() {
        _uiState.value = ClassMemberManagementUiState()
    }

    fun clearApprovalSuccess() {
        _uiState.value = _uiState.value.copy(
            approvalSuccess = false,
            approvalMessage = null
        )
    }
}