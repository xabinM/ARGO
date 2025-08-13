package com.example.bogoargo.ui.screens.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
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


                    // 반 신청 기능 (항상 표시)
                    item {
                        var inviteCode by remember { mutableStateOf("") }
                        
                        NatureComponents.NatureCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "🎓 새 반 참여하기",
                                    style = NatureTypography.titleMedium,
                                    color = NatureColors.earthBrown
                                )
                                
                                OutlinedTextField(
                                    value = inviteCode,
                                    onValueChange = { inviteCode = it },
                                    label = { 
                                        Text(
                                            "초대 코드", 
                                            style = NatureTypography.bodyMedium
                                        ) 
                                    },
                                    placeholder = {
                                        Text(
                                            "선생님께서 주신 코드를 입력하세요",
                                            style = NatureTypography.bodySmall
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isLoading,
                                    shape = NatureShapes.medium,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NatureColors.forestGreen,
                                        focusedLabelColor = NatureColors.forestGreen,
                                        unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                                    )
                                )
                                
                                NatureComponents.NatureButton(
                                    onClick = {
                                        if (inviteCode.isNotBlank()) {
                                            viewModel.applyToClass(inviteCode.trim())
                                        }
                                    },
                                    text = "🌱 반 참여하기",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    enabled = inviteCode.isNotBlank() && !uiState.isLoading,
                                    backgroundColor = NatureColors.forestGreen
                                )
                            }
                        }
                    }

                    // 반 목록이 비어있을 때 안내 메시지
                    if (uiState.classes.isEmpty()) {
                        item {
                            NatureComponents.NatureCard(
                                containerColor = NatureColors.leafGreen.copy(alpha = 0.1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🌱",
                                        style = NatureTypography.titleLarge
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "아직 참여한 반이 없어요",
                                        style = NatureTypography.titleLarge,
                                        color = NatureColors.earthBrown,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "위의 반 참여하기로\n새로운 반에 참여해보세요!",
                                        style = NatureTypography.bodyMedium,
                                        color = NatureColors.earthBrown.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center
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