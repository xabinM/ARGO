package com.example.bogoargo.ui.screens.cardgame

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation
import com.example.bogoargo.ui.components.GameCardComponent
import com.example.bogoargo.ui.components.StatChip
import com.example.bogoargo.ui.components.CardDetailDialog
import com.example.bogoargo.ui.components.CardGridComponent
import com.example.bogoargo.ui.components.CardDisplayMode
import com.example.bogoargo.ui.components.CardFiltersSection
import com.example.bogoargo.ui.components.BattleResultDialog
import com.example.bogoargo.ui.viewmodels.cardgame.CardSelectionViewModel

data class CardSelectionParams(
    val teamId: Long,
    val targetTeamId: Long,
    val matchId: Long? = null, // 대전 응답 시에만 필요
    val isResponse: Boolean = false // true = 응답, false = 신규 신청
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CardSelectionScreen(
    navController: NavController,
    params: CardSelectionParams,
    viewModel: CardSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 화면 진입 시 팀 카드 컬렉션 로드
    LaunchedEffect(params.teamId) {
        viewModel.loadTeamCardCollection(params.teamId)
    }
    
    val availableCards = uiState.teamCardCollection?.cards ?: emptyList()

    var selectedCard by remember { mutableStateOf<Long?>(null) }
    var selectedStance by remember { mutableStateOf<BattleStance?>(null) }
    var showCardDetail by remember { mutableStateOf(false) }
    var selectedCardForDetail by remember { mutableStateOf<GameCard?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    val rarityFilter = remember { mutableStateOf<CardTier?>(null) }
    
    // 대전 결과 처리
    LaunchedEffect(uiState.battleResult, uiState.battleErrorMessage) {
        if (uiState.battleResult != null || uiState.battleErrorMessage != null) {
            showResultDialog = true
        }
    }

    val targetTeamName = remember {
        when (params.targetTeamId) {
            2L -> "불사조 팀 🔥"
            3L -> "그리핀 팀 🦅"
            4L -> "유니콘 팀 🦄"
            5L -> "드래곤 팀 🐉"
            else -> "상대팀"
        }
    }

    // 필터링된 카드 리스트 (레어도 필터 + 활성 카드만)
    val filteredCards = availableCards.filter { card ->
        val rarityMatch = rarityFilter.value?.let { it == card.rarity } ?: true
        rarityMatch // 모든 카드 표시 (활성/비활성 구분은 UI에서)
    }
    
    // 선택 가능한 카드만 필터링 (활성 카드만)
    val selectableCards = filteredCards.filter { it.isActive }

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "${if (params.isResponse) "대전 수락" else "대전 신청"} ${if (selectedCard != null) "(1/1)" else "(0/1)"}",
                emoji = if (params.isResponse) "🤝" else "⚔️",
                onNavigationClick = { navController.popBackStack() }
            )
        },
            bottomBar = {
                NatureComponents.NatureCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = NatureShapes.large,
                    containerColor = NatureColors.whiteTransparent,
                    elevation = NatureElevation.large
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "vs $targetTeamName",
                            style = NatureTypography.titleMedium,
                            color = NatureColors.forestGreen,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // 스탠스 선택 UI (카드 선택 후 나타남)
                        if (selectedCard != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            StanceSelectionSection(
                                selectedStance = selectedStance,
                                onStanceSelected = { selectedStance = it }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        NatureComponents.NatureButton(
                            onClick = { showConfirmDialog = true },
                            enabled = selectedCard != null && selectedStance != null && !uiState.isBattleLoading,
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = NatureColors.leafGreen
                        ) {
                            if (uiState.isBattleLoading) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "처리중...",
                                        style = NatureTypography.labelLarge
                                    )
                                }
                            } else {
                                Text(
                                    text = when {
                                        selectedCard == null -> "카드를 선택해주세요"
                                        selectedStance == null -> "스탠스를 선택해주세요"
                                        params.isResponse -> "대전 수락하기 🤝"
                                        else -> "대전 신청 보내기 ⚔️"
                                    },
                                    style = NatureTypography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(NatureColors.lightBeige)
            ) {
                // 로딩 상태 처리
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NatureColors.forestGreen)
                    }
                    return@Column
                }
                
                // 오류 상태 또는 데이터 없음
                if (uiState.errorMessage != null || availableCards.isEmpty()) {
                    ErrorStateCard(
                        errorMessage = uiState.errorMessage,
                        onRetry = { viewModel.loadTeamCardCollection(params.teamId) },
                        onUseDummy = { viewModel.loadDummyTeamCardCollection(params.teamId) }
                    )
                    return@Column
                }
                
                // 설명 카드
                BattleInstructionCard(selectableCount = selectableCards.size, isResponse = params.isResponse)
                
                // 필터링 섹션 (레어도만)
                CardFiltersSection(
                    cards = availableCards,
                    selectedRarity = rarityFilter.value,
                    showStatusFilter = false,
                    onRaritySelected = { rarityFilter.value = it }
                )

                // 공통 카드 그리드 컴포넌트 사용
                CardGridComponent(
                    cards = filteredCards,
                    selectedCardId = selectedCard,
                    displayMode = CardDisplayMode.SINGLE_SELECT,
                    onCardClick = { card ->
                        // 활성 카드만 선택 가능
                        if (card.isActive) {
                            selectedCard = if (selectedCard == card.cardId) null else card.cardId
                        }
                    },
                    onCardLongClick = {
                        selectedCardForDetail = it
                        showCardDetail = true
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                )
            }
        }

        if (showConfirmDialog) {
            BattleConfirmDialog(
                selectedCards = listOfNotNull(availableCards.find { it.cardId == selectedCard }),
                selectedStance = selectedStance!!,
                targetTeamName = targetTeamName,
                onDismiss = { showConfirmDialog = false },
                onConfirm = {
                    showConfirmDialog = false
                    
                    // 선택된 카드의 teamCardId 찾기
                    val selectedGameCard = availableCards.find { it.cardId == selectedCard }
                    selectedGameCard?.teamCardId?.let { teamCardId ->
                        if (params.isResponse && params.matchId != null) {
                            // 대전 응답
                            viewModel.respondToBattle(
                                matchId = params.matchId,
                                action = "accept",
                                selectedCardTeamCardId = teamCardId,
                                battleStance = selectedStance!!
                            )
                        } else {
                            // 새 대전 신청
                            viewModel.createBattle(
                                challengerTeamId = params.teamId,
                                challengedTeamId = params.targetTeamId,
                                selectedCardTeamCardId = teamCardId,
                                battleStance = selectedStance!!
                            )
                        }
                    }
                }
            )
        }
        
        // 카드 상세보기 Dialog
        CardDetailDialog(
            card = selectedCardForDetail,
            isVisible = showCardDetail,
            onDismiss = {
                showCardDetail = false
                selectedCardForDetail = null
            }
        )
        
        // 대전 결과 Dialog
        BattleResultDialog(
            isVisible = showResultDialog,
            isSuccess = uiState.battleResult != null,
            message = uiState.battleResult?.message ?: uiState.battleErrorMessage ?: "",
            isResponse = params.isResponse,
            onDismiss = { 
                showResultDialog = false
                viewModel.clearBattleResult()
                viewModel.clearBattleErrorMessage()
            },
            onRetry = if (uiState.battleErrorMessage != null) {
                {
                    // 재시도 로직
                    val selectedGameCard = availableCards.find { it.cardId == selectedCard }
                    selectedGameCard?.teamCardId?.let { teamCardId ->
                        if (params.isResponse && params.matchId != null) {
                            viewModel.respondToBattle(
                                matchId = params.matchId,
                                action = "accept", 
                                selectedCardTeamCardId = teamCardId,
                                battleStance = selectedStance!!
                            )
                        } else {
                            viewModel.createBattle(
                                challengerTeamId = params.teamId,
                                challengedTeamId = params.targetTeamId,
                                selectedCardTeamCardId = teamCardId,
                                battleStance = selectedStance!!
                            )
                        }
                    }
                }
            } else null,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

@Composable
fun StanceSelectionSection(
    selectedStance: BattleStance?,
    onStanceSelected: (BattleStance) -> Unit
) {
    Column {
        Text(
            text = "⚔️ 배틀 스탠스 선택",
            style = NatureTypography.titleSmall,
            color = NatureColors.earthBrown,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 공격 버튼
            NatureComponents.NatureCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onStanceSelected(BattleStance.ATTACK) },
                containerColor = if (selectedStance == BattleStance.ATTACK) 
                    NatureColors.forestGreen 
                else 
                    Color.White,
                shape = NatureShapes.small,
                elevation = if (selectedStance == BattleStance.ATTACK) NatureElevation.large else NatureElevation.small
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚔️",
                        fontSize = 24.sp
                    )
                    Text(
                        text = "공격",
                        style = NatureTypography.labelMedium,
                        color = if (selectedStance == BattleStance.ATTACK) 
                            Color.White 
                        else 
                            NatureColors.earthBrown
                    )
                    Text(
                        text = "이기면 100점",
                        style = NatureTypography.labelSmall,
                        color = if (selectedStance == BattleStance.ATTACK) 
                            Color.White.copy(alpha = 0.8f) 
                        else 
                            Color.Gray
                    )
                }
            }
            
            // 방어 버튼
            NatureComponents.NatureCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onStanceSelected(BattleStance.DEFENSE) },
                containerColor = if (selectedStance == BattleStance.DEFENSE) 
                    NatureColors.forestGreen 
                else 
                    Color.White,
                shape = NatureShapes.small,
                elevation = if (selectedStance == BattleStance.DEFENSE) NatureElevation.large else NatureElevation.small
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🛡️",
                        fontSize = 24.sp
                    )
                    Text(
                        text = "방어",
                        style = NatureTypography.labelMedium,
                        color = if (selectedStance == BattleStance.DEFENSE) 
                            Color.White 
                        else 
                            NatureColors.earthBrown
                    )
                    Text(
                        text = "이기면 50점",
                        style = NatureTypography.labelSmall,
                        color = if (selectedStance == BattleStance.DEFENSE) 
                            Color.White.copy(alpha = 0.8f) 
                        else 
                            Color.Gray
                    )
                }
            }
        }
    }
}


@Composable
fun BattleConfirmDialog(
    selectedCards: List<GameCard>,
    selectedStance: BattleStance,
    targetTeamName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val totalAttack = selectedCards.sumOf { it.attack }
    val totalDefense = selectedCards.sumOf { it.defense }
    val totalPower = totalAttack + totalDefense

    Dialog(onDismissRequest = onDismiss) {
        NatureComponents.NatureCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = NatureShapes.extraLarge,
            containerColor = NatureColors.whiteTransparent,
            elevation = NatureElevation.large
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
                    text = "대전 신청 확인",
                    style = NatureTypography.titleLarge,
                    color = NatureColors.forestGreen
                )

                Text(
                    text = "$targetTeamName 에게 대전 신청을 보내시겠습니까?",
                    style = NatureTypography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                NatureComponents.NatureCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = NatureColors.forestGreen.copy(alpha = 0.1f),
                    shape = NatureShapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "선택한 카드",
                            style = NatureTypography.titleSmall,
                            color = NatureColors.forestGreen
                        )
                        
                        selectedCards.forEach { card ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = card.name,
                                    style = NatureTypography.bodyMedium
                                )
                                Text(
                                    text = "${card.attack}/${card.defense}",
                                    style = NatureTypography.bodySmall
                                )
                            }
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.2f))
                        
                        // 선택된 스탠스 표시
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "배틀 스탠스",
                                style = NatureTypography.bodyMedium
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = selectedStance.emoji,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = selectedStance.displayName,
                                    style = NatureTypography.bodyMedium,
                                    color = NatureColors.forestGreen
                                )
                            }
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = totalAttack.toString(),
                                    style = NatureTypography.titleMedium,
                                    color = Color(0xFFF44336)
                                )
                                Text(
                                    text = "총 공격력",
                                    style = NatureTypography.bodySmall
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = totalDefense.toString(),
                                    style = NatureTypography.titleMedium,
                                    color = Color(0xFF2196F3)
                                )
                                Text(
                                    text = "총 방어력",
                                    style = NatureTypography.bodySmall
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = totalPower.toString(),
                                    style = NatureTypography.titleMedium,
                                    color = NatureColors.leafGreen
                                )
                                Text(
                                    text = "종합 전투력",
                                    style = NatureTypography.bodySmall
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NatureComponents.NatureOutlinedButton(
                        onClick = onDismiss,
                        text = "취소",
                        modifier = Modifier.weight(1f)
                    )
                    
                    NatureComponents.NatureButton(
                        onClick = onConfirm,
                        text = "신청 보내기",
                        modifier = Modifier.weight(1f),
                        backgroundColor = NatureColors.leafGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorStateCard(
    errorMessage: String?,
    onRetry: () -> Unit,
    onUseDummy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NatureColors.whiteTransparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "❌ 데이터를 불러올 수 없습니다",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336)
                )
            )
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    ),
                    textAlign = TextAlign.Center
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NatureComponents.NatureOutlinedButton(
                    onClick = onRetry,
                    text = "재시도",
                    modifier = Modifier.weight(1f)
                )
                NatureComponents.NatureButton(
                    onClick = onUseDummy,
                    text = "테스트 데이터",
                    modifier = Modifier.weight(1f),
                    backgroundColor = NatureColors.leafGreen
                )
            }
        }
    }
}

@Composable
private fun BattleInstructionCard(selectableCount: Int, isResponse: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NatureColors.whiteTransparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isResponse) "🤝 대전 수락용 카드를 선택하세요" else "⚔️ 대전 신청용 카드를 선택하세요",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = NatureColors.forestGreen
                )
            )
            Text(
                text = if (isResponse) 
                    "대전을 수락하기 위해 사용할 카드 1장을 선택해주세요. (선택 가능: ${selectableCount}장)"
                else 
                    "대전 신청에서 사용할 카드 1장을 선택해주세요. (선택 가능: ${selectableCount}장)",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            )
        }
    }
}