package com.example.bogoargo.ui.screens.user

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.domain.model.User
import com.example.bogoargo.domain.model.UserRole
import com.example.bogoargo.navigation.Screen
import com.example.bogoargo.ui.viewmodels.user.LoginViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NotificationType
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var showLocationPermissionDialog by remember { mutableStateOf(false) }
    
    // 위치 권한 요청 launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            // 권한이 승인되면 다이얼로그 닫기
            showLocationPermissionDialog = false
        }
    }
    
    // 화면 진입 시 위치 권한 체크
    LaunchedEffect(Unit) {
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (!hasLocationPermission) {
            showLocationPermissionDialog = true
        }
    }
    
    // NavigationEvent 처리
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { screen ->
            navController.navigate(screen.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }
    
    NatureComponents.NatureBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            // 개선된 헤더 카드 (그라데이션 배경)
            NatureComponents.HeaderCard(
                title = "로그인",
                subtitle = "게임과 함께하는 즐거운 학습",
                emoji = "🌱",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
            
            // 로그인 폼 카드
            NatureComponents.NatureCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🔑 계정 정보를 입력해주세요",
                        style = NatureTypography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = viewModel::updateUsername,
                        label = { 
                            Text(
                                text = "아이디",
                                style = NatureTypography.bodyMedium
                            ) 
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        enabled = !uiState.isLoading,
                        shape = NatureShapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NatureColors.forestGreen,
                            focusedLabelColor = NatureColors.forestGreen,
                            unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                        )
                    )
                    
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::updatePassword,
                        label = { 
                            Text(
                                "비밀번호", 
                                style = NatureTypography.bodyMedium
                            ) 
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (passwordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff
                            
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = image,
                                    contentDescription = if (passwordVisible) "비밀번호 숨기기" else "비밀번호 보기",
                                    tint = NatureColors.earthBrown
                                )
                            }
                        },
                        enabled = !uiState.isLoading,
                        shape = NatureShapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NatureColors.forestGreen,
                            focusedLabelColor = NatureColors.forestGreen,
                            unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = NatureColors.forestGreen
                            )
                        }
                    } else {
                        NatureComponents.ActionButton(
                            onClick = viewModel::login,
                            text = "로그인",
                            emoji = "🌱",
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.username.isNotEmpty() && uiState.password.isNotEmpty()
                        )
                    }
                }
            }
            
            // 에러 메시지 (개선된 알림 배너)
            uiState.errorMessage?.let { errorMessage ->
                Spacer(modifier = Modifier.height(16.dp))
                NatureComponents.NotificationBanner(
                    message = errorMessage,
                    emoji = "⚠️",
                    type = NotificationType.ERROR,
                    onDismiss = viewModel::clearErrorMessage
                )
            }
            
            // 회원가입 링크
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "아직 계정이 없으신가요?",
                    style = NatureTypography.bodyMedium.copy(
                        color = NatureColors.earthBrown.copy(alpha = 0.8f)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        navController.navigate("signUp")
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = NatureColors.forestGreen
                    )
                ) {
                    Text(
                        "회원가입",
                        style = NatureTypography.bodyMedium
                    )
                }
            }
            
            // 하단 여백
            Spacer(modifier = Modifier.height(40.dp))
        }
        
        // 위치 권한 요청 다이얼로그
        if (showLocationPermissionDialog) {
            LocationPermissionDialog(
                onRequestPermission = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onDismiss = { showLocationPermissionDialog = false }
            )
        }
    }
}

@Composable
fun LocationPermissionDialog(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "📍 위치 권한 필요",
                style = NatureTypography.titleMedium,
                color = NatureColors.forestGreen
            )
        },
        text = {
            Column {
                Text(
                    text = "위치 기반 학습 게임을 위해 위치 권한이 필요합니다.",
                    style = NatureTypography.bodyMedium,
                    color = NatureColors.earthBrown
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• 미션 지점 찾기\n• 팀원 위치 공유\n• 학습 진도 추적",
                    style = NatureTypography.bodySmall,
                    color = NatureColors.earthBrown.copy(alpha = 0.8f)
                )
            }
        },
        confirmButton = {
            NatureComponents.NatureButton(
                onClick = onRequestPermission,
                text = "권한 허용",
                backgroundColor = NatureColors.forestGreen
            )
        },
        dismissButton = {
            NatureComponents.NatureButton(
                onClick = onDismiss,
                text = "나중에",
                backgroundColor = NatureColors.earthBrown.copy(alpha = 0.3f)
            )
        },
        containerColor = NatureColors.whiteTransparent90
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(navController = rememberNavController())
    }
}