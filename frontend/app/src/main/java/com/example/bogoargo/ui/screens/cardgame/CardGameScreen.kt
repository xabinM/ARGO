package com.example.bogoargo.ui.screens.cardgame

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.navigation.Screen
import com.example.bogoargo.ui.theme.NatureColors
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bogoargo.ui.viewmodels.cardgame.CardGameViewModel
import com.example.bogoargo.ui.components.BattleDetailDialog

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
    
    // Dialog 상태 관리
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedBattle by remember { mutableStateOf<BattleHistory?>(null) }
    
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
            // 1. 내가 신청한 PENDING 대전 - 신청 취소 버튼 표시 (내 카드만 있음)
            BattleHistory(
                matchId = 1,
                challengerTeamId = teamId, // 내 팀
                challengedTeamId = 2L,
                challengerTeamName = "드래곤 팀", // 내 팀
                challengedTeamName = "불사조 팀",
                status = BattleStatus.PENDING,
                resultView = ResultView.BothNotSee,
                winnerTeamId = null,
                loserTeamId = null,
                myCard = BattleCard(
                    gameCard = GameCard.create(1L, CardTier.RARE, 101L), // teamCardId 포함
                    battleStance = BattleStance.ATTACK
                ), // 내가 선택한 카드와 스탠스
                opponentCard = null, // 상대는 아직 카드 선택 안함
                createdAt = "2024-08-06",
                endedAt = null,
                myTeamId = teamId
            ),
            
            // 2. 상대가 신청한 PENDING 대전 - 대전 거절 버튼 표시 (상대 카드만 있음)
            BattleHistory(
                matchId = 2,
                challengerTeamId = 3L,
                challengedTeamId = teamId, // 내 팀
                challengerTeamName = "그리핀 팀",
                challengedTeamName = "드래곤 팀", // 내 팀
                status = BattleStatus.PENDING,
                resultView = ResultView.BothNotSee,
                winnerTeamId = null,
                loserTeamId = null,
                myCard = null, // 내가 아직 카드 선택 안함
                opponentCard = BattleCard(
                    gameCard = GameCard.create(5L, CardTier.EPIC),
                    battleStance = BattleStance.DEFENSE
                ), // 상대가 선택한 카드와 스탠스
                createdAt = "2024-08-05",
                endedAt = null,
                myTeamId = teamId,
            ),
            
            // 3. 완료된 대전 - 공격 vs 방어 승리 (100점)
            BattleHistory(
                matchId = 3,
                challengerTeamId = teamId, // 내 팀
                challengedTeamId = 4L,
                challengerTeamName = "드래곤 팀", // 내 팀  
                challengedTeamName = "유니콘 팀",
                status = BattleStatus.COMPLETED,
                resultView = ResultView.BothNotSee, // 아직 확인하지 않음
                winnerTeamId = teamId, // 내 팀이 승리
                loserTeamId = 4L,
                myCard = BattleCard(
                    gameCard = GameCard.create(7L, CardTier.LEGENDARY),
                    battleStance = BattleStance.ATTACK
                ), // 공격으로 승리
                opponentCard = BattleCard(
                    gameCard = GameCard.create(3L, CardTier.COMMON),
                    battleStance = BattleStance.DEFENSE
                ), // 방어로 패배
                createdAt = "2024-08-04",
                endedAt = "2024-08-04",
                myTeamId = teamId,
            ),
            
            // 4. 방어 vs 방어 특수 케이스 (50점)
            BattleHistory(
                matchId = 4,
                challengerTeamId = 5L,
                challengedTeamId = teamId, // 내 팀
                challengerTeamName = "이글 팀",
                challengedTeamName = "드래곤 팀", // 내 팀
                status = BattleStatus.COMPLETED,
                resultView = ResultView.SeeChallenger, // challenger만 확인함
                winnerTeamId = null, // 방 vs 방은 무승부 처리
                loserTeamId = null,
                myCard = BattleCard(
                    gameCard = GameCard.create(2L, CardTier.RARE),
                    battleStance = BattleStance.DEFENSE
                ), // 방어 선택
                opponentCard = BattleCard(
                    gameCard = GameCard.create(8L, CardTier.EPIC),
                    battleStance = BattleStance.DEFENSE
                ), // 상대도 방어 선택
                createdAt = "2024-08-03",
                endedAt = "2024-08-03",
                myTeamId = teamId,
            ),
            
            // 5. 공격 vs 공격 무승부 (카드 제거 + 100점)
            BattleHistory(
                matchId = 5,
                challengerTeamId = teamId, // 내 팀
                challengedTeamId = 6L,
                challengerTeamName = "드래곤 팀", // 내 팀
                challengedTeamName = "라이온 팀",
                status = BattleStatus.COMPLETED,
                resultView = ResultView.SeeChallenger, // 내가 확인함
                winnerTeamId = null, // 무승부
                loserTeamId = null,
                myCard = BattleCard(
                    gameCard = GameCard.create(4L, CardTier.RARE),
                    battleStance = BattleStance.ATTACK
                ), // 공격으로 무승부
                opponentCard = BattleCard(
                    gameCard = GameCard.create(6L, CardTier.COMMON),
                    battleStance = BattleStance.ATTACK
                ), // 상대도 공격으로 무승부
                createdAt = "2024-08-02",
                endedAt = "2024-08-02",
                myTeamId = teamId,
            ),
            
            // 6. 취소된 대전
            BattleHistory(
                matchId = 6,
                challengerTeamId = teamId, // 내 팀
                challengedTeamId = 7L,
                challengerTeamName = "드래곤 팀", // 내 팀
                challengedTeamName = "피닉스 팀",
                status = BattleStatus.CANCELLED,
                resultView = ResultView.BothNotSee,
                winnerTeamId = null,
                loserTeamId = null,
                myCard = null,
                opponentCard = null,
                createdAt = "2024-08-01",
                endedAt = "2024-08-01",
                myTeamId = teamId,
            ),
            
            // 7. 공격으로 패배 (카드 제거, 0점)
            BattleHistory(
                matchId = 7,
                challengerTeamId = 8L,
                challengedTeamId = teamId, // 내 팀
                challengerTeamName = "타이거 팀",
                challengedTeamName = "드래곤 팀", // 내 팀
                status = BattleStatus.COMPLETED,
                resultView = ResultView.BothSee, // 둘 다 확인함
                winnerTeamId = 8L, // 상대팀이 승리
                loserTeamId = teamId, // 내 팀이 패배
                myCard = BattleCard(
                    gameCard = GameCard.create(3L, CardTier.COMMON),
                    battleStance = BattleStance.ATTACK
                ), // 공격으로 패배 (카드 제거)
                opponentCard = BattleCard(
                    gameCard = GameCard.create(7L, CardTier.LEGENDARY),
                    battleStance = BattleStance.DEFENSE
                ), // 방어로 승리
                createdAt = "2024-08-01",
                endedAt = "2024-08-01",
                myTeamId = teamId,
            ),
            
            // 8. 방어로 승리 (50점)
            BattleHistory(
                matchId = 8,
                challengerTeamId = teamId, // 내 팀
                challengedTeamId = 9L,
                challengerTeamName = "드래곤 팀", // 내 팀
                challengedTeamName = "울프 팀",
                status = BattleStatus.COMPLETED,
                resultView = ResultView.BothNotSee, // 아직 확인하지 않음
                winnerTeamId = teamId, // 내 팀이 승리
                loserTeamId = 9L,
                myCard = BattleCard(
                    gameCard = GameCard.create(5L, CardTier.EPIC),
                    battleStance = BattleStance.DEFENSE
                ), // 방어로 승리
                opponentCard = BattleCard(
                    gameCard = GameCard.create(2L, CardTier.RARE),
                    battleStance = BattleStance.ATTACK
                ), // 공격으로 패배
                createdAt = "2024-07-31",
                endedAt = "2024-07-31",
                myTeamId = teamId
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
                        onCancelRequest = { matchId ->
                            // TODO: 대전 신청 취소 로직
                        },
                        onRejectBattle = { matchId ->
                            // TODO: 대전 거절 로직  
                        },
                        onAcceptBattle = { matchId ->
                            navController.navigate(Screen.CardSelection.createRoute(teamId, matchId))
                        },
                        onViewResult = { matchId ->
                            // 선택된 배틀 찾기
                            val battle = battleHistory.find { it.matchId == matchId }
                            battle?.let {
                                // 승패 결정 (실제로는 서버에서 받아야 함)
                                val isWin = it.winnerTeamId == teamId
                                // myCard와 opponentCard가 둘 다 있어야 진행
                                if (it.myCard != null && it.opponentCard != null) {
                                    navController.navigate(
                                        Screen.BattleResult.createRoute(
                                            myCardId = it.myCard.gameCard.cardId,
                                            myCardRarity = it.myCard.gameCard.rarity.name,
                                            myCardStance = it.myCard.battleStance.name,
                                            opponentCardId = it.opponentCard.gameCard.cardId,
                                            opponentCardRarity = it.opponentCard.gameCard.rarity.name,
                                            opponentCardStance = it.opponentCard.battleStance.name,
                                            isWin = isWin,
                                            myTeamName = if (it.isMyChallenge) it.challengerTeamName else it.challengedTeamName,
                                            opponentTeamName = it.opponentTeamName
                                        )
                                    )
                                }
                            }
                        },
                        onViewDetail = { battle ->
                            selectedBattle = battle
                            showDetailDialog = true
                        }
                    )
                }
            }
        }
        
        // Dialog 표시
        if (showDetailDialog && selectedBattle != null) {
            BattleDetailDialog(
                battle = selectedBattle!!,
                onDismiss = {
                    showDetailDialog = false
                    selectedBattle = null
                }
            )
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
    onRejectBattle: (Long) -> Unit,
    onAcceptBattle: (Long) -> Unit,
    onViewResult: (Long) -> Unit,
    onViewDetail: (BattleHistory) -> Unit
) {
    val backgroundColor = when (battle.status) {
        BattleStatus.COMPLETED -> {
            if (battle.hasViewedResult) {
                // 이미 확인한 경우만 실제 결과에 따른 색상
                when (battle.isWin) {
                    true -> Color(0xFFE8F5E8)    // 승리 - 연한 초록
                    false -> Color(0xFFFFEBEE)   // 패배 - 연한 빨강
                    null -> Color(0xFFF5F5F5)    // 무승부 - 연한 회색
                }
            } else {
                Color(0xFFE3F2FD) // 완료됨 (결과 미확인) - 연한 파랑
            }
        }
        BattleStatus.PENDING -> Color(0xFFFFF3E0)    // 대기중 - 연한 주황
        BattleStatus.CANCELLED -> Color(0xFFEEEEEE)  // 취소됨 - 연한 회색
        BattleStatus.EXPIRED -> Color(0xFFFFEBEE)    // 만료됨 - 연한 빨강
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
                        BattleStatus.COMPLETED -> battle.endedAt ?: ""
                        BattleStatus.CANCELLED, BattleStatus.EXPIRED -> battle.endedAt ?: battle.createdAt
                        else -> battle.createdAt
                    }
                    
                    if (dateText.isNotEmpty()) {
                        Text(
                            text = when (battle.status) {
                                BattleStatus.COMPLETED -> "대전일: $dateText"
                                BattleStatus.CANCELLED -> "취소일: $dateText"
                                BattleStatus.EXPIRED -> "만료일: $dateText"
                                else -> "신청일: $dateText"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray.copy(alpha = 0.6f)
                            )
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(battle.displayColor)
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = battle.displayStatus,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // 상태별 액션 버튼 및 정보 표시
            when {
                // 1. PENDING 상태 - 내가 신청한 경우: 취소 버튼
                battle.canCancel -> {
                    Button(
                        onClick = { onCancelRequest(battle.matchId) },
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
                
                // 2. PENDING 상태 - 상대가 신청한 경우: 카드 선택 + 거절 버튼
                battle.canReject -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 왼쪽: 카드 선택 버튼 (긍정적 행동)
                        Button(
                            onClick = { onAcceptBattle(battle.matchId) },
                            modifier = Modifier.weight(1f),
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
                            Text("카드 선택")
                        }
                        
                        // 오른쪽: 대전 거절 버튼 (부정적 행동)
                        Button(
                            onClick = { onRejectBattle(battle.matchId) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF44336)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Block,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("대전 거절")
                        }
                    }
                }
                
                // 3. COMPLETED 상태 - 결과 보기 가능한 경우
                battle.canViewResult -> {
                    Button(
                        onClick = { onViewResult(battle.matchId) },
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
                
                // 4. COMPLETED 상태 - 결과 확인됨 (점수 표시 + 상세정보 버튼)
                battle.hasViewedResult -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (battle.scoreGained != 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "대전 결과 확인됨",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.Gray.copy(alpha = 0.6f)
                                    )
                                )
                                Text(
                                    text = "${if (battle.scoreGained > 0) "+" else ""}${battle.scoreGained}점",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (battle.scoreGained > 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        } else {
                            Text(
                                text = "대전 결과 확인됨",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.Gray.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // 상세정보 버튼
                        Button(
                            onClick = { onViewDetail(battle) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NatureColors.earthBrown.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("상세정보")
                        }
                    }
                }
                
                // 5. PENDING 상태 - 기타 정보 표시 (버튼이 없는 경우)
                battle.status == BattleStatus.PENDING -> {
                    Text(
                        text = if (battle.isMyChallenge) "상대방 응답 대기 중..." else "대전 신청을 받았습니다",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // 6. COMPLETED 상태 - 권한이 없어서 결과를 볼 수 없는 경우
                battle.status == BattleStatus.COMPLETED && !battle.canViewResult && !battle.hasViewedResult -> {
                    Text(
                        text = "대전이 완료되었습니다",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}