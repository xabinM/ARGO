package com.example.bogoargo.ui.screens.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.domain.model.UserRole
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureElevation
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.viewmodels.user.TeacherHomeViewModel
import com.example.bogoargo.ui.viewmodels.user.LogoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherHomeScreen(
    navController: NavController,
    viewModel: TeacherHomeViewModel = hiltViewModel(),
    logoutViewModel: LogoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val logoutUiState by logoutViewModel.uiState.collectAsState()
    
    // 로그아웃 성공 시 로그인 화면으로 이동
    LaunchedEffect(logoutUiState.isLoggedOut) {
        if (logoutUiState.isLoggedOut) {
            navController.navigate("login") {
                popUpTo("teacherHome") { inclusive = true }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🌳 선생님 홈",
                        style = NatureTypography.titleLarge
                    )
                },
                actions = {
                    IconButton(
                        onClick = { logoutViewModel.logout() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "로그아웃",
                            tint = NatureColors.earthBrown
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NatureColors.warmBeige
                )
            )
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. 상단 유저 정보 영역 (그라데이션 헤더)
                NatureComponents.HeaderCard(
                    title = uiState.currentUser?.name?.let { "$it 님" } ?: "로딩중...",
                    subtitle = "환영합니다! 오늘도 즐거운 수업 되세요",
                    emoji = "👩‍🏫",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    startColor = NatureColors.earthBrown,
                    endColor = NatureColors.sunnyYellow
                )

                // 2. 메인 메뉴 버튼들 (그라데이션 카드 버튼들)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 우리 반 관리 버튼
                    NatureComponents.GradientCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                            .clickable {
                                navController.navigate("classManagement")
                            },
                        startColor = NatureColors.leafGreen,
                        endColor = NatureColors.lightGreen,
                        shape = NatureShapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🏫",
                                fontSize = 40.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "우리 반 관리",
                                style = NatureTypography.titleMedium.copy(color = Color.White),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // 미션 추가 생성 버튼
                    NatureComponents.GradientCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                            .clickable {
                                navController.navigate("classSelectionForProblemScreen")
                            },
                        startColor = NatureColors.sunnyYellow,
                        endColor = NatureColors.softOrange,
                        shape = NatureShapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🎒",
                                fontSize = 40.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "미션 추가 생성",
                                style = NatureTypography.titleMedium.copy(color = Color.White),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // 3. 사용자 상세 정보 영역
                NatureComponents.NatureCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = NatureElevation.large,
                    shape = NatureShapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        NatureComponents.SectionHeader(
                            text = "내 정보",
                            emoji = "📋"
                        )
                        
                        if (uiState.isLoading) {
                            Box(
                                modifier = Modifier.height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = NatureColors.leafGreen
                                )
                            }
                        } else if (uiState.errorMessage != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "⚠️",
                                    fontSize = 32.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "정보를 불러올 수 없어요",
                                    style = NatureTypography.titleMedium
                                )
                                Text(
                                    text = uiState.errorMessage.toString(),
                                    style = NatureTypography.bodySmall,
                                    color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.refreshUserInfo() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NatureColors.leafGreen
                                    )
                                ) {
                                    Text("다시 시도")
                                }
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                InfoItem(
                                    emoji = "👤",
                                    label = "이름",
                                    value = uiState.currentUser?.name ?: "정보 없음"
                                )
                                InfoItem(
                                    emoji = "🏷️",
                                    label = "역할",
                                    value = when(uiState.currentUser?.role) {
                                        UserRole.ROLE_TEACHER -> "선생님"
                                        UserRole.ROLE_STUDENT -> "학생"
                                        else -> "정보 없음"
                                    }
                                )
                                InfoItem(
                                    emoji = "🆔",
                                    label = "사용자 ID",
                                    value = uiState.currentUser?.name?.toString() ?: "정보 없음"
                                )
                            }
                        }
                    }
                }
                
                // 하단 여백 추가
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }


}

@Composable
fun InfoItem(
    emoji: String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            fontSize = 20.sp
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = NatureTypography.labelMedium.copy(
                    color = NatureColors.earthBrown.copy(alpha = 0.7f)
                )
            )
            Text(
                text = value,
                style = NatureTypography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTeacherMainScreen() {
    val navController = rememberNavController()
    TeacherHomeScreen(navController = navController)
}