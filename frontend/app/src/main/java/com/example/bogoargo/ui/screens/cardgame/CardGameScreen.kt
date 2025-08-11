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
import androidx.compose.ui.window.DialogProperties
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
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation
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
    val isTeamLeader = viewModel.isTeamLeader(leaderId)
    val uiState by viewModel.uiState.collectAsState()
    
    // Dialog 상태 관리
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedBattle by remember { mutableStateOf<BattleHistory?>(null) }
    
    // 신청 취소 및 대전 거절 Dialog 상태
    var showCancelDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedMatchId by remember { mutableStateOf<Long?>(null) }
    
    // 결과 Dialog 상태
    var showCancelResultDialog by remember { mutableStateOf(false) }
    var cancelResultMessage by remember { mutableStateOf("") }
    var isCancelSuccess by remember { mutableStateOf(false) }
    
    var showRejectResultDialog by remember { mutableStateOf(false) }
    var rejectResultMessage by remember { mutableStateOf("") }
    var isRejectSuccess by remember { mutableStateOf(false) }
    
    // 디버깅용 로그
    LaunchedEffect(leaderId) {
        println("CardGame Debug - leaderId: $leaderId, isTeamLeader: $isTeamLeader")
    }

    val teamStats = uiState.teamStats

    // 화면 진입 시 자동 API 호출
    LaunchedEffect(teamId) {
        viewModel.loadBattleHistory(teamId)
        viewModel.loadTeamStats(teamId)
    }
    
// 에러 메시지 처리
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            println("CardGame Error: $message")
            // TODO: 실제 앱에서는 Snackbar나 Toast로 에러 표시
        }
    }
    
    // 취소 결과 처리
    LaunchedEffect(uiState.cancelResult, uiState.cancelError) {
        uiState.cancelResult?.let { message ->
            cancelResultMessage = message
            isCancelSuccess = true
            showCancelResultDialog = true
            viewModel.clearCancelStatus()
        }
        uiState.cancelError?.let { error ->
            cancelResultMessage = error
            isCancelSuccess = false
            showCancelResultDialog = true
            viewModel.clearCancelStatus()
        }
    }
    
    // 결과 확인 처리
    LaunchedEffect(uiState.viewResultError) {
        uiState.viewResultError?.let { error ->
            println("View Result Error: $error")
            // TODO: 에러 메시지를 Snackbar나 Toast로 표시
            viewModel.clearViewResultStatus()
        }
    }
    
    // 거절 결과 처리
    LaunchedEffect(uiState.rejectResult, uiState.rejectError) {
        uiState.rejectResult?.let { message ->
            rejectResultMessage = message
            isRejectSuccess = true
            showRejectResultDialog = true
            viewModel.clearRejectStatus()
        }
        uiState.rejectError?.let { error ->
            rejectResultMessage = error
            isRejectSuccess = false
            showRejectResultDialog = true
            viewModel.clearRejectStatus()
        }
    }
    
    val battleHistory = uiState.battleHistories

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "카드 배틀",
                emoji = "🃏",
                onNavigationClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    if (teamStats != null) {
                        TeamStatsCard(teamStats = teamStats)
                    } else if (uiState.isStatsLoading) {
                        // 로딩 중 표시
                        TeamStatsLoadingCard()
                    } else if (uiState.statsError != null) {
                        // 에러 시 표시
                        TeamStatsErrorCard(
                            errorMessage = uiState.statsError ?: "알 수 없는 오류가 발생했습니다",
                            onRetry = { viewModel.loadTeamStats(teamId) },
                            onUseDummy = { viewModel.loadDummyTeamStats(teamId) }
                        )
                    }
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

                // API 실패 또는 빈 데이터일 때만 버튼 표시
                if (uiState.errorMessage != null || (!uiState.isLoading && battleHistory.isEmpty())) {
                    item {
                        TestDataButtonsRow(
                            teamId = teamId,
                            isLoading = uiState.isLoading,
                            hasError = uiState.errorMessage != null,
                            onLoadRealData = {
                                viewModel.clearErrorMessage()
                                viewModel.loadBattleHistory(teamId)
                            },
                            onLoadDummyData = {
                                viewModel.clearErrorMessage()
                                viewModel.loadDummyBattleHistory(teamId)
                            }
                        )
                    }
                }

                item {
                    Text(
                        text = "🏆 대전 기록",
                        style = NatureTypography.titleLarge,
                        color = NatureColors.forestGreen,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // 로딩 상태 표시
                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = NatureColors.leafGreen
                            )
                        }
                    }
                }


                items(battleHistory) { battle ->
                    BattleHistoryItem(
                        battle = battle,
                        isViewResultLoading = uiState.isViewResultLoading,
                        onCancelRequest = { matchId ->
                            selectedMatchId = matchId
                            showCancelDialog = true
                        },
                        onRejectBattle = { matchId ->
                            selectedMatchId = matchId
                            showRejectDialog = true
                        },
                        onAcceptBattle = { matchId ->
                            navController.navigate(Screen.CardSelection.createRoute(teamId, matchId))
                        },
                        onViewResult = { matchId ->
                            // viewBattleResult API를 호출하고 성공시에만 네비게이션
                            viewModel.viewBattleResult(matchId) { successMatchId ->
                                // 선택된 배틀 찾기
                                val battle = battleHistory.find { it.matchId == successMatchId }
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
        
        // 신청 취소 확인 Dialog
        if (showCancelDialog) {
            ConfirmCancelDialog(
                onConfirm = {
                    selectedMatchId?.let { matchId ->
                        viewModel.cancelBattle(matchId, teamId)
                    }
                    showCancelDialog = false
                    selectedMatchId = null
                },
                onDismiss = {
                    showCancelDialog = false
                    selectedMatchId = null
                }
            )
        }
        
        // 대전 거절 확인 Dialog
        if (showRejectDialog) {
            ConfirmRejectDialog(
                onConfirm = {
                    selectedMatchId?.let { matchId ->
                        viewModel.rejectBattle(matchId, teamId)
                    }
                    showRejectDialog = false
                    selectedMatchId = null
                },
                onDismiss = {
                    showRejectDialog = false
                    selectedMatchId = null
                }
            )
        }
        
        // 취소 결과 Dialog
        if (showCancelResultDialog) {
            ResultDialog(
                isSuccess = isCancelSuccess,
                title = "취소",
                message = cancelResultMessage,
                onDismiss = {
                    showCancelResultDialog = false
                    cancelResultMessage = ""
                }
            )
        }
        
        // 거절 결과 Dialog
        if (showRejectResultDialog) {
            ResultDialog(
                isSuccess = isRejectSuccess,
                title = "거절",
                message = rejectResultMessage,
                onDismiss = {
                    showRejectResultDialog = false
                    rejectResultMessage = ""
                }
            )
        }
    }
}

@Composable
fun TeamStatsCard(teamStats: TeamCardStats) {
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth(),
        shape = NatureShapes.large,
        containerColor = NatureColors.whiteTransparent,
        elevation = NatureElevation.medium
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
                    style = NatureTypography.titleMedium,
                    color = NatureColors.forestGreen
                )
                NatureComponents.NatureCard(
                    containerColor = NatureColors.leafGreen,
                    shape = NatureShapes.small
                ) {
                    Text(
                        text = "랭킹 ${teamStats.rank}위",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = NatureTypography.labelMedium,
                        color = Color.White
                    )
                }
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

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
            style = NatureTypography.titleMedium,
            color = color
        )
        Text(
            text = label,
            style = NatureTypography.labelSmall,
            color = Color.Gray.copy(alpha = 0.7f)
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
        NatureComponents.NatureButton(
            onClick = onViewCards,
            modifier = Modifier.weight(1f),
            backgroundColor = NatureColors.sunnyYellow,
            contentColor = NatureColors.earthBrown
        ) {
            Icon(
                Icons.Default.Group,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("우리 팀 카드 보기")
        }

        NatureComponents.NatureButton(
            onClick = onRequestBattle,
            enabled = isTeamLeader,
            modifier = Modifier.weight(1f),
            backgroundColor = if (isTeamLeader) NatureColors.leafGreen else Color.Gray,
            contentColor = Color.White
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
fun ConfirmCancelDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = "⚠️",
                fontSize = 32.sp
            )
        },
        title = {
            Text(
                text = "대전 신청 취소",
                style = NatureTypography.titleMedium,
                color = NatureColors.earthBrown
            )
        },
        text = {
            Text(
                text = "정말 대전 신청을 취소하시겠습니까?\n취소하면 다시 신청해야 합니다.",
                style = NatureTypography.bodyMedium,
                color = Color.Gray
            )
        },
        confirmButton = {
            NatureComponents.NatureButton(
                onClick = onConfirm,
                backgroundColor = Color(0xFFF44336),
                contentColor = Color.White
            ) {
                Text("신청 취소")
            }
        },
        dismissButton = {
            NatureComponents.NatureButton(
                onClick = onDismiss,
                backgroundColor = Color.Gray,
                contentColor = Color.White
            ) {
                Text("돌아가기")
            }
        },
        containerColor = NatureColors.whiteTransparent,
        shape = NatureShapes.medium
    )
}

@Composable
fun ConfirmRejectDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = "🚫",
                fontSize = 32.sp
            )
        },
        title = {
            Text(
                text = "대전 거절",
                style = NatureTypography.titleMedium,
                color = NatureColors.earthBrown
            )
        },
        text = {
            Text(
                text = "정말 대전을 거절하시겠습니까?\n거절하면 상대방에게 알림이 갑니다.",
                style = NatureTypography.bodyMedium,
                color = Color.Gray
            )
        },
        confirmButton = {
            NatureComponents.NatureButton(
                onClick = onConfirm,
                backgroundColor = Color(0xFFF44336),
                contentColor = Color.White
            ) {
                Text("대전 거절")
            }
        },
        dismissButton = {
            NatureComponents.NatureButton(
                onClick = onDismiss,
                backgroundColor = Color.Gray,
                contentColor = Color.White
            ) {
                Text("돌아가기")
            }
        },
        containerColor = NatureColors.whiteTransparent,
        shape = NatureShapes.medium
    )
}

@Composable
fun BattleHistoryItem(
    battle: BattleHistory,
    isViewResultLoading: Boolean,
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

    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth(),
        shape = NatureShapes.medium,
        containerColor = backgroundColor,
        elevation = NatureElevation.small
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
                        style = NatureTypography.titleMedium,
                        color = NatureColors.earthBrown
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
                            style = NatureTypography.labelSmall,
                            color = Color.Gray.copy(alpha = 0.6f)
                        )
                    }
                }

                NatureComponents.NatureCard(
                    containerColor = Color(battle.displayColor),
                    shape = NatureShapes.extraSmall
                ) {
                    Text(
                        text = battle.displayStatus,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = NatureTypography.labelSmall,
                        color = Color.White
                    )
                }
            }

            // 상태별 액션 버튼 및 정보 표시
            when {
                // 1. PENDING 상태 - 내가 신청한 경우: 취소 버튼
                battle.canCancel -> {
                    NatureComponents.NatureButton(
                        onClick = { onCancelRequest(battle.matchId) },
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFFF44336),
                        contentColor = Color.White
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
                        NatureComponents.NatureButton(
                            onClick = { onAcceptBattle(battle.matchId) },
                            modifier = Modifier.weight(1f),
                            backgroundColor = NatureColors.leafGreen,
                            contentColor = Color.White
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
                        NatureComponents.NatureButton(
                            onClick = { onRejectBattle(battle.matchId) },
                            modifier = Modifier.weight(1f),
                            backgroundColor = Color(0xFFF44336),
                            contentColor = Color.White
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
                    NatureComponents.NatureButton(
                        onClick = { 
                            if (!isViewResultLoading) {
                                onViewResult(battle.matchId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = if (isViewResultLoading) Color.Gray else NatureColors.sunnyYellow,
                        contentColor = if (isViewResultLoading) Color.White else NatureColors.earthBrown,
                        enabled = !isViewResultLoading
                    ) {
                        if (isViewResultLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("확인 중...")
                        } else {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("결과 보기")
                        }
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
                                    style = NatureTypography.labelSmall,
                                    color = Color.Gray.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "${if (battle.scoreGained > 0) "+" else ""}${battle.scoreGained}점",
                                    style = NatureTypography.bodyMedium,
                                    color = if (battle.scoreGained > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                            }
                        } else {
                            Text(
                                text = "대전 결과 확인됨",
                                style = NatureTypography.labelSmall,
                                color = Color.Gray.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // 상세정보 버튼
                        NatureComponents.NatureButton(
                            onClick = { onViewDetail(battle) },
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = NatureColors.earthBrown.copy(alpha = 0.8f),
                            contentColor = Color.White
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
                        style = NatureTypography.labelSmall,
                        color = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // 6. COMPLETED 상태 - 권한이 없어서 결과를 볼 수 없는 경우
                battle.status == BattleStatus.COMPLETED && !battle.canViewResult && !battle.hasViewedResult -> {
                    Text(
                        text = "대전이 완료되었습니다",
                        style = NatureTypography.labelSmall,
                        color = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun TestDataButtonsRow(
    teamId: Long,
    isLoading: Boolean,
    hasError: Boolean,
    onLoadRealData: () -> Unit,
    onLoadDummyData: () -> Unit
) {
    val cardColor = if (hasError) {
        NatureColors.sunnyYellow.copy(alpha = 0.2f) // 에러 시 더 강조된 색상
    } else {
        NatureColors.sunnyYellow.copy(alpha = 0.1f)
    }
    
    val titleText = if (hasError) {
        "⚠️ API 오류 발생"
    } else {
        "📭 데이터 없음"
    }
    
    val descriptionText = if (hasError) {
        "API 요청에 실패했습니다. 재시도하거나 테스트용 더미 데이터를 사용해보세요."
    } else {
        "대전 기록이 없습니다. 실제 데이터를 다시 불러오거나 테스트용 더미 데이터를 사용해보세요."
    }
    
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth(),
        shape = NatureShapes.medium,
        containerColor = cardColor,
        elevation = NatureElevation.small
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = titleText,
                style = NatureTypography.titleMedium,
                color = NatureColors.earthBrown
            )
            
            Text(
                text = descriptionText,
                style = NatureTypography.bodyMedium,
                color = Color.Gray.copy(alpha = 0.8f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 재요청 버튼
                NatureComponents.NatureButton(
                    onClick = onLoadRealData,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    backgroundColor = NatureColors.leafGreen,
                    contentColor = Color.White
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isLoading) "로딩중..." else if (hasError) "재요청" else "다시 불러오기",
                        style = NatureTypography.labelLarge
                    )
                }
                
                // 테스트용 더미 데이터 버튼
                NatureComponents.NatureButton(
                    onClick = onLoadDummyData,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    backgroundColor = NatureColors.sunnyYellow,
                    contentColor = NatureColors.earthBrown
                ) {
                    Text(
                        text = "테스트용 더미 데이터",
                        style = NatureTypography.labelLarge
                    )
                }
            }
            
            Text(
                text = "팀 ID: $teamId",
                style = NatureTypography.labelSmall,
                color = Color.Gray.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun ResultDialog(
    isSuccess: Boolean,
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isSuccess) "✅" else "❌",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = if (isSuccess) "$title 완료" else "$title 실패",
                    style = NatureTypography.titleLarge,
                    color = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        },
        text = {
            Text(
                text = message,
                style = NatureTypography.bodyLarge,
                color = NatureColors.earthBrown
            )
        },
        confirmButton = {
            NatureComponents.NatureButton(
                onClick = onDismiss,
                backgroundColor = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336),
                contentColor = Color.White
            ) {
                Text("확인")
            }
        },
        containerColor = NatureColors.whiteTransparent,
        shape = NatureShapes.medium
    )
}

@Composable
fun TeamStatsLoadingCard() {
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth(),
        shape = NatureShapes.large,
        containerColor = NatureColors.whiteTransparent,
        elevation = NatureElevation.medium
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = NatureColors.forestGreen)
        }
    }
}

@Composable
fun TeamStatsErrorCard(
    errorMessage: String,
    onRetry: () -> Unit,
    onUseDummy: () -> Unit
) {
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth(),
        shape = NatureShapes.large,
        containerColor = NatureColors.whiteTransparent,
        elevation = NatureElevation.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "팀 통계를 불러올 수 없습니다",
                style = NatureTypography.titleMedium,
                color = Color(0xFFF44336)
            )
            
            Text(
                text = errorMessage,
                style = NatureTypography.bodySmall,
                color = Color.Gray
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NatureComponents.NatureOutlinedButton(
                    onClick = onRetry,
                    text = "재시도"
                )
                NatureComponents.NatureButton(
                    onClick = onUseDummy,
                    text = "테스트 데이터",
                    backgroundColor = NatureColors.leafGreen
                )
            }
        }
    }
}

