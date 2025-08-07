package com.example.bogoargo.ui.screens.classRoom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.ui.viewmodels.user.StudentClassDetailViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentClassDetailScreen(
    navController: NavController,
    classId: Long,
    viewModel: StudentClassDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(classId) {
        viewModel.loadClassDetail(classId)
    }

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "우리반",
                emoji = "🎒",
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when {
                    uiState.isLoading -> {
                        item {
                            NatureComponents.NatureCard {
                                NatureComponents.NatureLoadingIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                            }
                        }
                    }
                    uiState.errorMessage != null -> {
                        item {
                            NatureComponents.NatureCard(
                                containerColor = NatureColors.earthBrown.copy(alpha = 0.1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "⚠️",
                                        fontSize = 32.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = uiState.errorMessage!!,
                                        style = NatureTypography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        
                        // 디버그 버튼
                        item {
                            NatureComponents.NatureCard(
                                containerColor = NatureColors.sunnyYellow.copy(alpha = 0.15f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🔧 개발용 테스트",
                                        style = NatureTypography.titleMedium.copy(
                                            color = NatureColors.earthBrown
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "API 연결이 준비되지 않아 더미 데이터를 사용합니다",
                                        style = NatureTypography.bodySmall.copy(
                                            color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.loadDummyData(classId) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = NatureColors.sunnyYellow
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "더미 데이터 로드",
                                            color = NatureColors.earthBrown
                                        )
                                    }
                                }
                            }
                        }
                    }
                    uiState.classDetail != null -> {
                        val classDetail = uiState.classDetail!!
                        
                        // 반 기본 정보
                        item {
                            NatureComponents.NatureCard(
                                elevation = NatureElevation.large
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    // 반 이름
                                    Text(
                                        text = classDetail.className,
                                        style = NatureTypography.titleLarge.copy(fontSize = 22.sp),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    
                                    // 장소
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        Text(text = "📍", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = classDetail.location,
                                            style = NatureTypography.bodyMedium
                                        )
                                    }
                                    
                                    // 활동 날짜
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        Text(text = "📅", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = classDetail.activityDate.toString(),
                                            style = NatureTypography.bodyMedium
                                        )
                                    }
                                    
                                    // 참여자 수
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    ) {
                                        Text(text = "👥", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${classDetail.currentStudents}명의 친구들과 함께해요",
                                            style = NatureTypography.bodyMedium
                                        )
                                    }
                                    
                                    // 설명
                                    if (classDetail.description.isNotEmpty()) {
                                        NatureComponents.NatureCard(
                                            containerColor = NatureColors.sunnyYellow.copy(alpha = 0.1f),
                                            shape = NatureShapes.medium
                                        ) {
                                            Text(
                                                text = classDetail.description,
                                                style = NatureTypography.bodyMedium.copy(
                                                    lineHeight = 20.sp
                                                ),
                                                modifier = Modifier.padding(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // 내 팀 정보
                        if (uiState.myTeam != null) {
                            item {
                                NatureComponents.NatureCard(
                                    containerColor = NatureColors.leafGreen.copy(alpha = 0.1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp)
                                    ) {
                                        Text(
                                            text = "🏆 내 팀",
                                            style = NatureTypography.titleMedium.copy(
                                                color = NatureColors.forestGreen
                                            ),
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                        
                                        val myTeam = uiState.myTeam!!
                                        
                                        // 팀 이름
                                        Text(
                                            text = myTeam.teamName,
                                            style = NatureTypography.bodyLarge.copy(
                                                color = NatureColors.earthBrown
                                            ),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        
                                        // 팀원 수
                                        Text(
                                            text = "팀원 ${myTeam.memberCount}명",
                                            style = NatureTypography.bodyMedium.copy(
                                                color = NatureColors.earthBrown.copy(alpha = 0.8f)
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            item {
                                NatureComponents.NatureCard(
                                    containerColor = NatureColors.earthBrown.copy(alpha = 0.1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "👥", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "아직 팀이 배정되지 않았어요",
                                            style = NatureTypography.bodyMedium,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "선생님이 곧 팀을 만들어 주실 거예요!",
                                            style = NatureTypography.bodySmall.copy(
                                                color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                        
                        // 모든 팀 정보
                        if (uiState.allTeams.isNotEmpty()) {
                            item {
                                NatureComponents.NatureCard {
                                    Column(
                                        modifier = Modifier.padding(20.dp)
                                    ) {
                                        Text(
                                            text = "🌟 모든 팀",
                                            style = NatureTypography.titleMedium.copy(
                                                color = NatureColors.forestGreen
                                            ),
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                        
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            uiState.allTeams.forEach { team ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = team.teamName,
                                                        style = NatureTypography.bodyMedium
                                                    )
                                                    Text(
                                                        text = "${team.memberCount}명",
                                                        style = NatureTypography.bodySmall.copy(
                                                            color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // 게임 옵션들
                        if (uiState.myTeam != null) {
                            item {
                                NatureComponents.NatureCard(
                                    containerColor = NatureColors.sunnyYellow.copy(alpha = 0.1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp)
                                    ) {
                                        Text(
                                            text = "🎮 게임 선택",
                                            style = NatureTypography.titleMedium.copy(
                                                color = NatureColors.forestGreen
                                            ),
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                        
                                        // 메인 게임 버튼
                                        NatureComponents.NatureCard(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp),
                                            containerColor = NatureColors.leafGreen.copy(alpha = 0.1f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "🗺️ Argo Game",
                                                    style = NatureTypography.titleMedium
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "지도를 활용한 위치 기반 게임",
                                                    style = NatureTypography.bodySmall.copy(
                                                        color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                                    ),
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Button(
                                                    onClick = { navController.navigate("game") },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = NatureColors.leafGreen
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "게임 시작",
                                                        color = NatureColors.earthBrown
                                                    )
                                                }
                                            }
                                        }
                                        
                                        // 카드게임 버튼
                                        NatureComponents.NatureCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            containerColor = NatureColors.earthBrown.copy(alpha = 0.1f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "🃏 카드 배틀",
                                                    style = NatureTypography.titleMedium
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "팀 대 팀 전략 카드 게임 (개발용)",
                                                    style = NatureTypography.bodySmall.copy(
                                                        color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                                    ),
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Button(
                                                    onClick = { 
                                                        // 더미 데이터: 내 팀의 teamId와 leaderId 사용
                                                        val teamId = uiState.myTeam!!.teamId
                                                        navController.navigate("cardGame/$teamId/2") 
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = NatureColors.earthBrown.copy(alpha = 0.8f)
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "카드게임 시작",
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            item {
                                NatureComponents.NatureCard(
                                    containerColor = NatureColors.earthBrown.copy(alpha = 0.1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "⏳", fontSize = 28.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "팀이 배정되면 게임을 시작할 수 있어요!",
                                            style = NatureTypography.bodyMedium.copy(
                                                color = NatureColors.earthBrown
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "선생님이 곧 팀을 만들어 주실 거예요.",
                                            style = NatureTypography.bodySmall.copy(
                                                color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                            ),
                                            textAlign = TextAlign.Center
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
}

@Preview(showBackground = true)
@Composable
fun StudentClassDetailScreenPreview() {
    MaterialTheme {
        StudentClassDetailScreen(
            navController = rememberNavController(),
            classId = 1L
        )
    }
}