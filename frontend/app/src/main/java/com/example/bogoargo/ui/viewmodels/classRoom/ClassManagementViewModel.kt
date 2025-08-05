package com.example.bogoargo.ui.viewmodels.classRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.use_case.classroom.ApplyClassUseCase
import com.example.bogoargo.domain.use_case.classroom.DeleteClassUseCase
import com.example.bogoargo.domain.use_case.classroom.GetStudentClassListUseCase
import com.example.bogoargo.domain.use_case.classroom.GetTeacherClassListUseCase
import com.example.bogoargo.domain.use_case.classroom.LeaveClassUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class ClassManagementUiState(
    val isLoading: Boolean = false,
    val teacherClasses: List<Class> = emptyList(),
    val studentClasses: List<Class> = emptyList(),
    val errorMessage: String? = null,
    val deleteSuccess: Boolean = false,
    val applySuccess: Boolean = false,
    val leaveSuccess: Boolean = false
)

@HiltViewModel
class ClassManagementViewModel @Inject constructor(
    private val getTeacherClassListUseCase: GetTeacherClassListUseCase,
    private val getStudentClassListUseCase: GetStudentClassListUseCase,
    private val deleteClassUseCase: DeleteClassUseCase,
    private val applyClassUseCase: ApplyClassUseCase,
    private val leaveClassUseCase: LeaveClassUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassManagementUiState())
    val uiState: StateFlow<ClassManagementUiState> = _uiState

    fun loadTeacherClasses(page: Int = 1, size: Int = 10, status: String? = "active") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = getTeacherClassListUseCase(page, size, status)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        teacherClasses = result.data
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun loadStudentClasses(page: Int = 1, size: Int = 10, status: String? = "active") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = getStudentClassListUseCase(page, size, status)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        studentClasses = result.data
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun deleteClass(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = deleteClassUseCase(classId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        deleteSuccess = true
                    )
                    loadTeacherClasses()
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun applyToClass(inviteCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = applyClassUseCase(inviteCode)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        applySuccess = true
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun leaveClass(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = leaveClassUseCase(classId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        leaveSuccess = true
                    )
                    loadStudentClasses()
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun clearState() {
        _uiState.value = ClassManagementUiState()
    }

    fun clearSuccessFlags() {
        _uiState.value = _uiState.value.copy(
            deleteSuccess = false,
            applySuccess = false,
            leaveSuccess = false
        )
    }
}