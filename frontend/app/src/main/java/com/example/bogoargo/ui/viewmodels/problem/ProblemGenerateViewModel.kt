package com.example.bogoargo.ui.viewmodels.problem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.dto.request.ProblemGenerateRequest
import com.example.bogoargo.data.dto.request.ProblemRegisterRequest
import com.example.bogoargo.data.dto.response.ProblemDataQuizDto
import com.example.bogoargo.data.dto.response.ProblemData
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.Spot
import com.example.bogoargo.domain.use_case.classroom.GetClassDetailUseCase
import com.example.bogoargo.domain.use_case.problem.GenerateProblemUseCase
import com.example.bogoargo.domain.use_case.problem.RegisterProblemUseCase
import com.example.bogoargo.domain.use_case.spot.GetSpotListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.getOrNull

data class ProblemGenerateUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val spots: List<Spot> = emptyList(),
    val selectedSpot: Spot? = null,
    val problemCount: Int = 1,
    val generatedProblems: List<ProblemDataQuizDto> = emptyList(),
    val currentProblemIndex: Int = 0,
    val registeredProblemsCount: Int = 0,
    val isGeneratingProblems: Boolean = false,
    val isRegisteringProblem: Boolean = false,
    val showProblemCards: Boolean = false,
    val grade: Int = 1
)

@HiltViewModel
class ProblemGenerateViewModel @Inject constructor(
    private val getSpotListUseCase: GetSpotListUseCase,
    private val getClassDetailUseCase: GetClassDetailUseCase,
    private val generateProblemUseCase: GenerateProblemUseCase,
    private val registerProblemUseCase: RegisterProblemUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProblemGenerateUiState())
    val uiState: StateFlow<ProblemGenerateUiState> = _uiState.asStateFlow()

    fun loadInitialData(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val classResult = getClassDetailUseCase(classId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(grade = classResult.data.grade!!)

                    when (val spotResult = getSpotListUseCase(classId)) {
                        is DataResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                spots = spotResult.data
                            )
                        }
                        is DataResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = spotResult.exception.message ?: "스팟 목록을 불러올 수 없습니다."
                            )
                        }
                        else -> Unit
                    }
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = classResult.exception.message ?: "클래스 정보를 불러올 수 없습니다."
                    )
                }
                else -> Unit
            }
        }
    }

    fun selectSpot(spot: Spot) {
        _uiState.value = _uiState.value.copy(selectedSpot = spot)
    }

    fun updateProblemCount(count: Int) {
        val validCount = count.coerceIn(1, 5)
        _uiState.value = _uiState.value.copy(problemCount = validCount)
    }

    fun generateProblems() {
        val currentState = _uiState.value
        val selectedSpot = currentState.selectedSpot ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingProblems = true, errorMessage = null)

            val request = ProblemGenerateRequest(
                spotId = selectedSpot.spotId,
                grade = currentState.grade,
                problemCnt = currentState.problemCount
            )

            when (val result = generateProblemUseCase(
                request.spotId, request.grade, request.problemCnt
            )) {
                is DataResult.Success -> {
                    val quizProblems = (result.data.problems.problems as? List<ProblemDataQuizDto>)
                        ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isGeneratingProblems = false,
                        generatedProblems = quizProblems,
                        showProblemCards = true,
                        currentProblemIndex = 0,
                        registeredProblemsCount = 0
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingProblems = false,
                        errorMessage = result.exception.message ?: "문제 생성에 실패했습니다."
                    )
                }
                else -> Unit
            }
        }
    }

    fun registerCurrentProblem() {
        val currentState = _uiState.value
        val currentProblem = currentState.generatedProblems.getOrNull(currentState.currentProblemIndex) ?: return
        val selectedSpot = currentState.selectedSpot ?: return

        // 이제 currentProblem이 직접 ProblemDataQuizDto 타입
        val quizProblem = currentProblem

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRegisteringProblem = true)

            val request = ProblemRegisterRequest(
                question = quizProblem.question,
                choices = quizProblem.choices,
                correctIndex = quizProblem.correctIndex,
                explanation = quizProblem.explanation,
                spotId = selectedSpot.spotId,
                grade = currentState.grade.toLong()
            )

            when (val result = registerProblemUseCase(
                request.spotId,
                request.question,
                request.choices,
                request.correctIndex,
                request.explanation,
                request.grade
            )) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isRegisteringProblem = false,
                        registeredProblemsCount = currentState.registeredProblemsCount + 1
                    )
                    goToNextProblem()
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isRegisteringProblem = false,
                        errorMessage = result.exception.message ?: "문제 등록에 실패했습니다."
                    )
                }
                else -> Unit
            }
        }
    }

    fun skipCurrentProblem() {
        goToNextProblem()
    }

    private fun goToNextProblem() {
        val currentState = _uiState.value
        val nextIndex = currentState.currentProblemIndex + 1

        if (nextIndex >= currentState.generatedProblems.size) {
            _uiState.value = _uiState.value.copy(
                showProblemCards = false,
                currentProblemIndex = 0
            )
        } else {
            _uiState.value = _uiState.value.copy(currentProblemIndex = nextIndex)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetGeneration() {
        _uiState.value = _uiState.value.copy(
            generatedProblems = emptyList(),
            currentProblemIndex = 0,
            registeredProblemsCount = 0,
            showProblemCards = false,
            selectedSpot = null,
            problemCount = 1
        )
    }
}
