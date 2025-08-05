package com.example.bogoargo.ui.screens.cardgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.navigation.Screen
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureComponents
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bogoargo.ui.viewmodels.cardgame.CardGameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardGameScreen(
    navController: NavController,
    teamId: Long,
    leaderId: Long,
    viewModel: CardGameViewModel = hiltViewModel()
) {
    val currentUserId by viewModel.currentUserId.collectAsState()
    val isTeamLeader = currentUserId != null && currentUserId == leaderId
    
    // 디버깅용 로그
    LaunchedEffect(currentUserId, leaderId) {
        println("CardGame Debug - currentUserId: $currentUserId, leaderId: $leaderId, isTeamLeader: $isTeamLeader")
    }

    val teamStats = remember {
        TeamCardStats(
            teamId = teamId,
            teamName = "드래곤 슬레이어",
            wins = 12,
            losses = 3,
            totalScore = 2450,
            rank = 2
        )
    }

    val battleHistory = remember {
        listOf(
            BattleHistory(
                battleId = 1,
                opponentTeamName = "불사조 팀",
                isWin = true,
                scoreGained = 150,
                battleDate = "2024-08-04",
                requestDate = null,
                status = BattleStatus.COMPLETED,
                myCards = emptyList(),
                opponentCards = emptyList()
            ),
            BattleHistory(
                battleId = 2,
                opponentTeamName = "그리핀 팀",
                isWin = null,
                scoreGained = 0,
                battleDate = "",
                requestDate = "2024-08-05",
                status = BattleStatus.WAITING_OPPONENT,
                myCards = emptyList(),
                opponentCards = emptyList()
            ),
            BattleHistory(
                battleId = 3,
                opponentTeamName = "유니콘 팀",
                isWin = null,
                scoreGained = 0,
                battleDate = "",
                requestDate = "2024-08-04",
                status = BattleStatus.WAITING_MY_CARDS,
                myCards = emptyList(),
                opponentCards = emptyList()
            ),
            BattleHistory(
                battleId = 4,
                opponentTeamName = "이글 팀",
                isWin = false,
                scoreGained = -30,
                battleDate = "2024-08-03",
                requestDate = null,
                status = BattleStatus.RESULT_PENDING,
                myCards = emptyList(),
                opponentCards = emptyList()
            ),
            BattleHistory(
                battleId = 5,
                opponentTeamName = "라이온 팀",
                isWin = true,
                scoreGained = 200,
                battleDate = "2024-08-02",
                requestDate = null,
                status = BattleStatus.COMPLETED,
                myCards = emptyList(),
                opponentCards = emptyList()
            )
        )
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("카드 배틀 🃏") },
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    TeamStatsCard(teamStats = teamStats)
                }

                item {
                    ActionButtonsRow(
                        isTeamLeader = isTeamLeader,
                        onViewCards = {
                            navController.navigate(Screen.CardCollection.createRoute(teamId))
                        },
                        onRequestBattle = {
                            navController.navigate(Screen.BattleRequest.createRoute(teamId, leaderId))
                        }
                    )
                }

                item {
                    Text(
                        text = "🏆 대전 기록",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NatureColors.forestGreen
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(battleHistory) { battle ->
                    BattleHistoryItem(
                        battle = battle,
                        onCancelRequest = { battleId ->
                            // TODO: 대전 신청 취소 로직
                        },
                        onSelectCards = { battleId ->
                            navController.navigate(Screen.CardSelection.createRoute(teamId, battleId))
                        },
                        onViewResult = { battleId ->
                            // TODO: 결과 애니메이션 화면으로 네비게이션
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TeamStatsCard(teamStats: TeamCardStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NatureColors.whiteTransparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🛡️ ${teamStats.teamName}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NatureColors.forestGreen
                    )
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = NatureColors.leafGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "랭킹 ${teamStats.rank}위",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Divider(color = Color.Gray.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = "🏆",
                    label = "승리",
                    value = teamStats.wins.toString(),
                    color = Color(0xFF4CAF50)
                )
                StatItem(
                    icon = "💔",
                    label = "패배",
                    value = teamStats.losses.toString(),
                    color = Color(0xFFF44336)
                )
                StatItem(
                    icon = "⭐",
                    label = "총점",
                    value = teamStats.totalScore.toString(),
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.Gray.copy(alpha = 0.7f)
            )
        )
    }
}

@Composable
fun ActionButtonsRow(
    isTeamLeader: Boolean,
    onViewCards: () -> Unit,
    onRequestBattle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onViewCards,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = NatureColors.sunnyYellow
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.Group,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("우리 팀 카드 보기")
        }

        Button(
            onClick = onRequestBattle,
            enabled = isTeamLeader,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isTeamLeader) NatureColors.leafGreen else Color.Gray
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isTeamLeader) "대전 신청" else "팀장 전용")
        }
    }
}

@Composable
fun BattleHistoryItem(
    battle: BattleHistory,
    onCancelRequest: (Long) -> Unit,
    onSelectCards: (Long) -> Unit,
    onViewResult: (Long) -> Unit
) {
    val backgroundColor = when (battle.status) {
        BattleStatus.COMPLETED -> {
            if (battle.isWin == true) Color(0xFFE8F5E8) else Color(0xFFFFEBEE)
        }
        BattleStatus.WAITING_OPPONENT -> Color(0xFFFFF3E0)
        BattleStatus.WAITING_MY_CARDS -> Color(0xFFE3F2FD)
        BattleStatus.RESULT_PENDING -> Color(0xFFF3E5F5)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "vs ${battle.opponentTeamName}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NatureColors.earthBrown
                        )
                    )
                    
                    val dateText = when (battle.status) {
                        BattleStatus.COMPLETED -> battle.battleDate
                        BattleStatus.RESULT_PENDING -> battle.battleDate
                        else -> battle.requestDate ?: ""
                    }
                    
                    if (dateText.isNotEmpty()) {
                        Text(
                            text = if (battle.status == BattleStatus.COMPLETED || battle.status == BattleStatus.RESULT_PENDING) {
                                "대전일: $dateText"
                            } else {
                                "신청일: $dateText"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray.copy(alpha = 0.6f)
                            )
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (battle.status) {
                            BattleStatus.COMPLETED -> if (battle.isWin == true) Color(0xFF4CAF50) else Color(0xFFF44336)
                            BattleStatus.WAITING_OPPONENT -> Color(0xFFFF9800)
                            BattleStatus.WAITING_MY_CARDS -> Color(0xFF2196F3)
                            BattleStatus.RESULT_PENDING -> Color(0xFF9C27B0)
                        }
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = when (battle.status) {
                            BattleStatus.COMPLETED -> if (battle.isWin == true) "승리 🏆" else "패배 💔"
                            BattleStatus.WAITING_OPPONENT -> "대기중 ⏳"
                            BattleStatus.WAITING_MY_CARDS -> "선택대기 🃏"
                            BattleStatus.RESULT_PENDING -> "결과확인 🎯"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // 상태별 액션 버튼
            when (battle.status) {
                BattleStatus.WAITING_OPPONENT -> {
                    Button(
                        onClick = { onCancelRequest(battle.battleId) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("신청 취소")
                    }
                }
                
                BattleStatus.WAITING_MY_CARDS -> {
                    Button(
                        onClick = { onSelectCards(battle.battleId) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NatureColors.leafGreen
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Style,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("카드 선택하기")
                    }
                }
                
                BattleStatus.RESULT_PENDING -> {
                    Button(
                        onClick = { onViewResult(battle.battleId) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NatureColors.sunnyYellow
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("결과 보기")
                    }
                }
                
                BattleStatus.COMPLETED -> {
                    // 완료된 대전의 경우 점수 표시
                    if (battle.scoreGained != 0) {
                        Text(
                            text = "${if (battle.scoreGained > 0) "+" else ""}${battle.scoreGained}점",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (battle.scoreGained > 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}