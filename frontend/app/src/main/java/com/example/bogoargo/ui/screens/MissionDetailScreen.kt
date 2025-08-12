package com.example.bogoargo.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.viewmodels.MissionDetailViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MissionDetailScreen(
    spotId: Long,
    classId: Long,
    teamId: Long,
    onNavigateBack: () -> Unit,
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
                            onNavigateBack = onNavigateBack
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
                                    onSubmitAnswer = viewModel::submitQuizAnswer,
                                    isLoading = uiState.isLoading
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

@Composable
private fun LoadingSection() {
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = NatureColors.forestGreen
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "🎯 미션을 준비하고 있어요...",
                style = NatureTypography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorSection(
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚠️",
                style = NatureTypography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "미션 생성 실패",
                style = NatureTypography.titleMedium,
                color = NatureColors.earthBrown
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = errorMessage,
                style = NatureTypography.bodyMedium,
                color = NatureColors.earthBrown.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NatureComponents.NatureButton(
                    onClick = onRetry,
                    text = "다시 시도",
                    modifier = Modifier.weight(1f),
                    backgroundColor = NatureColors.forestGreen
                )
                NatureComponents.NatureButton(
                    onClick = onDismiss,
                    text = "확인",
                    modifier = Modifier.weight(1f),
                    backgroundColor = NatureColors.softOrange
                )
            }
        }
    }
}

@Composable
private fun QuizMissionSection(
    problem: QuizProblem,
    selectedAnswer: Int?,
    isAnswerSubmitted: Boolean,
    onAnswerSelected: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    isLoading: Boolean
) {
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "📚 퀴즈 미션",
                style = NatureTypography.titleLarge,
                color = NatureColors.forestGreen
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = problem.question,
                style = NatureTypography.bodyLarge,
                color = NatureColors.earthBrown
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                problem.choices.forEachIndexed { index, choice ->
                    val isSelected = selectedAnswer == index
                    val backgroundColor = when {
                        isAnswerSubmitted && index == problem.correctIndex -> NatureColors.leafGreen.copy(alpha = 0.3f)
                        isAnswerSubmitted && isSelected && index != problem.correctIndex -> Color.Red.copy(alpha = 0.3f)
                        isSelected -> NatureColors.forestGreen.copy(alpha = 0.2f)
                        else -> NatureColors.lightBeige.copy(alpha = 0.1f)
                    }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = { if (!isAnswerSubmitted) onAnswerSelected(index) },
                                role = Role.RadioButton,
                                enabled = !isAnswerSubmitted
                            ),
                        colors = CardDefaults.cardColors(containerColor = backgroundColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                                enabled = !isAnswerSubmitted,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NatureColors.forestGreen
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = choice,
                                style = NatureTypography.bodyMedium,
                                color = NatureColors.earthBrown
                            )
                            
                            if (isAnswerSubmitted) {
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = when {
                                        index == problem.correctIndex -> "✅"
                                        isSelected && index != problem.correctIndex -> "❌"
                                        else -> ""
                                    },
                                    style = NatureTypography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
            
            if (isAnswerSubmitted && problem.explanation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = NatureColors.leafGreen.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "💡 설명",
                            style = NatureTypography.titleSmall,
                            color = NatureColors.forestGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = problem.explanation,
                            style = NatureTypography.bodyMedium,
                            color = NatureColors.earthBrown
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            NatureComponents.NatureButton(
                onClick = onSubmitAnswer,
                text = if (isLoading) "제출 중..." else "정답 제출",
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedAnswer != null && !isAnswerSubmitted && !isLoading,
                backgroundColor = NatureColors.forestGreen
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun SelfieMissionSection(
    problem: SelfieProblem,
    capturedImage: String?,
    validationResult: Boolean?,
    isValidating: Boolean,
    isLoading: Boolean,
    onImageCaptured: (String) -> Unit,
    onValidateSelfie: () -> Unit,
    onSubmitMission: () -> Unit
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    // 카메라 촬영 결과를 처리하는 launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            // Bitmap을 Base64로 변환
            val outputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
            onImageCaptured(base64String)
        }
    }
    
    // 카메라 권한 요청 launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        }
    }
    
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📸 셀피 미션",
                style = NatureTypography.titleLarge,
                color = NatureColors.forestGreen
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = problem.guideline,
                style = NatureTypography.bodyLarge,
                color = NatureColors.earthBrown,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = NatureColors.leafGreen.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🤳 포즈 가이드",
                        style = NatureTypography.titleSmall,
                        color = NatureColors.forestGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = problem.poseHint,
                        style = NatureTypography.bodyMedium,
                        color = NatureColors.earthBrown,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 촬영된 이미지 표시
            if (capturedImage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NatureColors.lightBeige.copy(alpha = 0.3f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val bitmap = remember(capturedImage) {
                            try {
                                val imageBytes = Base64.decode(capturedImage, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "촬영된 사진",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            Text(
                                text = "🖼️ 사진이 촬영되었습니다",
                                style = NatureTypography.bodyMedium,
                                color = NatureColors.earthBrown
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 검증 결과 표시
                if (validationResult != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (validationResult) 
                                NatureColors.leafGreen.copy(alpha = 0.2f) 
                            else 
                                Color.Red.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (validationResult) "✅ 포즈 확인!" else "❌ 포즈가 일치하지 않아요",
                                style = NatureTypography.bodyMedium,
                                color = NatureColors.earthBrown
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 버튼들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 다시 촬영 버튼
                    NatureComponents.NatureButton(
                        onClick = {
                            if (cameraPermissionState.status.isGranted) {
                                takePictureLauncher.launch(null)
                            } else {
                                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        text = "다시 촬영",
                        modifier = Modifier.weight(1f),
                        backgroundColor = NatureColors.softOrange
                    )
                    
                    // 검증 또는 제출 버튼
                    if (validationResult == null) {
                        NatureComponents.NatureButton(
                            onClick = onValidateSelfie,
                            text = if (isValidating) "검증 중..." else "포즈 검증",
                            modifier = Modifier.weight(1f),
                            enabled = !isValidating,
                            backgroundColor = NatureColors.forestGreen
                        )
                    } else if (validationResult) {
                        NatureComponents.NatureButton(
                            onClick = onSubmitMission,
                            text = if (isLoading) "제출 중..." else "미션 완료",
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            backgroundColor = NatureColors.forestGreen
                        )
                    }
                }
            } else {
                // 촬영 버튼
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NatureColors.lightBeige.copy(alpha = 0.1f)
                    ),
                    onClick = {
                        if (cameraPermissionState.status.isGranted) {
                            takePictureLauncher.launch(null)
                        } else {
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "카메라",
                                modifier = Modifier.size(48.dp),
                                tint = NatureColors.forestGreen
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "사진 촬영하기",
                                style = NatureTypography.bodyLarge,
                                color = NatureColors.forestGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionCompletedSection(
    submitResult: MissionSubmitResult?,
    onNavigateBack: () -> Unit
) {
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎉",
                style = NatureTypography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "미션 완료!",
                style = NatureTypography.titleLarge,
                color = NatureColors.forestGreen
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (submitResult?.successful == true) {
                Text(
                    text = "축하합니다! 미션을 성공적으로 완료했어요 🎊",
                    style = NatureTypography.bodyMedium,
                    color = NatureColors.earthBrown,
                    textAlign = TextAlign.Center
                )
                
                submitResult.cardId?.let { cardId ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = NatureColors.leafGreen.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🎴 획득한 카드",
                                style = NatureTypography.titleSmall,
                                color = NatureColors.forestGreen
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "카드 ID: $cardId",
                                style = NatureTypography.bodyMedium
                            )
                            submitResult.tier?.let { tier ->
                                Text(
                                    text = "등급: $tier",
                                    style = NatureTypography.bodyMedium
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "미션을 완료했지만 결과를 확인할 수 없어요. 다시 시도해보세요.",
                    style = NatureTypography.bodyMedium,
                    color = NatureColors.earthBrown.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            NatureComponents.NatureButton(
                onClick = onNavigateBack,
                text = "완료",
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NatureColors.forestGreen
            )
        }
    }
}