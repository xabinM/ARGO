package com.example.bogoargo.ui.screens.cardgame

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.navigation.Screen
import com.example.bogoargo.ui.theme.NatureColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleRequestScreen(
    navController: NavController,
    teamId: Long,
    leaderId: Long
) {
    val availableTeams = remember {
        listOf(
            BattleTeam(
                teamId = 2,
                teamName = "불사조 팀 🔥",
                memberCount = 4,
                averageScore = 2100,
                isAvailable = true
            ),
            BattleTeam(
                teamId = 3,
                teamName = "그리핀 팀 🦅",
                memberCount = 3,
                averageScore = 1850,
                isAvailable = true
            ),
            BattleTeam(
                teamId = 4,
                teamName = "유니콘 팀 🦄",
                memberCount = 5,
                averageScore = 2300,
                isAvailable = false
            ),
            BattleTeam(
                teamId = 5,
                teamName = "드래곤 팀 🐉",
                memberCount = 4,
                averageScore = 2450,
                isAvailable = true
            )
        )
    }

    var showBattleRequestModal by remember { mutableStateOf(false) }
    var selectedTeam by remember { mutableStateOf<BattleTeam?>(null) }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("대전 상대 선택") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NatureColors.forestGreen,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(NatureColors.lightBeige)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NatureColors.whiteTransparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⚔️ 대전 상대를 선택하세요",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NatureColors.forestGreen
                                )
                            )
                            Text(
                                text = "현재 온라인인 팀들을 대상으로 대전을 신청할 수 있습니다.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.Gray.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                }

                items(availableTeams) { team ->
                    BattleTeamItem(
                        team = team,
                        onClick = {
                            if (team.isAvailable) {
                                selectedTeam = team
                                showBattleRequestModal = true
                            }
                        }
                    )
                }
            }
        }

        if (showBattleRequestModal && selectedTeam != null) {
            BattleRequestModal(
                targetTeam = selectedTeam!!,
                onDismiss = {
                    showBattleRequestModal = false
                    selectedTeam = null
                },
                onConfirm = { targetTeam ->
                    showBattleRequestModal = false
                    selectedTeam = null
                    navController.navigate(
                        Screen.CardSelection.createRoute(teamId, targetTeam.teamId)
                    )
                }
            )
        }
    }
}

@Composable
fun BattleTeamItem(
    team: BattleTeam,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = team.isAvailable) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (team.isAvailable) 
                NatureColors.whiteTransparent
            else 
                NatureColors.whiteTransparent.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (team.isAvailable) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = team.teamName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (team.isAvailable) 
                                NatureColors.earthBrown
                            else 
                                NatureColors.earthBrown.copy(alpha = 0.5f)
                        )
                    )
                    
                    if (!team.isAvailable) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "대전 중",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            tint = NatureColors.forestGreen.copy(
                                alpha = if (team.isAvailable) 1f else 0.5f
                            ),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${team.memberCount}명",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray.copy(
                                    alpha = if (team.isAvailable) 0.7f else 0.4f
                                )
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFF9800).copy(
                                alpha = if (team.isAvailable) 1f else 0.5f
                            ),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${team.averageScore}점",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray.copy(
                                    alpha = if (team.isAvailable) 0.7f else 0.4f
                                )
                            )
                        )
                    }
                }
            }

            if (team.isAvailable) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NatureColors.leafGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "도전하기 ⚔️",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun BattleRequestModal(
    targetTeam: BattleTeam,
    onDismiss: () -> Unit,
    onConfirm: (BattleTeam) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NatureColors.whiteTransparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚔️",
                    style = MaterialTheme.typography.displayMedium
                )
                
                Text(
                    text = "대전 신청",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NatureColors.forestGreen
                    )
                )

                Text(
                    text = "${targetTeam.teamName}에게 대전을 신청하시겠습니까?",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = NatureColors.forestGreen.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "상대팀 정보",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NatureColors.forestGreen
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "팀원 수",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${targetTeam.memberCount}명",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "평균 점수",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${targetTeam.averageScore}점",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("취소")
                    }
                    
                    Button(
                        onClick = { onConfirm(targetTeam) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NatureColors.leafGreen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("신청하기")
                    }
                }
            }
        }
    }
}