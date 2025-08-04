package com.example.bogoargo.ui.viewmodels.classRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.model.Class
import com.example.bogoargo.data.repository.AuthRepository
import com.example.bogoargo.data.repository.ClassRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClassDetailUiState(
    val isLoading: Boolean = false,
    val classDetail: Class? = null,
    val errorMessage: String? = null
)

class ClassDetailViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassDetailUiState())
    val uiState: StateFlow<ClassDetailUiState> = _uiState

    fun loadClassDetail(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            classRepository.getClassDetail(classId).fold(
                onSuccess = { classDetail ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        classDetail = classDetail
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

    fun refreshClassDetail(classId: Long) {
        loadClassDetail(classId)
    }

    fun clearState() {
        _uiState.value = ClassDetailUiState()
    }
}