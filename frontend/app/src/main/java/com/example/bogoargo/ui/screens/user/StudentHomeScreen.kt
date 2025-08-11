package com.example.bogoargo.ui.screens.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.navigation.Screen
import com.example.bogoargo.ui.viewmodels.StudentHomeViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    navController: NavController,
    viewModel: StudentHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "내 반들",
                emoji = "🎒",
                onNavigationClick = null
            )
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    NatureComponents.NatureLoadingIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 환영 메시지
                    item {
                        Text(
                            text = uiState.welcomeMessage,
                            style = NatureTypography.titleLarge,
                            textAlign = TextAlign.Center,
                            color = NatureColors.earthBrown,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 개발용 더미 데이터 로드 버튼
                    item {
                        NatureComponents.NatureCard(
                            containerColor = NatureColors.sunnyYellow.copy(alpha = 0.2f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🔧 개발용 테스트",
                                    style = NatureTypography.titleMedium,
                                    color = NatureColors.earthBrown
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "API 준비 중이니 더미 데이터로 테스트해보세요",
                                    style = NatureTypography.bodySmall,
                                    color = NatureColors.earthBrown.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.loadDummyClasses() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NatureColors.sunnyYellow
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "더미 반 데이터 로드",
                                        color = NatureColors.earthBrown
                                    )
                                }
                            }
                        }
                    }

                    // 참여한 반 목록
                    if (uiState.classes.isNotEmpty()) {
                        item {
                            Text(
                                text = "🌟 내가 참여한 반",
                                style = NatureTypography.titleMedium,
                                color = NatureColors.forestGreen,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(uiState.classes) { classItem ->
                            NatureComponents.NatureCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate(Screen.StudentClassDetail.createRoute(classItem.classId)) },
                                containerColor = NatureColors.leafGreen.copy(alpha = 0.1f),
                                shape = NatureShapes.large
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = classItem.className,
                                            style = NatureTypography.titleMedium,
                                            color = NatureColors.earthBrown,
                                            modifier = Modifier.weight(1f)
                                        )
                                        NatureComponents.NatureCard(
                                            containerColor = NatureColors.forestGreen.copy(alpha = 0.1f),
                                            shape = NatureShapes.small
                                        ) {
                                            Text(
                                                text = "${classItem.currentStudents}/${classItem.maxStudents}명",
                                                style = NatureTypography.labelSmall,
                                                color = NatureColors.forestGreen,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    if (classItem.description.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = classItem.description,
                                            style = NatureTypography.bodyMedium,
                                            color = NatureColors.earthBrown.copy(alpha = 0.8f),
                                            maxLines = 2
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "📍 ${classItem.location}",
                                            style = NatureTypography.labelMedium,
                                            color = NatureColors.forestGreen
                                        )
                                        Text(
                                            text = "📅 ${classItem.activityDate}",
                                            style = NatureTypography.labelMedium,
                                            color = NatureColors.forestGreen
                                        )
                                        Text(
                                            text = "🏆 ${classItem.teamCount}팀",
                                            style = NatureTypography.labelMedium,
                                            color = NatureColors.forestGreen
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 네비게이션 메뉴
                    item {
                        NatureComponents.NatureCard(
                            containerColor = NatureColors.earthBrown.copy(alpha = 0.1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "⚙️ 메뉴",
                                    style = NatureTypography.titleMedium,
                                    color = NatureColors.earthBrown,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { navController.navigate(Screen.Profile.route) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = NatureColors.leafGreen.copy(alpha = 0.3f),
                                            contentColor = NatureColors.earthBrown
                                        )
                                    ) {
                                        Text("👤 프로필", style = NatureTypography.labelMedium)
                                    }

                                    Button(
                                        onClick = { navController.navigate(Screen.Settings.route) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = NatureColors.leafGreen.copy(alpha = 0.3f),
                                            contentColor = NatureColors.earthBrown
                                        )
                                    ) {
                                        Text("⚙️ 설정", style = NatureTypography.labelMedium)
                                    }

                                    Button(
                                        onClick = { viewModel.refreshData() },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = NatureColors.leafGreen.copy(alpha = 0.3f),
                                            contentColor = NatureColors.earthBrown
                                        )
                                    ) {
                                        Text("🔄 새로고침", style = NatureTypography.labelMedium)
                                    }
                                }
                            }
                        }
                    }

                    // 에러 메시지 표시
                    uiState.errorMessage?.let { error ->
                        item {
                            NatureComponents.NatureCard(
                                containerColor = NatureColors.earthBrown.copy(alpha = 0.2f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "⚠️",
                                        style = NatureTypography.titleLarge
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = error,
                                        color = NatureColors.earthBrown,
                                        style = NatureTypography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // 기타 테스트 아이템들 (개발용)
                    itemsIndexed(uiState.items) { index, item ->
                        NatureComponents.NatureCard(
                            modifier = Modifier.clickable { viewModel.selectItem(index) },
                            containerColor = if (uiState.selectedItemIndex == index)
                                NatureColors.sunnyYellow.copy(alpha = 0.3f)
                            else NatureColors.leafGreen.copy(alpha = 0.05f)
                        ) {
                            Text(
                                text = item,
                                modifier = Modifier.padding(16.dp),
                                style = NatureTypography.bodyMedium,
                                color = NatureColors.earthBrown
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
fun StudentHomeScreenPreview() {
    MaterialTheme {
        StudentHomeScreen(navController = rememberNavController())
    }
}