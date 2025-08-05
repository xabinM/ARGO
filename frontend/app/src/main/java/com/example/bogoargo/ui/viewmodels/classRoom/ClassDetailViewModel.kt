package com.example.bogoargo.ui.viewmodels.classRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.use_case.classroom.GetClassDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class ClassDetailUiState(
    val isLoading: Boolean = false,
    val classDetail: Class? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ClassDetailViewModel @Inject constructor(
    private val getClassDetailUseCase: GetClassDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassDetailUiState())
    val uiState: StateFlow<ClassDetailUiState> = _uiState

    fun loadClassDetail(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = getClassDetailUseCase(classId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        classDetail = result.data
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

    fun refreshClassDetail(classId: Long) {
        loadClassDetail(classId)
    }

    fun clearState() {
        _uiState.value = ClassDetailUiState()
    }
}