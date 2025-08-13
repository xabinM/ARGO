package com.example.bogoargo.ui.screens.classRoom

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.bogoargo.navigation.Screen
import com.example.bogoargo.ui.viewmodels.classRoom.ClassDetailViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    navController: NavController,
    classId: Long,
    viewModel: ClassDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(classId) {
        viewModel.loadClassDetail(classId)
    }

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "우리반 정보",
                emoji = "🏫",
                onNavigationClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 헤더 카드 (학교 + 반 정보)
                item {
                    NatureComponents.NatureCard(
                        elevation = NatureElevation.extraLarge,
                        shape = NatureShapes.extraLarge
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            when {
                                uiState.isLoading -> {
                                    NatureComponents.NatureLoadingIndicator(
                                        modifier = Modifier.height(200.dp)
                                    )
                                }
                                uiState.errorMessage != null -> {
                                    Text(
                                        text = "⚠️ ${uiState.errorMessage}",
                                        style = NatureTypography.bodyMedium.copy(
                                            color = NatureColors.earthBrown
                                        ),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                                uiState.classDetail != null -> {
                                    val classDetail = uiState.classDetail!!
                                    
                                    // 반 제목
                                    Text(
                                        text = classDetail.className,
                                        style = NatureTypography.titleLarge.copy(fontSize = 24.sp),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    
                                    // 장소 정보
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    ) {
                                        Text(
                                            text = "📍",
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = classDetail.location,
                                            style = NatureTypography.bodyMedium
                                        )
                                    }
                                    
                                    // 활동 날짜
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    ) {
                                        Text(
                                            text = "📅",
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = classDetail.createdAt.toString(),
                                            style = NatureTypography.bodyMedium
                                        )
                                    }
                                    
                                    // 인원 정보
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    ) {
                                        Text(
                                            text = "👥",
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${classDetail.studentCount}/${classDetail.maxStudents}명",
                                            style = NatureTypography.bodyMedium
                                        )
                                    }
                                    
                                    // 설명
                                    if (classDetail.description.isNotEmpty()) {
                                        Text(
                                            text = classDetail.description,
                                            style = NatureTypography.bodyMedium.copy(
                                                color = NatureColors.earthBrown.copy(alpha = 0.8f),
                                                lineHeight = 22.sp
                                            ),
                                            modifier = Modifier.padding(bottom = 20.dp)
                                        )
                                    }
                                    
                                    // 초대 코드
                                    NatureComponents.NatureCard(
                                        shape = NatureShapes.medium,
                                        containerColor = NatureColors.forestGreen.copy(alpha = 0.1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "🎫 초대 코드",
                                                style = NatureTypography.labelMedium.copy(
                                                    color = NatureColors.forestGreen
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = classDetail.inviteCode,
                                                style = NatureTypography.titleMedium.copy(
                                                    fontSize = 20.sp,
                                                    letterSpacing = 2.sp,
                                                    color = NatureColors.forestGreen
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 관리 버튼들 - 첫 번째 줄
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 구성원 관리 버튼
                        NatureComponents.NatureCard(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp),
                            shape = NatureShapes.large,
                            containerColor = NatureColors.sunnyYellow.copy(alpha = 0.2f)
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate("classMemberManagement/$classId")
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
                                        text = "👥",
                                        fontSize = 28.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "구성원 관리",
                                        style = NatureTypography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        
                        // 팀 관리 버튼
                        NatureComponents.NatureCard(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp),
                            shape = NatureShapes.large,
                            containerColor = NatureColors.leafGreen.copy(alpha = 0.2f)
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate("teamManagement/$classId")
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
                                        text = "🏆",
                                        fontSize = 28.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "팀 관리",
                                        style = NatureTypography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // 학생 위치 보기 버튼
                item {
                    NatureComponents.NatureCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = NatureShapes.large,
                        containerColor = NatureColors.forestGreen.copy(alpha = 0.2f)
                    ) {
                        Button(
                            onClick = {
                                navController.navigate("studentLocation/$classId")
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
                                    text = "📍",
                                    fontSize = 28.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "학생 위치 보기",
                                    style = NatureTypography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // 팀 정보 섹션
                item {
                    NatureComponents.NatureCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = NatureColors.leafGreen.copy(alpha = 0.1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🏆 팀 정보",
                                style = NatureTypography.titleMedium.copy(
                                    color = NatureColors.forestGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (uiState.classDetail != null) {
                                Text(
                                    text = "총 ${uiState.classDetail!!.teamCount}개의 팀이 있습니다",
                                    style = NatureTypography.bodyMedium.copy(
                                        color = NatureColors.earthBrown
                                    )
                                )
                            } else {
                                Text(
                                    text = "팀 정보를 불러오는 중...",
                                    style = NatureTypography.bodyMedium.copy(
                                        color = NatureColors.earthBrown.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ClassDetailScreenPreview() {
    MaterialTheme {
        ClassDetailScreen(
            navController = rememberNavController(),
            classId = 0L
        )
    }
}