package com.example.bogoargo.ui.screens.team

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.ui.viewmodels.team.TeamManagementViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(
    navController: NavController,
    classId: String = "",
    viewModel: TeamManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.assignSuccess) {
        if (uiState.assignSuccess) {
            viewModel.clearSuccessFlags()
        }
    }
    
    LaunchedEffect(uiState.randomAssignSuccess) {
        if (uiState.randomAssignSuccess) {
            viewModel.clearSuccessFlags()
        }
    }
    
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            viewModel.clearSuccessFlags()
        }
    }

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "팀 관리",
                emoji = "🏆",
                onNavigationClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("teamCreate/$classId")
                },
                containerColor = NatureColors.leafGreen,
                shape = NatureShapes.large
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = "팀 추가", 
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (uiState.errorMessage != null) {
                    NatureComponents.NatureCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = NatureColors.softOrange.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "⚠️ ${uiState.errorMessage}",
                            modifier = Modifier.padding(16.dp),
                            style = NatureTypography.bodyMedium.copy(color = NatureColors.earthBrown)
                        )
                    }
                }

                if (uiState.assignResponse != null) {
                    NatureComponents.NatureCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = NatureColors.leafGreen.copy(alpha = 0.2f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "✅ 팀 배정 완료",
                                style = NatureTypography.titleMedium.copy(
                                    color = NatureColors.forestGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                if (uiState.deletedStudents != null) {
                    NatureComponents.NatureCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = NatureColors.sunnyYellow.copy(alpha = 0.2f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "🗑️ 팀 삭제 완료",
                                style = NatureTypography.titleMedium.copy(
                                    color = NatureColors.earthBrown
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "팀에서 제거된 학생: ${uiState.deletedStudents!!.size}명",
                                style = NatureTypography.bodyMedium.copy(
                                    color = NatureColors.earthBrown
                                )
                            )
                        }
                    }
                }

                // 팀 관리 액션들
                NatureComponents.NatureCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "🏆 팀 관리 도구",
                            style = NatureTypography.titleMedium
                        )
                        
                        Text(
                            text = "반 학생들의 팀을 관리해보세요.",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.earthBrown.copy(alpha = 0.8f)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 랜덤 배정 버튼
                        if (uiState.isLoading) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = NatureColors.forestGreen
                                )
                            }
                        } else {
                            NatureComponents.NatureButton(
                                onClick = { 
                                    val classIdLong = classId.toLongOrNull() ?: 0L
                                    viewModel.assignTeamRandom(classIdLong)
                                },
                                text = "🎲 팀 랜덤 배정",
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = NatureColors.sunnyYellow,
                                enabled = classId.isNotEmpty()
                            )
                        }
                    }
                }
                
                // 팀 생성 안내
                NatureComponents.NatureCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = NatureColors.forestGreen.copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "➕ 새로운 팀 만들기",
                            style = NatureTypography.titleMedium.copy(
                                color = NatureColors.forestGreen
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "오른쪽 하단의 + 버튼을 눌러\n새로운 팀을 만들어보세요!",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.earthBrown
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                
                // 추가 공간 (FAB와의 겹침 방지)
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeamManagementScreenPreview() {
    MaterialTheme {
        TeamManagementScreen(
            navController = rememberNavController(),
            classId = "1"
        )
    }
}