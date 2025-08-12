package com.example.bogoargo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.ui.screens.mission.components.*
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.viewmodels.MissionDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionDetailScreen(
    spotId: Long,
    classId: Long,
    teamId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToGame: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MissionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 미션 생성 (화면 진입 시)
    LaunchedEffect(spotId, teamId) {
        viewModel.createMission(teamId, spotId)
    }

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "🎯 미션 도전",
                emoji = "🗺️",
                onNavigationClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    uiState.isLoading -> {
                        LoadingSection()
                    }
                    uiState.errorMessage != null -> {
                        ErrorSection(
                            errorMessage = uiState.errorMessage!!,
                            onRetry = { viewModel.createMission(teamId, spotId) },
                            onDismiss = { viewModel.clearError() }
                        )
                    }
                    uiState.isCompleted -> {
                        MissionCompletedSection(
                            submitResult = uiState.submitResult,
                            isMissionSuccessful = uiState.isMissionSuccessful ?: false,
                            onNavigateBack = onNavigateBack,
                            onNavigateToGame = onNavigateToGame,
                            onRetryMission = { viewModel.retryMission() }
                        )
                    }
                    uiState.problemDetail != null -> {
                        when (val problem = uiState.problemDetail) {
                            is QuizProblem -> {
                                QuizMissionSection(
                                    problem = problem,
                                    selectedAnswer = uiState.selectedAnswer,
                                    isAnswerSubmitted = uiState.isAnswerSubmitted,
                                    onAnswerSelected = viewModel::selectAnswer,
                                    onSubmitAnswer = viewModel::checkAnswer,
                                    onRetryQuiz = viewModel::retryQuiz,
                                    isLoading = uiState.isLoading,
                                    attemptCount = uiState.attemptCount,
                                    showRetryButton = uiState.showRetryButton,
                                    canSubmitAnswer = uiState.canSubmitAnswer,
                                    showCorrectAnswer = uiState.showCorrectAnswer
                                )
                            }
                            is SelfieProblem -> {
                                SelfieMissionSection(
                                    problem = problem,
                                    capturedImage = uiState.capturedImageBase64,
                                    validationResult = uiState.selfieValidationResult,
                                    isValidating = uiState.isSelfieValidating,
                                    isLoading = uiState.isLoading,
                                    onImageCaptured = viewModel::setCapturedImage,
                                    onValidateSelfie = viewModel::validateSelfie,
                                    onSubmitMission = viewModel::submitSelfieMission
                                )
                            }
                            null -> {
                                // problemDetail이 null인 경우 (이미 위에서 체크했지만)
                            }
                        }
                    }
                    else -> {
                        // 기본 상태
                    }
                }
            }
        }
    }
}