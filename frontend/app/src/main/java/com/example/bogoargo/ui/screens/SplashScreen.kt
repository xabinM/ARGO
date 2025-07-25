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
import com.example.bogoargo.data.api.ApiClient
import com.example.bogoargo.data.repository.AuthRepository
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authRepository = AuthRepository(context)
    
    LaunchedEffect(Unit) {
        delay(1000)
        
        val tokenInfo = authRepository.getTokenInfo()
        if (tokenInfo != null) {
            // 토큰이 있으면 서버에 검증 요청
            try {
                val response = ApiClient.authApiService.validateToken()
                if (response.isSuccessful) {
                    // 토큰이 유효하면 홈으로 이동
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
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