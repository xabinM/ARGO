package com.example.bogoargo.ui.screens.classRoom

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
import com.example.bogoargo.data.dto.response.UserDataDto
import com.example.bogoargo.data.dto.response.ApplicationDataDto
import com.example.bogoargo.data.dto.response.StudentListResponseDto
import com.example.bogoargo.domain.model.Application
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation
import com.example.bogoargo.ui.viewmodels.classRoom.ClassMemberManagementViewModel
import com.example.bogoargo.ui.viewmodels.classRoom.ManagementTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassMemberManagementScreen(
    navController: NavController,
    classId: Long,
    viewModel: ClassMemberManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(classId) {
        // 진입 시 현재 탭의 데이터를 로드
        viewModel.loadData(classId)
    }

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "구성원 관리",
                emoji = "👥",
                onNavigationClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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

                // 탭 선택
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NatureComponents.NatureButton(
                        onClick = { viewModel.switchTab(ManagementTab.MEMBERS, classId) },
                        text = "반 구성원",
                        modifier = Modifier.weight(1f),
                        backgroundColor = if (uiState.currentTab == ManagementTab.MEMBERS)
                            NatureColors.forestGreen else NatureColors.earthBrown.copy(alpha = 0.3f)
                    )
                    NatureComponents.NatureButton(
                        onClick = { viewModel.switchTab(ManagementTab.APPLICATIONS, classId) },
                        text = "참여 신청",
                        modifier = Modifier.weight(1f),
                        backgroundColor = if (uiState.currentTab == ManagementTab.APPLICATIONS)
                            NatureColors.forestGreen else NatureColors.earthBrown.copy(alpha = 0.3f)
                    )
                }

                // 통계 카드
                NatureComponents.StatsCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = if (uiState.currentTab == ManagementTab.MEMBERS) "구성원 현황" else "신청 현황",
                    emoji = "👥"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (uiState.currentTab == ManagementTab.MEMBERS) {
                            NatureComponents.StatItem(
                                "전체",
                                uiState.classMembers.size.toString(),
                                "👥",
                                NatureColors.forestGreen
                            )
                        } else {
                            NatureComponents.StatItem(
                                "대기 중",
                                uiState.pendingApplications.size.toString(),
                                "⏳",
                                NatureColors.sunnyYellow
                            )
                        }
                    }
                }

                if (uiState.isLoading) {
                    NatureComponents.NatureLoadingIndicator()
                } else {
                    when (uiState.currentTab) {
                        ManagementTab.MEMBERS -> {
                            if (uiState.classMembers.isEmpty()) {
                                NatureComponents.EmptyStateCard(
                                    emoji = "👥",
                                    title = "구성원이 없어요",
                                    description = "아직 반에 참여한 학생들이 없습니다."
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(uiState.classMembers, key = { it.studentId }) { member ->
                                        StudentCard(student = member)
                                    }
                                }
                            }
                        }
                        ManagementTab.APPLICATIONS -> {
                            if (uiState.pendingApplications.isEmpty()) {
                                NatureComponents.EmptyStateCard(
                                    emoji = "📝",
                                    title = "신청이 없어요",
                                    description = "아직 참여 신청을 한 학생들이 없습니다."
                                )
                            } else {
                                Column {
                                    // 선택된 항목이 있을 때만 버튼 표시
                                    if (uiState.selectedApplicationIds.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            NatureComponents.NatureButton(
                                                onClick = { 
                                                    viewModel.rejectSelectedApplications(classId)
                                                },
                                                text = "거부 (${uiState.selectedApplicationIds.size})",
                                                backgroundColor = NatureColors.softOrange,
                                                modifier = Modifier.weight(1f)
                                            )
                                            NatureComponents.NatureButton(
                                                onClick = { 
                                                    viewModel.approveSelectedApplications(classId)
                                                },
                                                text = "승인 (${uiState.selectedApplicationIds.size})",
                                                backgroundColor = NatureColors.leafGreen,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                    
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(uiState.pendingApplications, key = { it.applicationId }) { application ->
                                            ApplicationCard(
                                                application = application,
                                                isSelected = uiState.selectedApplicationIds.contains(application.applicationId),
                                                onSelectionChanged = {
                                                    viewModel.selectApplication(application.applicationId)
                                                }
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
}


@Composable
fun MemberCard(member: UserDataDto) {
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NatureComponents.ProfileAvatar(
                emoji = "👶",
                backgroundColor = NatureColors.sunnyYellow,
                size = 50.dp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = NatureTypography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                NatureComponents.StatusBadge(
                    text = when (member.role.name) {
                        "ROLE_TEACHER" -> "👩‍🏫 선생님"
                        else -> "👶 학생"
                    },
                    backgroundColor = NatureColors.leafGreen.copy(alpha = 0.2f),
                    textColor = NatureColors.leafGreen
                )
            }
            
            if (member.team != null) {
                NatureComponents.StatusBadge(
                    text = member.team!!.teamName,
                    backgroundColor = NatureColors.forestGreen.copy(alpha = 0.2f),
                    textColor = NatureColors.forestGreen
                )
            }
        }
    }
}

@Composable
fun StudentCard(student: StudentListResponseDto) {
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NatureComponents.ProfileAvatar(
                emoji = "👶",
                backgroundColor = NatureColors.sunnyYellow,
                size = 50.dp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.studentName,
                    style = NatureTypography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "가입일: ${student.joinedAt}",
                    style = NatureTypography.bodySmall.copy(
                        color = NatureColors.earthBrown.copy(alpha = 0.7f)
                    )
                )
            }
            
            NatureComponents.StatusBadge(
                text = student.teamInfo.teamName,
                backgroundColor = NatureColors.forestGreen.copy(alpha = 0.2f),
                textColor = NatureColors.forestGreen
            )
        }
    }
}

@Composable
fun ApplicationCard(
    application: Application,
    isSelected: Boolean = false,
    onSelectionChanged: () -> Unit = {}
) {
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = if (isSelected) NatureColors.leafGreen.copy(alpha = 0.1f) else NatureColors.lightBeige
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectionChanged() },
                colors = CheckboxDefaults.colors(
                    checkedColor = NatureColors.forestGreen
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            NatureComponents.ProfileAvatar(
                emoji = "👶",
                backgroundColor = NatureColors.sunnyYellow,
                size = 50.dp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = application.studentName,
                    style = NatureTypography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "신청 일시: ${application.appliedAt}",
                    style = NatureTypography.bodySmall.copy(
                        color = NatureColors.earthBrown.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = "상태: ${application.status}",
                    style = NatureTypography.bodySmall.copy(
                        color = NatureColors.sunnyYellow
                    )
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewClassMemberManagementScreen() {
    MaterialTheme {
        ClassMemberManagementScreen(
            navController = rememberNavController(),
            classId = 0L
        )
    }
}