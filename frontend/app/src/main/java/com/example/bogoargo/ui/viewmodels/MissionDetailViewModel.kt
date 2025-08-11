package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.domain.use_case.mission.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MissionDetailUiState(
    val isLoading: Boolean = false,
    val missionCreateResult: MissionCreateResult? = null,
    val problemDetail: ProblemDetail? = null,
    val selectedAnswer: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val submitResult: MissionSubmitResult? = null,
    val errorMessage: String? = null,
    val isCompleted: Boolean = false,
    
    // 셀피 관련 상태
    val capturedImageBase64: String? = null,
    val isSelfieValidating: Boolean = false,
    val selfieValidationResult: Boolean? = null
)

@HiltViewModel
class MissionDetailViewModel @Inject constructor(
    private val createMissionUseCase: CreateMissionUseCase,
    private val submitQuizMissionUseCase: SubmitQuizMissionUseCase,
    private val submitSelfieMissionUseCase: SubmitSelfieMissionUseCase,
    private val validateSelfieUseCase: ValidateSelfieUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MissionDetailUiState())
    val uiState: StateFlow<MissionDetailUiState> = _uiState.asStateFlow()

    fun createMission(teamId: Long, spotId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = createMissionUseCase(teamId, spotId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        missionCreateResult = result.data,
                        problemDetail = result.data.problemDetail
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun selectAnswer(answerIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedAnswer = answerIndex)
    }

    fun submitQuizAnswer() {
        val currentState = _uiState.value
        val missionId = currentState.missionCreateResult?.missionId ?: return
        val selectedAnswer = currentState.selectedAnswer ?: return
        val problemDetail = currentState.problemDetail as? QuizProblem ?: return
        
        // 정답 확인
        val isCorrect = selectedAnswer == problemDetail.correctIndex
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isAnswerSubmitted = true)
            
            when (val result = submitQuizMissionUseCase(missionId, isCorrect)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        submitResult = result.data,
                        isCompleted = true
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    // TODO: 셀피 관련 메서드들 (백엔드 API 완성 후 구현)
    fun setCapturedImage(imageBase64: String) {
        _uiState.value = _uiState.value.copy(capturedImageBase64 = imageBase64)
    }

    fun validateSelfie() {
        val imageBase64 = _uiState.value.capturedImageBase64 ?: return
        val problemDetail = _uiState.value.problemDetail as? SelfieProblem ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSelfieValidating = true)
            
            when (val result = validateSelfieUseCase(imageBase64, problemDetail.pose)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSelfieValidating = false,
                        selfieValidationResult = result.data
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSelfieValidating = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isSelfieValidating = true)
                }
            }
        }
    }

    fun submitSelfieMission() {
        val currentState = _uiState.value
        val missionId = currentState.missionCreateResult?.missionId ?: return
        val imageBase64 = currentState.capturedImageBase64 ?: return
        val problemDetail = currentState.problemDetail as? SelfieProblem ?: return
        val isValid = currentState.selfieValidationResult ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = submitSelfieMissionUseCase(missionId, imageBase64, problemDetail.pose)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        submitResult = result.data,
                        isCompleted = true
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetMission() {
        _uiState.value = MissionDetailUiState()
    }
}