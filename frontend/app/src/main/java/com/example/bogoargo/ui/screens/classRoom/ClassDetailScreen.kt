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
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation
import com.example.bogoargo.ui.viewmodels.classRoom.ClassDetailTeacherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    navController: NavController,
    classId: Long,
    viewModel: ClassDetailTeacherViewModel = hiltViewModel()
) {
    val classInfo by viewModel.classInfo.collectAsState()
    val students by viewModel.students.collectAsState()
    val teams by viewModel.teams.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    LaunchedEffect(classId) {
        viewModel.getClassDetail(classId)
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
                                isLoading -> {
                                    NatureComponents.NatureLoadingIndicator(
                                        modifier = Modifier.height(200.dp)
                                    )
                                }
                                errorMessage != null -> {
                                    Text(
                                        text = "⚠️ $errorMessage",
                                        style = NatureTypography.bodyMedium.copy(
                                            color = NatureColors.earthBrown
                                        ),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                                classInfo != null -> {
                                    val classDetail = classInfo!!
                                    
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
                                            text = "${students.size}/${classDetail.maxStudents}명",
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

                // 팀 리스트 섹션
                if (teams.isNotEmpty()) {
                    item {
                        NatureComponents.NatureCard {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🏆 팀 목록",
                                        style = NatureTypography.titleMedium.copy(
                                            color = NatureColors.forestGreen
                                        )
                                    )
                                    NatureComponents.InfoChip(
                                        text = "${teams.size}개 팀",
                                        emoji = "👥",
                                        backgroundColor = NatureColors.sunnyYellow.copy(alpha = 0.2f),
                                        textColor = NatureColors.earthBrown
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    teams.forEach { team ->
                                        NatureComponents.NatureCard(
                                            containerColor = NatureColors.earthBrown.copy(alpha = 0.05f),
                                            shape = NatureShapes.medium
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = team.teamName,
                                                        style = NatureTypography.bodyLarge.copy(
                                                            color = NatureColors.earthBrown
                                                        )
                                                    )
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        NatureComponents.InfoChip(
                                                            text = "${team.memberCount}명",
                                                            emoji = "👥",
                                                            backgroundColor = NatureColors.leafGreen.copy(alpha = 0.2f),
                                                            textColor = NatureColors.earthBrown
                                                        )
                                                        NatureComponents.InfoChip(
                                                            text = "${team.totalScore}점",
                                                            emoji = "🏆",
                                                            backgroundColor = NatureColors.sunnyYellow.copy(alpha = 0.2f),
                                                            textColor = NatureColors.earthBrown
                                                        )
                                                    }
                                                }
                                                if (team.members.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = team.members.joinToString(", ") { it.studentName },
                                                        style = NatureTypography.bodySmall.copy(
                                                            color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
//                                Button(
//                                    onClick = {
//                                        navController.navigate(Screen.TeamManagement.createRoute(classId))
//                                    },
//                                    colors = ButtonDefaults.buttonColors(
//                                        containerColor = NatureColors.forestGreen
//                                    ),
//                                    shape = NatureShapes.button,
//                                    modifier = Modifier.fillMaxWidth()
//                                ) {
//                                    Text(
//                                        text = "🏆 팀 관리하기",
//                                        style = NatureTypography.labelLarge.copy(color = Color.White)
//                                    )
//                                }
                            }
                        }
                    }
                } else if (statistics != null && statistics!!.totalTeams == 0) {
                    item {
                        NatureComponents.NatureCard(
                            containerColor = NatureColors.earthBrown.copy(alpha = 0.1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "👥",
                                    fontSize = 32.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "아직 팀이 없습니다",
                                    style = NatureTypography.titleMedium.copy(
                                        color = NatureColors.earthBrown
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "팀 관리에서 새로운 팀을 만들어보세요!",
                                    style = NatureTypography.bodyMedium.copy(
                                        color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = {
                                        navController.navigate(Screen.TeamManagement.createRoute(classId))
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NatureColors.leafGreen
                                    ),
                                    shape = NatureShapes.button
                                ) {
                                    Text(
                                        text = "🏆 팀 관리하기",
                                        style = NatureTypography.labelLarge.copy(color = Color.White)
                                    )
                                }
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