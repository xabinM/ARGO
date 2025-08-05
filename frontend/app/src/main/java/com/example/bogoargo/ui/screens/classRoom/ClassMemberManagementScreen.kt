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
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation
import com.example.bogoargo.ui.viewmodels.classRoom.ClassMemberManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassMemberManagementScreen(
    navController: NavController,
    classId: String = "",
    viewModel: ClassMemberManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    LaunchedEffect(classId) {
        val classIdLong = classId.toLongOrNull() ?: 0L
        viewModel.loadClassMembers(classIdLong)
        viewModel.loadPendingApplications(classIdLong)
    }

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "강의실 관리",
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
                        onClick = { selectedTab = 0 },
                        text = "반 구성원",
                        modifier = Modifier.weight(1f),
                        backgroundColor = if (selectedTab == 0) NatureColors.forestGreen else NatureColors.earthBrown.copy(alpha = 0.3f)
                    )
                    NatureComponents.NatureButton(
                        onClick = { selectedTab = 1 },
                        text = "참여 신청",
                        modifier = Modifier.weight(1f),
                        backgroundColor = if (selectedTab == 1) NatureColors.forestGreen else NatureColors.earthBrown.copy(alpha = 0.3f)
                    )
                }
                
                // 통계 카드
                NatureComponents.StatsCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = if (selectedTab == 0) "구성원 현황" else "신청 현황",
                    emoji = "👥"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (selectedTab == 0) {
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
                    when (selectedTab) {
                        0 -> {
                            // 반 구성원 목록
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
                                    items(uiState.classMembers.size) { index ->
                                        // TODO: Replace with proper member data when available
                                        Text("Member ${index + 1}")
                                    }
                                }
                            }
                        }
                        1 -> {
                            // 참여 신청 목록
                            if (uiState.pendingApplications.isEmpty()) {
                                NatureComponents.EmptyStateCard(
                                    emoji = "📝",
                                    title = "신청이 없어요",
                                    description = "아직 참여 신청을 한 학생들이 없습니다."
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(uiState.pendingApplications.size) { index ->
                                        // TODO: Replace with proper application data when available
                                        Text("Application ${index + 1}")
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
                        "TEACHER" -> "👩‍🏫 선생님"
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
fun ApplicationCard(
    application: ApplicationDataDto,
    onApprove: () -> Unit
) {
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        text = application.user.name,
                        style = NatureTypography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "신청 일시: ${application.processedAt}",
                        style = NatureTypography.bodySmall.copy(
                            color = NatureColors.earthBrown.copy(alpha = 0.7f)
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
                //horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NatureComponents.NatureButton(
                    onClick = onApprove,
                    text = "승인",
                    backgroundColor = NatureColors.leafGreen
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
            classId = "class_1"
        )
    }
}