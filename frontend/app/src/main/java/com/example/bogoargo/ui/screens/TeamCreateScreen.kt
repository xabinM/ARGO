package com.example.bogoargo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.ui.viewmodels.TeamViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography

// 팀 데이터 클래스
data class Team(
    val id: String = "",
    val name: String,
    val description: String = "",
    val maxMembers: Int,
    val members: MutableList<Student> = mutableListOf(),
    val color: Color
)

// 학생 데이터 클래스
data class Student(
    val id: String,
    val name: String,
    val studentId: String,
    val classId: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCreateScreen(
    navController: NavController,
    classId: String = "",
    viewModel: TeamViewModel = viewModel()
) {
    // 팀 생성 관련 상태
    var teams by remember { mutableStateOf(listOf<Team>()) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    // 샘플 학생 데이터 (실제로는 ViewModel에서 가져와야 함)
    val students = remember {
        listOf(
            Student("1", "김철수", "2024001", classId),
            Student("2", "이영희", "2024002", classId),
            Student("3", "박민준", "2024003", classId),
            Student("4", "최서연", "2024004", classId),
            Student("5", "정하늘", "2024005", classId),
            Student("6", "강다은", "2024006", classId),
            Student("7", "윤지호", "2024007", classId),
            Student("8", "신예린", "2024008", classId)
        )
    }
    
    // 팀 색상 리스트
    val teamColors = listOf(
        NatureColors.forestGreen,
        NatureColors.sunnyYellow,
        NatureColors.leafGreen,
        NatureColors.softOrange,
        NatureColors.earthBrown
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is TeamViewModel.UiState.Success -> {
                navController.popBackStack()
            }
            is TeamViewModel.UiState.Loading -> {
                isLoading = true
            }
            else -> {
                isLoading = false
            }
        }
    }

    // 팀 추가 함수
    fun addTeam() {
        if (teams.size < 5) {
            val newTeam = Team(
                id = "team_${teams.size + 1}",
                name = "팀 ${teams.size + 1}",
                description = "",
                maxMembers = 4,
                color = teamColors[teams.size % teamColors.size]
            )
            teams = teams + newTeam
        }
    }

    // 팀 삭제 함수
    fun removeTeam(teamId: String) {
        teams = teams.filter { it.id != teamId }
    }

    // 배정되지 않은 학생들을 실시간으로 계산
    val assignedStudentIds = teams.flatMap { it.members }.map { it.id }.toSet()
    val availableStudents = students.filter { it.id !in assignedStudentIds }

    // 랜덤 배정 함수
    fun randomAssign() {
        if (teams.isEmpty()) return
        
        // 모든 팀에서 학생들을 제거
        teams = teams.map { it.copy(members = mutableListOf()) }
        
        // 학생들을 섞기
        val shuffledStudents = students.shuffled()
        var studentIndex = 0
        
        // 각 팀에 최대한 공평하게 배정
        while (studentIndex < shuffledStudents.size) {
            for (i in teams.indices) {
                if (studentIndex >= shuffledStudents.size) break
                if (teams[i].members.size < teams[i].maxMembers) {
                    teams[i].members.add(shuffledStudents[studentIndex])
                    studentIndex++
                }
            }
        }
    }

    // 학생을 팀에 추가하는 함수
    fun addStudentToTeam(student: Student, teamId: String) {
        teams = teams.map { team ->
            if (team.id == teamId && team.members.size < team.maxMembers) {
                team.copy(members = (team.members + student).toMutableList())
            } else {
                team
            }
        }
    }

    // 학생을 팀에서 제거하는 함수
    fun removeStudentFromTeam(student: Student, teamId: String) {
        teams = teams.map { team ->
            if (team.id == teamId) {
                team.copy(members = team.members.filter { it.id != student.id }.toMutableList())
            } else {
                team
            }
        }
    }

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "팀 구성하기",
                emoji = "🏆",
                onNavigationClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 상단 컨트롤 패널
                item {
                    NatureComponents.NatureCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            NatureComponents.SectionHeader(
                                text = "팀 관리",
                                emoji = "⚙️"
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                NatureComponents.NatureButton(
                                    onClick = { addTeam() },
                                    text = "➕ 팀 추가",
                                    modifier = Modifier.weight(1f),
                                    backgroundColor = NatureColors.leafGreen,
                                    enabled = teams.size < 5
                                )
                                
                                NatureComponents.NatureButton(
                                    onClick = { randomAssign() },
                                    text = "🎲 랜덤 배정",
                                    modifier = Modifier.weight(1f),
                                    backgroundColor = NatureColors.sunnyYellow,
                                    enabled = teams.isNotEmpty()
                                )
                            }
                            
                            if (errorMessage.isNotEmpty()) {
                                NatureComponents.NatureCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    containerColor = NatureColors.softOrange.copy(alpha = 0.3f)
                                ) {
                                    Text(
                                        text = "⚠️ $errorMessage",
                                        modifier = Modifier.padding(16.dp),
                                        style = NatureTypography.bodyMedium.copy(color = NatureColors.earthBrown)
                                    )
                                }
                            }
                        }
                    }
                }

                // 수동 배정을 위한 학생 목록
                if (availableStudents.isNotEmpty()) {
                    item {
                        NatureComponents.NatureCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                NatureComponents.SectionHeader(
                                    text = "배정 대기 학생 (${availableStudents.size}명)",
                                    emoji = "👥"
                                )
                                
                                Text(
                                    text = "🌿 학생을 클릭하여 원하는 팀에 배정하세요",
                                    style = NatureTypography.bodySmall.copy(
                                        color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                    )
                                )
                                
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(availableStudents) { student ->
                                        StudentCard(
                                            student = student,
                                            teams = teams,
                                            onAssignToTeam = { teamId ->
                                                addStudentToTeam(student, teamId)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 팀 목록
                items(teams) { team ->
                    TeamCard(
                        team = team,
                        students = students,
                        onRemoveTeam = { removeTeam(team.id) },
                        onUpdateTeam = { updatedTeam ->
                            teams = teams.map { if (it.id == team.id) updatedTeam else it }
                        },
                        onRemoveStudentFromTeam = { student ->
                            removeStudentFromTeam(student, team.id)
                        }
                    )
                }

                // 최종 생성 버튼
                if (teams.isNotEmpty()) {
                    item {
                        NatureComponents.NatureButton(
                            onClick = {
                                // 실제 팀 생성 로직
                                teams.forEach { team ->
                                    viewModel.createTeam(
                                        classId = classId,
                                        name = team.name,
                                        description = team.description,
                                        maxMembers = team.maxMembers,
                                        color = "#66BB6A"
                                    )
                                }
                            },
                            text = if (isLoading) "🌱 생성 중..." else "🏆 모든 팀 생성하기",
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = NatureColors.forestGreen,
                            enabled = !isLoading && teams.any { it.members.isNotEmpty() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCard(
    student: Student,
    teams: List<Team>,
    onAssignToTeam: (String) -> Unit
) {
    var showTeamSelection by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .width(80.dp)
            .clickable { showTeamSelection = true },
        colors = CardDefaults.cardColors(
            containerColor = NatureColors.whiteTransparent
        ),
        shape = NatureShapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "👦",
                style = NatureTypography.bodyMedium
            )
            Text(
                text = student.name,
                style = NatureTypography.bodySmall.copy(color = NatureColors.earthBrown)
            )
            Text(
                text = student.studentId,
                style = NatureTypography.labelSmall.copy(
                    color = NatureColors.earthBrown.copy(alpha = 0.7f)
                )
            )
        }
    }
    
    // 팀 선택 다이얼로그
    if (showTeamSelection) {
        AlertDialog(
            onDismissRequest = { showTeamSelection = false },
            title = {
                Text(
                    text = "${student.name} 학생을 배정할 팀을 선택하세요",
                    style = NatureTypography.titleMedium
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(teams.filter { it.members.size < it.maxMembers }) { team ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAssignToTeam(team.id)
                                    showTeamSelection = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = team.color.copy(alpha = 0.2f)
                            ),
                            shape = NatureShapes.small
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🏆",
                                    style = NatureTypography.bodyMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${team.name} (${team.members.size}/${team.maxMembers})",
                                    style = NatureTypography.bodyMedium.copy(color = team.color),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                NatureComponents.NatureOutlinedButton(
                    onClick = { showTeamSelection = false },
                    text = "취소"
                )
            }
        )
    }
}

@Composable
fun TeamCard(
    team: Team,
    students: List<Student>,
    onRemoveTeam: () -> Unit,
    onUpdateTeam: (Team) -> Unit,
    onRemoveStudentFromTeam: (Student) -> Unit
) {
    var teamName by remember(team.id) { mutableStateOf(team.name) }
    var maxMembers by remember(team.id) { mutableStateOf(team.maxMembers.toString()) }
    
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = team.color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 팀 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏆 $teamName",
                    style = NatureTypography.titleMedium.copy(color = team.color),
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = onRemoveTeam,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "팀 삭제",
                        tint = NatureColors.softOrange
                    )
                }
            }
            
            // 팀 설정
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { 
                        teamName = it
                        onUpdateTeam(team.copy(name = it))
                    },
                    label = { Text("팀 이름", style = NatureTypography.bodySmall) },
                    modifier = Modifier.weight(2f),
                    singleLine = true,
                    shape = NatureShapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = team.color,
                        focusedLabelColor = team.color,
                        unfocusedBorderColor = team.color.copy(alpha = 0.5f)
                    )
                )
                
                OutlinedTextField(
                    value = maxMembers,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                            maxMembers = newValue
                            newValue.toIntOrNull()?.let { max ->
                                if (max > 0) {
                                    onUpdateTeam(team.copy(maxMembers = max))
                                }
                            }
                        }
                    },
                    label = { Text("최대인원", style = NatureTypography.bodySmall) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = NatureShapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = team.color,
                        focusedLabelColor = team.color,
                        unfocusedBorderColor = team.color.copy(alpha = 0.5f)
                    )
                )
            }
            
            // 현재 멤버 표시
            Text(
                text = "👥 현재 멤버 (${team.members.size}/${team.maxMembers})",
                style = NatureTypography.bodyMedium.copy(color = team.color)
            )
            
            if (team.members.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = NatureColors.whiteTransparent.copy(alpha = 0.5f)
                    ),
                    shape = NatureShapes.small
                ) {
                    Text(
                        text = "🌿 아직 배정된 학생이 없습니다",
                        modifier = Modifier.padding(16.dp),
                        style = NatureTypography.bodySmall.copy(
                            color = NatureColors.earthBrown.copy(alpha = 0.7f)
                        )
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(team.members) { student ->
                        Card(
                            modifier = Modifier.clickable {
                                onRemoveStudentFromTeam(student)
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = team.color.copy(alpha = 0.2f)
                            ),
                            shape = NatureShapes.small,
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "👦",
                                    style = NatureTypography.bodyMedium
                                )
                                Text(
                                    text = student.name,
                                    style = NatureTypography.bodySmall.copy(color = team.color)
                                )
                                Text(
                                    text = student.studentId,
                                    style = NatureTypography.labelSmall.copy(
                                        color = team.color.copy(alpha = 0.7f)
                                    )
                                )
                                Text(
                                    text = "클릭하여 제거",
                                    style = NatureTypography.labelSmall.copy(
                                        color = team.color.copy(alpha = 0.5f)
                                    )
                                )
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
fun TeamCreateScreenPreview() {
    MaterialTheme {
        TeamCreateScreen(navController = rememberNavController())
    }
}