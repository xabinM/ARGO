package com.example.bogoargo.ui.viewmodels.classRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.dto.response.MessageResponseDto
import com.example.bogoargo.data.model.Class
import com.example.bogoargo.data.repository.AuthRepository
import com.example.bogoargo.data.repository.ClassRepository
import com.example.bogoargo.data.response.applyClassResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClassManagementUiState(
    val isLoading: Boolean = false,
    val teacherClasses: List<Class> = emptyList(),
    val studentClasses: List<Class> = emptyList(),
    val errorMessage: String? = null,
    val deleteSuccess: Boolean = false,
    val applySuccess: Boolean = false,
    val applyResponse: applyClassResponse? = null,
    val leaveSuccess: Boolean = false
)

class ClassManagementViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassManagementUiState())
    val uiState: StateFlow<ClassManagementUiState> = _uiState

    fun loadTeacherClasses(page: Int = 1, size: Int = 10, status: String? = "active") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            classRepository.getTeacherClassList(page, size, status).fold(
                onSuccess = { classes ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        teacherClasses = classes
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

    fun loadStudentClasses(page: Int = 1, size: Int = 10, status: String? = "active") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            classRepository.getStudentClassList(page, size, status).fold(
                onSuccess = { classes ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        studentClasses = classes
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

    fun deleteClass(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            classRepository.deleteClass(classId).fold(
                onSuccess = { _ ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        deleteSuccess = true
                    )
                    loadTeacherClasses()
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

    fun applyToClass(inviteCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            classRepository.applyClass(inviteCode).fold(
                onSuccess = { applyResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        applySuccess = true,
                        applyResponse = applyResponse
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

    fun leaveClass(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            classRepository.leaveClass(classId).fold(
                onSuccess = { _ ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        leaveSuccess = true
                    )
                    loadStudentClasses()
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
        _uiState.value = ClassManagementUiState()
    }

    fun clearSuccessFlags() {
        _uiState.value = _uiState.value.copy(
            deleteSuccess = false,
            applySuccess = false,
            leaveSuccess = false,
            applyResponse = null
        )
    }
}