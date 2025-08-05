package com.example.bogoargo.ui.viewmodels.classRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.use_case.classroom.CreateClassUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class ClassCreateUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val createdClass: Class? = null
)

@HiltViewModel
class ClassCreateViewModel @Inject constructor(
    private val createClassUseCase: CreateClassUseCase
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
            
            when (val result = createClassUseCase(className, description, location, activityDate, maxStudents)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        createdClass = result.data
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

    fun clearState() {
        _uiState.value = ClassCreateUiState()
    }
}