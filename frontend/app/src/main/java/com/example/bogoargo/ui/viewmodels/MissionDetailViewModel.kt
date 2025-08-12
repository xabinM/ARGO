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
    val isMissionSuccessful: Boolean? = null, // 미션 성공/실패 상태
    
    // 퀴즈 2회 기회 시스템 관련 상태
    val attemptCount: Int = 0, // 시도 횟수 (0, 1, 2)
    val showRetryButton: Boolean = false, // 재도전 버튼 표시 여부
    val isFinalAttempt: Boolean = false, // 마지막 시도 여부
    val canSubmitAnswer: Boolean = true, // 답안 제출 가능 여부
    val showCorrectAnswer: Boolean = false, // 정답 공개 여부 (2차 실패 시에만 true)
    
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

    fun checkAnswer() {
        val currentState = _uiState.value
        val selectedAnswer = currentState.selectedAnswer ?: return
        val problemDetail = currentState.problemDetail as? QuizProblem ?: return
        
        // 정답 확인
        val isCorrect = selectedAnswer == problemDetail.correctIndex
        
        // 답안 제출 상태로 변경
        _uiState.value = _uiState.value.copy(isAnswerSubmitted = true)
        
        if (isCorrect) {
            // 정답이면 즉시 성공 API 호출
            submitSuccessfulMission()
        } else {
            // 오답이면 시도 횟수에 따라 처리
            handleWrongAnswer()
        }
    }
    
    private fun handleWrongAnswer() {
        val currentState = _uiState.value
        val newAttemptCount = currentState.attemptCount + 1
        
        if (newAttemptCount == 1) {
            // 1차 실패: 재도전 기회 제공 (정답은 공개하지 않음)
            _uiState.value = _uiState.value.copy(
                attemptCount = newAttemptCount,
                showRetryButton = true,
                canSubmitAnswer = false,
                showCorrectAnswer = false // 1차 실패시에는 정답 비공개
            )
        } else {
            // 2차 실패: 정답 공개 후 미션 실패로 처리
            _uiState.value = _uiState.value.copy(
                attemptCount = newAttemptCount,
                canSubmitAnswer = false,
                showCorrectAnswer = true // 2차 실패시에만 정답 공개
            )
            submitFailedMission()
        }
    }
    
    private fun submitSuccessfulMission() {
        val currentState = _uiState.value
        val missionId = currentState.missionCreateResult?.missionId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = submitQuizMissionUseCase(missionId, true)) {
                is DataResult.Success -> {
                    val missionResult = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        submitResult = missionResult,
                        isCompleted = true,
                        isMissionSuccessful = true
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
    
    private fun submitFailedMission() {
        val currentState = _uiState.value
        val missionId = currentState.missionCreateResult?.missionId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = submitQuizMissionUseCase(missionId, false)) {
                is DataResult.Success -> {
                    val missionResult = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        submitResult = missionResult,
                        isCompleted = true,
                        isMissionSuccessful = false
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
    
    fun retryQuiz() {
        _uiState.value = _uiState.value.copy(
            selectedAnswer = null,
            isAnswerSubmitted = false,
            showRetryButton = false,
            isFinalAttempt = true,
            canSubmitAnswer = true,
            showCorrectAnswer = false // 재도전 시 정답 비공개
        )
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
                    val missionResult = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        submitResult = missionResult,
                        isCompleted = true,
                        isMissionSuccessful = missionResult.successful
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
    
    fun retryMission() {
        _uiState.value = _uiState.value.copy(
            selectedAnswer = null,
            isAnswerSubmitted = false,
            submitResult = null,
            isMissionSuccessful = null,
            isCompleted = false,
            errorMessage = null,
            attemptCount = 0,
            showRetryButton = false,
            isFinalAttempt = false,
            canSubmitAnswer = true,
            showCorrectAnswer = false // 미션 재시작 시 정답 비공개
        )
    }
}