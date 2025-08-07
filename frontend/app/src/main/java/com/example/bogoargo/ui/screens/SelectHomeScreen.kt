package com.example.bogoargo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.data.preferences.UserPreferences
import com.example.bogoargo.domain.model.UserRole
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectHomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var currentUser by remember { mutableStateOf(null as com.example.bogoargo.domain.model.User?) }
    
    // 사용자 정보 로드 - LoginUseCase를 이용
    val loginUseCase = hiltViewModel<com.example.bogoargo.ui.viewmodels.user.LoginViewModel>()
    
    LaunchedEffect(Unit) {
        currentUser = loginUseCase.getLoggedInUser()
    }
    
    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "홈 선택",
                emoji = "🏠",
                onNavigationClick = { /* 뒤로가기 비활성화 */ }
            )
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 환영 메시지 카드
                NatureComponents.NatureCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        NatureComponents.ProfileAvatar(
                            emoji = if (currentUser?.role == UserRole.ROLE_TEACHER) "👩‍🏫" else "👶",
                            backgroundColor = NatureColors.leafGreen,
                            size = 80.dp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "${currentUser?.name ?: "사용자"}님, 환영합니다!",
                            style = NatureTypography.titleLarge
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "홈 화면을 선택해주세요",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.earthBrown.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
                
                // 홈 선택 버튼들
                NatureComponents.NatureCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "🏠 홈 화면 선택",
                            style = NatureTypography.titleMedium
                        )
                        
                        // 선생님 홈 버튼
                        NatureComponents.NatureButton(
                            onClick = {
                                navController.navigate("teacherHome") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            text = "👩‍🏫 선생님 홈",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = currentUser?.role == UserRole.ROLE_TEACHER,
                            backgroundColor = if (currentUser?.role == UserRole.ROLE_TEACHER) {
                                NatureColors.forestGreen
                            } else {
                                NatureColors.earthBrown.copy(alpha = 0.3f)
                            }
                        )
                        
                        // 학생 홈 버튼
                        NatureComponents.NatureButton(
                            onClick = {
                                navController.navigate("studentHome") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            text = "👶 학생 홈",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = currentUser?.role == UserRole.ROLE_STUDENT,
                            backgroundColor = if (currentUser?.role == UserRole.ROLE_STUDENT) {
                                NatureColors.leafGreen
                            } else {
                                NatureColors.earthBrown.copy(alpha = 0.3f)
                            }
                        )
                        
                        // 역할 정보 텍스트
                        currentUser?.let { user ->
                            Text(
                                text = "현재 역할: ${if (user.role == UserRole.ROLE_TEACHER) "선생님" else "학생"}",
                                style = NatureTypography.bodySmall.copy(
                                    color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectHomeScreenPreview() {
    MaterialTheme {
        SelectHomeScreen(navController = rememberNavController())
    }
}