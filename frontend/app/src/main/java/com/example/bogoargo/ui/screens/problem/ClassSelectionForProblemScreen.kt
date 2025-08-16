package com.example.bogoargo.ui.screens.problem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.UserRole
import com.example.bogoargo.ui.viewmodels.classRoom.ClassManagementViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassSelectionForProblemScreen(
    navController: NavController,
    viewModel: ClassManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentUser by remember { mutableStateOf(null as com.example.bogoargo.domain.model.User?) }
    
    // 로그인한 사용자 정보 로드
    val loginViewModel = hiltViewModel<com.example.bogoargo.ui.viewmodels.user.LoginViewModel>()
    
    LaunchedEffect(Unit) {
        currentUser = loginViewModel.getLoggedInUser()
        val isTeacher = currentUser?.role == UserRole.ROLE_TEACHER
        
        if (isTeacher) {
            viewModel.loadTeacherClasses()
        }
    }

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "문제 생성",
                emoji = "📝",
                onNavigationClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
            ) {
                
                // 설명 카드
                NatureComponents.NatureCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    containerColor = NatureColors.sunnyYellow.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡",
                            style = NatureTypography.headlineMedium
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = "문제 생성하기",
                                style = NatureTypography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "반을 선택하면 해당 반의 학년에 맞는\n문제를 생성할 수 있어요",
                                style = NatureTypography.bodyMedium.copy(
                                    color = NatureColors.earthBrown.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }

                NatureComponents.SectionHeader(
                    text = "내 반 목록",
                    emoji = "🏠"
                )
                
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

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        NatureComponents.NatureLoadingIndicator()
                    }
                } else {
                    val isTeacher = currentUser?.role == UserRole.ROLE_TEACHER
                    val classes = if (isTeacher) uiState.teacherClasses else uiState.studentClasses
                    
                    if (classes.isEmpty()) {
                        NatureComponents.EmptyStateCard(
                            emoji = "🏠",
                            title = "만든 반이 없어요",
                            description = "문제를 생성하려면\n먼저 반을 만들어주세요!"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(classes) { classInfo ->
                                ClassSelectionCard(
                                    classInfo = classInfo,
                                    onClassSelected = { selectedClass ->
                                        // 선택된 반 정보를 가지고 문제 생성 페이지로 이동
                                        navController.navigate("problemGenerate/${selectedClass.classId}")
                                    }
                                )
                            }
                            // 빈 공간 추가
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassSelectionCard(
    classInfo: Class,
    onClassSelected: (Class) -> Unit
) {
    NatureComponents.NatureCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClassSelected(classInfo) },
        shape = NatureShapes.card
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Class Icon
                NatureComponents.ProfileAvatar(
                    emoji = "🏠",
                    backgroundColor = NatureColors.leafGreen,
                    size = 60.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Class Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = classInfo.className,
                        style = NatureTypography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "📍 ${classInfo.location}",
                        style = NatureTypography.bodyMedium.copy(
                            color = NatureColors.earthBrown.copy(alpha = 0.8f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            NatureComponents.InfoChip(
                                text = "${classInfo.grade}학년",
                                emoji = "📚",
                                backgroundColor = NatureColors.sunnyYellow.copy(alpha = 0.2f),
                                textColor = NatureColors.earthBrown
                            )
                            
                            NatureComponents.InfoChip(
                                text = "${classInfo.studentCount}명",
                                emoji = "👥",
                                backgroundColor = NatureColors.leafGreen.copy(alpha = 0.2f),
                                textColor = NatureColors.earthBrown
                            )
                        }
                        
                        NatureComponents.InfoChip(
                            text = classInfo.activityDate.toString(),
                            emoji = "📅",
                            backgroundColor = NatureColors.forestGreen.copy(alpha = 0.2f),
                            textColor = NatureColors.earthBrown
                        )
                    }
                }
                
                // 화살표 아이콘
                Text(
                    text = "▶",
                    style = NatureTypography.headlineSmall,
                    color = NatureColors.earthBrown.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PreviewClassSelectionForProblemScreen() {
    val navController = rememberNavController()
    ClassSelectionForProblemScreen(navController = navController)
}