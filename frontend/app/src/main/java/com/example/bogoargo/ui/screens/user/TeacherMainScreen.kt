package com.example.bogoargo.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.data.model.UserRole
import com.example.bogoargo.ui.viewmodels.user.TeacherMainViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation
// R.drawable.profile_placeholder와 같은 리소스 ID를 사용하려면
// res/drawable 폴더에 이미지를 추가해야 합니다.
// 예시를 위해 임시로 안드로이드 아이콘을 사용합니다. 실제 앱에서는 자신의 이미지를 사용하세요.
//import android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherMainScreen(
    navController: NavController,
    viewModel: TeacherMainViewModel = viewModel()
) {
    /* // TODO: 교사 메인 페이지
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "선생님 홈",
                emoji = "🌳"
            ) { 
                // 메인 화면이므로 뒤로가기 없음 //TODO: 로그아웃 추가
            }
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. 상단 유저 정보 영역
                NatureComponents.NatureCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    elevation = NatureElevation.extraLarge,
                    shape = NatureShapes.extraLarge
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 프로필 아바타
                        NatureComponents.ProfileAvatar(
                            emoji = "👩‍🏫",
                            backgroundColor = NatureColors.earthBrown,
                            size = 80.dp
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        // 사용자 정보
                        Column {
                            NatureComponents.StatusBadge(
                                text = "🌟 선생님",
                                backgroundColor = NatureColors.sunnyYellow.copy(alpha = 0.3f),
                                textColor = NatureColors.earthBrown
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = user?.nickname?.let { "$it 님" } ?: "로딩중...",
                                style = NatureTypography.titleLarge.copy(fontSize = 24.sp)
                            )
                        }
                    }
                }

                // 2. 메인 메뉴 버튼들
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 우리 반 관리 버튼
                    NatureComponents.NatureCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp),
                        shape = NatureShapes.large,
                        containerColor = NatureColors.leafGreen.copy(alpha = 0.2f)
                    ) {
                        Button(
                            onClick = {
                                navController.navigate("classManagement")
                            },
                            modifier = Modifier.fillMaxSize(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = NatureColors.earthBrown
                            ),
                            shape = NatureShapes.large
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "🏫",
                                    fontSize = 32.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "우리 반 관리",
                                    style = NatureTypography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // 프로그램 관리 버튼
                    NatureComponents.NatureCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp),
                        shape = NatureShapes.large,
                        containerColor = NatureColors.sunnyYellow.copy(alpha = 0.2f)
                    ) {
                        Button(
                            onClick = {
                                navController.navigate("programManagement")
                            },
                            modifier = Modifier.fillMaxSize(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = NatureColors.earthBrown
                            ),
                            shape = NatureShapes.large
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "🎒",
                                    fontSize = 32.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "프로그램 관리",
                                    style = NatureTypography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // 3. 사용자 상세 정보 영역
                NatureComponents.NatureCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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
                        
                        when (uiState) {
                            is TeacherMainViewModel.UiState.Loading -> {
                                NatureComponents.NatureLoadingIndicator(
                                    modifier = Modifier.height(100.dp)
                                )
                            }
                            is TeacherMainViewModel.UiState.Authenticated -> {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    InfoItem(
                                        emoji = "👤",
                                        label = "이름",
                                        value = user?.name ?: "정보 없음"
                                    )
                                    InfoItem(
                                        emoji = "🏷️",
                                        label = "역할",
                                        value = when(user?.role) {
                                            UserRole.TEACHER -> "선생님"
                                            UserRole.STUDENT -> "학생"
                                            else -> "정보 없음"
                                        }
                                    )
                                    InfoItem(
                                        emoji = "🆔",
                                        label = "사용자 ID",
                                        value = user?.id?.toString() ?: "정보 없음"
                                    )
                                }
                            }
                            is TeacherMainViewModel.UiState.Error -> {
                                NatureComponents.EmptyStateCard(
                                    emoji = "⚠️",
                                    title = "정보를 불러올 수 없어요",
                                    description = "잠시 후 다시 시도해주세요"
                                )
                            }
                            else -> {
                                NatureComponents.EmptyStateCard(
                                    emoji = "🔍",
                                    title = "정보를 찾을 수 없어요",
                                    description = "사용자 정보를 확인해주세요"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    */

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
    TeacherMainScreen(navController = navController)
}