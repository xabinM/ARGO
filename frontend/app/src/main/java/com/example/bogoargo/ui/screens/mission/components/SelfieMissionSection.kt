package com.example.bogoargo.ui.screens.mission.components

import android.Manifest
import android.graphics.Bitmap
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bogoargo.domain.model.SelfieProblem
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureTypography
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SelfieMissionSection(
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
            
            // Pose hint card
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
            
            // Display captured image or camera button
            if (capturedImage != null) {
                CapturedImageDisplay(
                    capturedImageBase64 = capturedImage
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Show validation result if available
                validationResult?.let { result ->
                    SelfieValidationResult(
                        validationResult = result
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Retake photo button
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
                    
                    // Validate or submit button
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
                // Show camera button when no image is captured
                CameraButton(
                    onClick = {
                        if (cameraPermissionState.status.isGranted) {
                            takePictureLauncher.launch(null)
                        } else {
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )
            }
        }
    }
}