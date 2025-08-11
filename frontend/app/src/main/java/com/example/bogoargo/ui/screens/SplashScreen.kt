package com.example.bogoargo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bogoargo.domain.model.UserRole
import com.example.bogoargo.ui.viewmodels.SplashViewModel
import com.example.bogoargo.ui.viewmodels.user.LoginViewModel

@Composable
fun SplashScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val splashViewModel: SplashViewModel = hiltViewModel()
    val loginViewModel: LoginViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        delay(1000)
        
        // 로컬 세션 유효성 확인
        if (splashViewModel.hasValidSession()) {
            val user = loginViewModel.getLoggedInUser()
            
            if (user != null) {
                // 로컬 세션이 유효하면 바로 홈으로 이동
                // 토큰 만료 등의 문제는 실제 API 호출 시점에 TokenManagementInterceptor가 처리
                if (user.role == UserRole.ROLE_TEACHER) {
                    navController.navigate("teacherHome") {
                        popUpTo("splash") { inclusive = true }
                    }
                } else {
                    navController.navigate("studentHome") {
                        popUpTo("splash") { inclusive = true }
                    }
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
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Argo",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}