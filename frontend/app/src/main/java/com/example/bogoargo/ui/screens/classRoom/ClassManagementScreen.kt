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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.data.model.Class
import com.example.bogoargo.ui.viewmodels.classRoom.ClassManagementViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassManagementScreen(
    navController: NavController,
    viewModel: ClassManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isTeacher by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        if (isTeacher) {
            viewModel.loadTeacherClasses()
        } else {
            viewModel.loadStudentClasses()
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
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
            ) {
                // 탭 선택
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NatureComponents.NatureButton(
                        onClick = { 
                            isTeacher = true
                            viewModel.loadTeacherClasses()
                        },
                        text = "선생님 반",
                        modifier = Modifier.weight(1f),
                        backgroundColor = if (isTeacher) NatureColors.forestGreen else NatureColors.earthBrown.copy(alpha = 0.3f)
                    )
                    NatureComponents.NatureButton(
                        onClick = { 
                            isTeacher = false
                            viewModel.loadStudentClasses()
                        },
                        text = "학생 반",
                        modifier = Modifier.weight(1f),
                        backgroundColor = if (!isTeacher) NatureColors.forestGreen else NatureColors.earthBrown.copy(alpha = 0.3f)
                    )
                }
                
                // Statistics Card
                NatureComponents.StatsCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "반 현황",
                    emoji = "🌟"
                ) {
                    NatureComponents.StatItem(
                        label = "참여 중인 반",
                        value = if (isTeacher) uiState.teacherClasses.size.toString() else uiState.studentClasses.size.toString(),
                        emoji = "🏠",
                        color = NatureColors.forestGreen
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                NatureComponents.SectionHeader(
                    text = if (isTeacher) "내가 만든 반" else "참여 중인 반",
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
                                    onDeleteClick = if (isTeacher) { { viewModel.deleteClass(classInfo.classId) } } else null,
                                    onLeaveClick = if (!isTeacher) { { viewModel.leaveClass(classInfo.classId) } } else null
                                ) {
                                    navController.navigate("classDetail/${classInfo.classId}")
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
    onLeaveClick: (() -> Unit)? = null,
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
            } else if (!isTeacher && onLeaveClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onLeaveClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NatureColors.softOrange
                        )
                    ) {
                        Text("반 탈퇴")
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