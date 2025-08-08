package com.example.bogoargo.ui.screens.classRoom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.UserRole
import com.example.bogoargo.ui.viewmodels.classRoom.ClassManagementViewModel
import com.example.bogoargo.navigation.Screen
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassManagementScreen(
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
                title = "우리반 관리",
                emoji = "🏠",
                onNavigationClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            // 선생님일 때만 반 추가 버튼 표시
            if (currentUser?.role == UserRole.ROLE_TEACHER) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate("classCreate")
                    },
                    containerColor = NatureColors.leafGreen,
                    shape = NatureShapes.large
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "반 추가",
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
            ) {
                
                // Statistics Card
                NatureComponents.StatsCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "반 현황",
                    emoji = "🌟"
                ) {
                    NatureComponents.StatItem(
                        label = "생성된 반",
                        value = uiState.teacherClasses.size.toString(),
                        emoji = "🏠",
                        color = NatureColors.forestGreen
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                NatureComponents.SectionHeader(
                    text = "반 목록",
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
                    NatureComponents.NatureLoadingIndicator()
                } else {
                    val isTeacher = currentUser?.role == UserRole.ROLE_TEACHER
                    val classes = if (isTeacher) uiState.teacherClasses else uiState.studentClasses
                    
                    if (classes.isEmpty()) {
                        NatureComponents.EmptyStateCard(
                            emoji = "🏠",
                            title = if (isTeacher) "만든 반이 없어요" else "참여 중인 반이 없어요",
                            description = if (isTeacher) "새로운 반을 만들어\n아이들과 함께 재미있는 학습을 시작해보세요!" else "초대 코드로 반에 참여해보세요!"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(classes) { classInfo ->
                                ClassInfoCard(
                                    classInfo = classInfo,
                                    isTeacher = isTeacher,
                                    onDeleteClick = if (isTeacher) { { viewModel.deleteClass(classInfo.classId) } } else null
                                ) {
                                    navController.navigate(Screen.ClassDetail.createRoute(classInfo.classId))
                                }
                            }
                            // 빈 공간 추가 (FAB와의 겹침 방지)
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassInfoCard(
    classInfo: Class, 
    isTeacher: Boolean,
    onDeleteClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    NatureComponents.NatureCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    backgroundColor = NatureColors.sunnyYellow,
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

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NatureComponents.StatusBadge(
                            text = "📅 ${classInfo.activityDate}",
                            backgroundColor = NatureColors.forestGreen.copy(alpha = 0.2f),
                            textColor = NatureColors.forestGreen
                        )

                        NatureComponents.StatusBadge(
                            text = "👥 ${classInfo.studentCount}/${classInfo.maxStudents}",
                            backgroundColor = NatureColors.leafGreen.copy(alpha = 0.2f),
                            textColor = NatureColors.leafGreen
                        )
                    }
                }
            }
            
            // Action buttons
            if (isTeacher && onDeleteClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NatureColors.softOrange
                        )
                    ) {
                        Text("반 삭제")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PreviewClassManagementScreen() {
    val navController = rememberNavController()
    ClassManagementScreen(navController = navController)
}