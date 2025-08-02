package com.example.bogoargo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.ui.viewmodels.ClassViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation

// 장소 데이터 클래스

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    navController: NavController,
    classId: String,
    viewModel: ClassViewModel = viewModel()
) {
    // ViewModel에서 반 상세 정보와 팀 진행도 데이터를 가져옴
    val classDetail by viewModel.classDetail.collectAsState()
    val teamProgressList by viewModel.teamProgressList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // 페이지 진입 시 데이터 로드
    LaunchedEffect(classId) {
        viewModel.loadClassDetail(classId)
        viewModel.loadTeamProgress(classId)
    }
    
    // 에러 처리
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            println("Error: $errorMessage")
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "우리반 정보",
                emoji = "🏫"
            ) { 
                navController.popBackStack() 
            }
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
                            if (classDetail != null) {
                                // 학교 + 반 제목
                                Text(
                                    text = "${classDetail!!.schoolName} ${classDetail!!.className}",
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
                                    Column {
                                        Text(
                                            text = classDetail!!.location.name,
                                            style = NatureTypography.bodyMedium
                                        )
                                        if (classDetail!!.location.address.isNotEmpty()) {
                                            Text(
                                                text = classDetail!!.location.address,
                                                style = NatureTypography.bodySmall.copy(
                                                    color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                                )
                                            )
                                        }
                                    }
                                }
                                
                                // 설명
                                Text(
                                    text = classDetail!!.description,
                                    style = NatureTypography.bodyMedium.copy(
                                        color = NatureColors.earthBrown.copy(alpha = 0.8f),
                                        lineHeight = 22.sp
                                    ),
                                    modifier = Modifier.padding(bottom = 20.dp)
                                )
                                
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
                                            text = classDetail!!.invitationCode,
                                            style = NatureTypography.titleMedium.copy(
                                                fontSize = 20.sp,
                                                letterSpacing = 2.sp,
                                                color = NatureColors.forestGreen
                                            )
                                        )
                                    }
                                }
                            } else if (isLoading) {
                                // 로딩 중
                                NatureComponents.NatureLoadingIndicator(
                                    modifier = Modifier.height(200.dp)
                                )
                            }
                        }
                    }
                }

                // 관리 버튼들
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
                                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
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
                                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
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

                // 팀별 진행도 섹션
                item {
                    NatureComponents.SectionHeader(
                        text = "팀별 진행도",
                        emoji = "📊"
                    )
                }
                
                // 팀별 진행도 데이터 표시
                if (isLoading) {
                    item {
                        NatureComponents.NatureLoadingIndicator(
                            modifier = Modifier.height(200.dp)
                        )
                    }
                } else if (teamProgressList.isEmpty()) {
                    item {
                        NatureComponents.EmptyStateCard(
                            emoji = "🌱",
                            title = "아직 팀이 없어요",
                            description = "팀을 만들고 활동을\n시작해보세요!"
                        )
                    }
                } else {
                    item {
                        TeamProgressSection(teamProgressList = teamProgressList)
                    }
                }
            }
        }
    }
}

@Composable
fun TeamProgressSection(teamProgressList: List<TeamProgress>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        teamProgressList.forEach { teamProgress ->
            TeamProgressCard(teamProgress = teamProgress)
        }
    }
}

@Composable
fun TeamProgressCard(teamProgress: TeamProgress) {
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = teamProgress.color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🏆 ${teamProgress.teamName}",
                        style = NatureTypography.titleMedium.copy(color = teamProgress.color)
                    )
                    Text(
                        text = teamProgress.stage,
                        style = NatureTypography.bodySmall.copy(
                            color = NatureColors.earthBrown.copy(alpha = 0.7f)
                        )
                    )
                }
                
                // 진행률 표시
                NatureComponents.StatusBadge(
                    text = "${teamProgress.progress}%",
                    backgroundColor = teamProgress.color.copy(alpha = 0.2f),
                    textColor = teamProgress.color
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 진행 바
            NatureComponents.NatureCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                containerColor = NatureColors.earthBrown.copy(alpha = 0.1f),
                shape = NatureShapes.small
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(teamProgress.progress / 100f)
                        .height(8.dp)
                        .padding(0.dp)
                ) {
                    NatureComponents.NatureCard(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = teamProgress.color,
                        shape = NatureShapes.small
                    ) {}
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
            classId = "preview_class"
        )
    }
}