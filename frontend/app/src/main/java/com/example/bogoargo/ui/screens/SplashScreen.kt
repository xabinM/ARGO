package com.example.bogoargo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bogoargo.domain.repository.IAuthRepository
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bogoargo.data.preferences.UserPreferences
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
    val authRepository = splashViewModel.authRepository

    LaunchedEffect(Unit) {
        delay(1000)
        
        val tokenInfo = authRepository.getTokenInfo()
        val user = loginViewModel.getLoggedInUser()

        if (tokenInfo != null) {
            // 토큰이 있으면 서버에 검증 요청
            try {
                val response = splashViewModel.validateToken()
                if (response.isSuccessful && user != null) {

                    // 토큰이 유효하고 유저 데이터 있으면 각 홈으로 이동
                    if(user.role == UserRole.ROLE_TEACHER) {
                        navController.navigate("teacherHome") {
                            popUpTo("splash"){ inclusive = true }
                        }
                    } else {
                        navController.navigate("studentHome") {
                            popUpTo("splash"){ inclusive = true }
                        }
                    }

                } else {
                    // 토큰이 무효하면 토큰 삭제 후 로그인으로 이동
                    authRepository.clearTokens()
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            } catch (e: Exception) {
                // 네트워크 에러 등의 경우 로그인으로 이동
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        } else {
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