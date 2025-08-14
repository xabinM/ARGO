package com.example.bogoargo.ui.screens.team

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.ui.viewmodels.team.TeamManagementViewModel
import com.example.bogoargo.ui.viewmodels.classRoom.ClassDetailTeacherViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.domain.model.Team
import com.example.bogoargo.domain.model.TeamDetail
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(
    navController: NavController,
    classId: Long,
    teams: List<TeamDetail>? = null,
    viewModel: TeamManagementViewModel = hiltViewModel(),
    classDetailViewModel: ClassDetailTeacherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val classTeams by classDetailViewModel.teams.collectAsState()
    val isClassDetailLoading by classDetailViewModel.isLoading.collectAsState()
    var showCreateTeamDialog by remember { mutableStateOf(false) }
    var selectedTeamDetail by remember { mutableStateOf<TeamDetail?>(null) }
    
    LaunchedEffect(classId) {
        classDetailViewModel.getClassDetail(classId)
        viewModel.loadClassDetail(classId)
    }
    
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
    
    LaunchedEffect(uiState.createSuccess) {
        if (uiState.createSuccess) {
            showCreateTeamDialog = false
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
                    showCreateTeamDialog = true
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

                if (uiState.createdTeam != null) {
                    NatureComponents.NatureCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = NatureColors.leafGreen.copy(alpha = 0.2f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "🎉 팀 생성 완료",
                                style = NatureTypography.titleMedium.copy(
                                    color = NatureColors.forestGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "팀 이름: ${uiState.createdTeam!!.name}",
                                style = NatureTypography.bodyMedium.copy(
                                    color = NatureColors.earthBrown
                                )
                            )
                            Text(
                                text = "최대 인원: ${uiState.createdTeam!!.maxMembers}명",
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
                                    val classIdLong = classId
                                    viewModel.assignTeamRandom(classIdLong)
                                },
                                text = "🎲 팀 랜덤 배정",
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = NatureColors.sunnyYellow,
                                enabled = true
                            )
                        }
                    }
                }
                
                // 팀 리스트
                NatureComponents.NatureCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏆 팀 목록",
                                style = NatureTypography.titleMedium
                            )
                            if (uiState.className.isNotEmpty()) {
                                Text(
                                    text = uiState.className,
                                    style = NatureTypography.bodyMedium.copy(
                                        color = NatureColors.earthBrown.copy(alpha = 0.8f)
                                    )
                                )
                            }
                        }
                        
                        // ClassDetailTeacherViewModel에서 팀 데이터 사용
                        val displayTeams = classTeams
                        
                        when {
                            isClassDetailLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = NatureColors.forestGreen
                                    )
                                }
                            }
                            displayTeams.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "➕",
                                        fontSize = 48.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "아직 생성된 팀이 없습니다",
                                        style = NatureTypography.bodyMedium.copy(
                                            color = NatureColors.earthBrown
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "+ 버튼을 눌러 새로운 팀을 만들어보세요!",
                                        style = NatureTypography.bodySmall.copy(
                                            color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                        )
                                    )
                                }
                            }
                            else -> {
                                displayTeams.forEach { teamDetail ->
                                    TeamDetailListItem(
                                        teamDetail = teamDetail,
                                        onClick = { 
                                            selectedTeamDetail = teamDetail
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                
                // 추가 공간 (FAB와의 겹침 방지)
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    
    // 팀 생성 다이얼로그
    if (showCreateTeamDialog) {
        CreateTeamDialog(
            onDismiss = { showCreateTeamDialog = false },
            onCreateTeam = { teamName, maxMembers ->
                viewModel.createTeam(classId, teamName, maxMembers)
            },
            isLoading = uiState.isLoading
        )
    }
    
    // 팀 상세정보 다이얼로그
    selectedTeamDetail?.let { teamDetail ->
        TeamDetailDetailDialog(
            teamDetail = teamDetail,
            onDismiss = { selectedTeamDetail = null },
            onDeleteTeam = { teamId ->
                viewModel.deleteTeam(classId, teamId)
                selectedTeamDetail = null
            }
        )
    }
}

@Composable
fun CreateTeamDialog(
    onDismiss: () -> Unit,
    onCreateTeam: (String, Int) -> Unit,
    isLoading: Boolean
) {
    var teamName by remember { mutableStateOf("") }
    var maxMembers by remember { mutableStateOf("4") }
    var teamNameError by remember { mutableStateOf<String?>(null) }
    var maxMembersError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = NatureColors.whiteTransparent90,
        title = {
            Text(
                text = "🏆 새 팀 만들기",
                style = NatureTypography.titleMedium.copy(
                    color = NatureColors.forestGreen
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 팀 이름 입력
                Column {
                    Text(
                        text = "팀 이름",
                        style = NatureTypography.bodyMedium.copy(
                            color = NatureColors.earthBrown
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { 
                            teamName = it
                            teamNameError = null
                        },
                        placeholder = { 
                            Text(
                                "예: 자연탐험대",
                                style = NatureTypography.bodySmall.copy(
                                    color = NatureColors.earthBrown.copy(alpha = 0.6f)
                                )
                            ) 
                        },
                        isError = teamNameError != null,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NatureColors.forestGreen,
                            unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.3f),
                            errorBorderColor = NatureColors.softOrange
                        ),
                        shape = NatureShapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (teamNameError != null) {
                        Text(
                            text = teamNameError!!,
                            color = NatureColors.softOrange,
                            style = NatureTypography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }

                // 최대 인원 입력
                Column {
                    Text(
                        text = "최대 인원",
                        style = NatureTypography.bodyMedium.copy(
                            color = NatureColors.earthBrown
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = maxMembers,
                        onValueChange = { 
                            maxMembers = it.filter { char -> char.isDigit() }
                            maxMembersError = null
                        },
                        placeholder = { 
                            Text(
                                "4",
                                style = NatureTypography.bodySmall.copy(
                                    color = NatureColors.earthBrown.copy(alpha = 0.6f)
                                )
                            ) 
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = maxMembersError != null,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NatureColors.forestGreen,
                            unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.3f),
                            errorBorderColor = NatureColors.softOrange
                        ),
                        shape = NatureShapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (maxMembersError != null) {
                        Text(
                            text = maxMembersError!!,
                            color = NatureColors.softOrange,
                            style = NatureTypography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = NatureColors.forestGreen,
                    strokeWidth = 2.dp
                )
            } else {
                TextButton(
                    onClick = {
                        // 유효성 검사
                        teamNameError = when {
                            teamName.isBlank() -> "팀 이름을 입력해주세요"
                            teamName.length > 20 -> "팀 이름은 20자 이내로 입력해주세요"
                            else -> null
                        }
                        
                        val maxMembersInt = maxMembers.toIntOrNull()
                        maxMembersError = when {
                            maxMembers.isBlank() -> "최대 인원을 입력해주세요"
                            maxMembersInt == null -> "올바른 숫자를 입력해주세요"
                            maxMembersInt < 1 -> "최대 인원은 1명 이상이어야 합니다"
                            maxMembersInt > 10 -> "최대 인원은 10명 이하로 설정해주세요"
                            else -> null
                        }
                        
                        if (teamNameError == null && maxMembersError == null) {
                            onCreateTeam(teamName, maxMembersInt!!)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = NatureColors.forestGreen
                    )
                ) {
                    Text("생성")
                }
            }
        },
        dismissButton = {
            if (!isLoading) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = NatureColors.earthBrown
                    )
                ) {
                    Text("취소")
                }
            }
        }
    )
}

@Composable
fun TeamListItem(
    team: Team,
    onClick: () -> Unit
) {
    NatureComponents.NatureCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        containerColor = NatureColors.leafGreen.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "🏆 ${team.name}",
                    style = NatureTypography.titleMedium.copy(
                        color = NatureColors.forestGreen
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "멤버: ${team.currentMembers}/${team.maxMembers}명",
                    style = NatureTypography.bodyMedium.copy(
                        color = NatureColors.earthBrown
                    )
                )
                if ((team.totalPoints ?: 0) > 0) {
                    Text(
                        text = "점수: ${team.totalPoints}점",
                        style = NatureTypography.bodySmall.copy(
                            color = NatureColors.earthBrown.copy(alpha = 0.7f)
                        )
                    )
                }
            }
            
            Text(
                text = "👆",
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun TeamDetailDialog(
    team: Team,
    onDismiss: () -> Unit,
    onDeleteTeam: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NatureColors.whiteTransparent90,
        title = {
            Text(
                text = "🏆 ${team.name}",
                style = NatureTypography.titleMedium.copy(
                    color = NatureColors.forestGreen
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 팀 정보
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "팀 ID:",
                        style = NatureTypography.bodyMedium.copy(
                            color = NatureColors.earthBrown
                        )
                    )
                    Text(
                        text = "${team.id}",
                        style = NatureTypography.bodyMedium.copy(
                            color = NatureColors.earthBrown
                        )
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "최대 인원:",
                        style = NatureTypography.bodyMedium.copy(
                            color = NatureColors.earthBrown
                        )
                    )
                    Text(
                        text = "${team.maxMembers}명",
                        style = NatureTypography.bodyMedium.copy(
                            color = NatureColors.earthBrown
                        )
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "현재 인원:",
                        style = NatureTypography.bodyMedium.copy(
                            color = NatureColors.earthBrown
                        )
                    )
                    Text(
                        text = "${team.currentMembers}명",
                        style = NatureTypography.bodyMedium.copy(
                            color = NatureColors.earthBrown
                        )
                    )
                }
                
                if ((team.totalPoints ?: 0) > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "팀 점수:",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.earthBrown
                            )
                        )
                        Text(
                            text = "${team.totalPoints}점",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.forestGreen
                            )
                        )
                    }
                }
                
                // 팀원 목록
                if (team.currentMembers > 0) {
                    Divider(
                        color = NatureColors.earthBrown.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Text(
                        text = "👥 팀원 목록",
                        style = NatureTypography.bodyMedium.copy(
                            color = NatureColors.forestGreen
                        )
                    )
                    
                    Text(
                        text = "팀원 정보는 별도 조회가 필요합니다",
                        style = NatureTypography.bodySmall.copy(
                            color = NatureColors.earthBrown.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                } else {
                    Text(
                        text = "👥 아직 팀원이 없습니다",
                        style = NatureTypography.bodySmall.copy(
                            color = NatureColors.earthBrown.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = NatureColors.forestGreen
                )
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDeleteTeam(team.id) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = NatureColors.softOrange
                )
            ) {
                Text("팀 삭제")
            }
        }
    )
}

@Composable
fun TeamDetailListItem(
    teamDetail: TeamDetail,
    onClick: () -> Unit
) {
    NatureComponents.NatureCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        containerColor = NatureColors.leafGreen.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "🏆 ${teamDetail.teamName}",
                    style = NatureTypography.titleMedium.copy(
                        color = NatureColors.forestGreen
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "멤버: ${teamDetail.memberCount}명",
                    style = NatureTypography.bodyMedium.copy(
                        color = NatureColors.earthBrown
                    )
                )
                if (teamDetail.totalScore > 0) {
                    Text(
                        text = "점수: ${teamDetail.totalScore}점",
                        style = NatureTypography.bodySmall.copy(
                            color = NatureColors.earthBrown.copy(alpha = 0.7f)
                        )
                    )
                }
            }
            
            Text(
                text = "👆",
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun TeamDetailDetailDialog(
    teamDetail: TeamDetail,
    onDismiss: () -> Unit,
    onDeleteTeam: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NatureColors.whiteTransparent90,
        title = {
            Text(
                text = "🏆 ${teamDetail.teamName}",
                style = NatureTypography.titleMedium.copy(
                    color = NatureColors.forestGreen
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "팀 ID:",
                        style = NatureTypography.bodyMedium.copy(
                            color = NatureColors.earthBrown
                        )
                    )
                    Text(
                        text = "${teamDetail.teamId}",
                        style = NatureTypography.bodyMedium.copy(
                            color = NatureColors.earthBrown
                        )
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "멤버 수:",
                        style = NatureTypography.bodyMedium.copy(
                            color = NatureColors.earthBrown
                        )
                    )
                    Text(
                        text = "${teamDetail.memberCount}명",
                        style = NatureTypography.bodyMedium.copy(
                            color = NatureColors.earthBrown
                        )
                    )
                }
                
                if (teamDetail.totalScore > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "팀 점수:",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.earthBrown
                            )
                        )
                        Text(
                            text = "${teamDetail.totalScore}점",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.forestGreen
                            )
                        )
                    }
                }
                
                if (teamDetail.leaderId > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "팀장 ID:",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.earthBrown
                            )
                        )
                        Text(
                            text = "${teamDetail.leaderId}",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.earthBrown
                            )
                        )
                    }
                }
                
                if (teamDetail.members.isNotEmpty()) {
                    Divider(
                        color = NatureColors.earthBrown.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Text(
                        text = "👥 팀원 목록",
                        style = NatureTypography.bodyMedium.copy(
                            color = NatureColors.forestGreen
                        )
                    )
                    
                    teamDetail.members.forEach { member ->
                        Text(
                            text = "• ${member.studentName}",
                            style = NatureTypography.bodySmall.copy(
                                color = NatureColors.earthBrown.copy(alpha = 0.8f)
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                } else {
                    Text(
                        text = "👥 아직 팀원이 없습니다",
                        style = NatureTypography.bodySmall.copy(
                            color = NatureColors.earthBrown.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = NatureColors.forestGreen
                )
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDeleteTeam(teamDetail.teamId) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = NatureColors.softOrange
                )
            ) {
                Text("팀 삭제")
            }
        }
    )
}

//@Preview(showBackground = true)
//@Composable
//fun TeamManagementScreenPreview() {
//    MaterialTheme {
//        TeamManagementScreen(
//            navController = rememberNavController(),
//            classId = "1"
//        )
//    }
//}