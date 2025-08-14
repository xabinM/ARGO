package com.example.bogoargo.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bogoargo.domain.model.UserRole
import com.example.bogoargo.ui.viewmodels.SplashViewModel
import com.example.bogoargo.ui.viewmodels.user.LoginViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureTypography

@Composable
fun SplashScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val splashViewModel: SplashViewModel = hiltViewModel()
    val loginViewModel: LoginViewModel = hiltViewModel()
    
    // Animation states
    val infiniteTransition = rememberInfiniteTransition()
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val logoRotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Fade in animation for the entire screen
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        delay(2000) // Increased delay for better UX
        
        // 로컬 세션 유효성 확인
        if (splashViewModel.hasValidSession()) {
            val user = loginViewModel.getLoggedInUser()
            
            if (user != null) {
                // 로컬 세션이 유효하면 SelectHomeScreen으로 이동
                // 토큰 만료 등의 문제는 실제 API 호출 시점에 TokenManagementInterceptor가 처리
                navController.navigate("selectHome") {
                    popUpTo("splash") { inclusive = true }
                }
            } else {
                // 사용자 정보 없으면 데이터 삭제 후 로그인으로 이동
                splashViewModel.clearUserData()
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        } else {
            // 세션이 유효하지 않으면 데이터 삭제 후 로그인으로 이동
            splashViewModel.clearUserData()
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }
    
    NatureComponents.NatureBackground {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Background gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NatureColors.sunnyYellow.copy(alpha = 0.3f),
                                NatureColors.forestGreen.copy(alpha = 0.2f),
                                NatureColors.leafGreen.copy(alpha = 0.1f)
                            )
                        )
                    )
            )
            
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(1000)) + scaleIn(animationSpec = tween(1000))
            ) {
                Card(
                    modifier = Modifier
                        .padding(32.dp)
                        .shadow(12.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Animated logo section
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            NatureColors.sunnyYellow.copy(alpha = 0.3f),
                                            NatureColors.forestGreen.copy(alpha = 0.2f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🌍🗺️",
                                fontSize = 48.sp,
                                modifier = Modifier
                                    .scale(logoScale)
                                    .rotate(logoRotation)
                            )
                        }
                        
                        // App title
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Argo",
                                style = NatureTypography.headlineMedium.copy(
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = NatureColors.forestGreen
                            )
                            
                            Text(
                                text = "🎒 AR 체험학습 대모험!",
                                style = NatureTypography.titleMedium.copy(
                                    fontSize = 20.sp
                                ),
                                color = NatureColors.earthBrown,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        // Description
                        Text(
                            text = "친구들과 함께 신나는\n현장체험학습을 준비하고 있어요!",
                            style = NatureTypography.bodyMedium.copy(
                                fontSize = 16.sp
                            ),
                            color = NatureColors.earthBrown.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        
                        // Loading section
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            NatureComponents.NatureLoadingIndicator()
                            
                            Text(
                                text = "로딩 중...",
                                style = NatureTypography.bodySmall,
                                color = NatureColors.forestGreen
                            )
                        }
                        
                        // Fun emoji decoration
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            listOf("🌱", "🔍", "🏆", "✨").forEach { emoji ->
                                Text(
                                    text = emoji,
                                    fontSize = 24.sp,
                                    modifier = Modifier
                                        .scale(logoScale * 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}