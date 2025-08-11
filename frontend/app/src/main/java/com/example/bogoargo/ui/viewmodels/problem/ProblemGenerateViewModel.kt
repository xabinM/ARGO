package com.example.bogoargo.ui.viewmodels.problem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.dto.response.ProblemResponseDto
import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.use_case.classroom.GetClassDetailUseCase
import com.example.bogoargo.domain.use_case.problem.GenerateProblemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProblemGenerateUiState(
    val isLoading: Boolean = false,
    val classDetail: Class? = null,
    val problemCount: Int = 1,
    val spotId: Long = 1L, // 기본값 설정 (실제로는 사용자가 선택하거나 고정값 사용)
    val generatedProblems: ProblemResponseDto? = null,
    val errorMessage: String? = null,
    val isGenerating: Boolean = false
)

@HiltViewModel
class ProblemGenerateViewModel @Inject constructor(
    private val getClassDetailUseCase: GetClassDetailUseCase,
    private val generateProblemUseCase: GenerateProblemUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProblemGenerateUiState())
    val uiState: StateFlow<ProblemGenerateUiState> = _uiState

    fun loadClassDetail(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
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

    fun updateProblemCount(count: Int) {
        if (count >= 1) {
            _uiState.value = _uiState.value.copy(problemCount = count)
        }
    }

    fun updateSpotId(spotId: Long) {
        _uiState.value = _uiState.value.copy(spotId = spotId)
    }

    fun generateProblems() {
        val currentState = _uiState.value
        val classDetail = currentState.classDetail
        
        if (classDetail == null) {
            _uiState.value = currentState.copy(
                errorMessage = "반 정보를 먼저 불러와야 합니다."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isGenerating = true,
                errorMessage = null
            )

            val grade = classDetail.grade

            when (val result = generateProblemUseCase(
                spotId = currentState.spotId,
                grade = grade!!,
                problemCount = currentState.problemCount
            )) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        generatedProblems = result.data
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun clearGeneratedProblems() {
        _uiState.value = _uiState.value.copy(
            generatedProblems = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetState() {
        _uiState.value = ProblemGenerateUiState()
    }
}