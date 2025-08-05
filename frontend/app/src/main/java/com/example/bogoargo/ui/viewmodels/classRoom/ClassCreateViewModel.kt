package com.example.bogoargo.ui.viewmodels.classRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.dto.ClassCreateRequest
import com.example.bogoargo.data.model.Class
import com.example.bogoargo.data.repository.AuthRepository
import com.example.bogoargo.data.repository.ClassRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClassCreateUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val createdClass: Class? = null
)

class ClassCreateViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassCreateUiState())
    val uiState: StateFlow<ClassCreateUiState> = _uiState

    fun createClass(
        className: String,
        description: String,
        location: String,
        activityDate: String,
        maxStudents: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val request = ClassCreateRequest(
                className = className,
                description = description,
                location = location,
                activityDate = activityDate,
                maxStudents = maxStudents
            )
            
            classRepository.createClass(request).fold(
                onSuccess = { createdClass ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        createdClass = createdClass
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
        _uiState.value = ClassCreateUiState()
    }
}