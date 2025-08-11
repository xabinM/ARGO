package com.example.bogoargo.ui.screens.cardgame

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation
import com.example.bogoargo.ui.components.GameCardComponent
import com.example.bogoargo.ui.components.CardDetailDialog
import com.example.bogoargo.ui.components.CardGridComponent
import com.example.bogoargo.ui.components.CardDisplayMode
import com.example.bogoargo.ui.components.CardFiltersSection
import com.example.bogoargo.ui.components.CardStatusFilter
import com.example.bogoargo.ui.viewmodels.cardgame.CardCollectionViewModel
import com.example.bogoargo.data.mapper.TeamCardCollection

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CardCollectionScreen(
    navController: NavController,
    teamId: Long,
    viewModel: CardCollectionViewModel = hiltViewModel()
) {
    var showCardDetail by remember { mutableStateOf(false) }
    var selectedCardForDetail by remember { mutableStateOf<GameCard?>(null) }
    
    val uiState by viewModel.uiState.collectAsState()
    val teamCardCollection = uiState.teamCardCollection
    
    // 화면 진입 시 자동으로 팀 카드 컬렉션 조회
    LaunchedEffect(teamId) {
        viewModel.loadTeamCardCollection(teamId)
    }

    val rarityFilter = remember { mutableStateOf<CardTier?>(null) }
    val statusFilter = remember { mutableStateOf(CardStatusFilter.ALL) }
    
    val filteredCards = teamCardCollection?.cards?.filter { card ->
        // 레어도 필터링
        val rarityMatch = rarityFilter.value?.let { it == card.rarity } ?: true
        
        // 상태 필터링
        val statusMatch = when (statusFilter.value) {
            CardStatusFilter.ALL -> true
            CardStatusFilter.ACTIVE -> card.isActive
            CardStatusFilter.LOCKED -> card.isLocked
            CardStatusFilter.LOST -> card.isLost
        }
        
        rarityMatch && statusMatch
    } ?: emptyList()

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "우리 팀 카드 컬렉션",
                emoji = "🃏",
                onNavigationClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 로딩 상태 표시
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = NatureColors.forestGreen
                            )
                            Text(
                                text = "팀 카드 컬렉션을 불러오는 중...",
                                style = NatureTypography.bodyMedium,
                                color = NatureColors.earthBrown
                            )
                        }
                    }
                }
                
                // 오류나 빈 데이터일 때 테스트 버튼 표시
                if (uiState.errorMessage != null || (!uiState.isLoading && (teamCardCollection?.cards?.isEmpty() == true))) {
                    TestDataButtonsRow(
                        onRetryClick = { viewModel.loadTeamCardCollection(teamId) },
                        onDummyDataClick = { viewModel.loadDummyTeamCardCollection(teamId) },
                        errorMessage = uiState.errorMessage
                    )
                }
                
                teamCardCollection?.let { collection ->
                    CollectionStats(cards = collection.cards)
                    
                    // 공통 필터링 컴포넌트 사용
                    CardFiltersSection(
                        cards = collection.cards,
                        selectedRarity = rarityFilter.value,
                        selectedStatus = statusFilter.value,
                        showStatusFilter = true,
                        onRaritySelected = { rarityFilter.value = it },
                        onStatusSelected = { statusFilter.value = it }
                    )

                    // 공통 카드 그리드 컴포넌트 사용
                    CardGridComponent(
                        cards = filteredCards,
                        displayMode = CardDisplayMode.VIEW_ONLY,
                        onCardLongClick = {
                            selectedCardForDetail = it
                            showCardDetail = true
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }
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
}

@Composable
fun CollectionStats(cards: List<GameCard>) {
    val rarityGroups = cards.groupBy { it.rarity }
    val activeCards = cards.filter { it.isActive }
    val lockedCards = cards.filter { it.isLocked }
    val lostCards = cards.filter { it.isLost }
    
    NatureComponents.NatureCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = NatureShapes.large,
        containerColor = NatureColors.whiteTransparent,
        elevation = NatureElevation.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 컬렉션 현황",
                style = NatureTypography.titleLarge,
                color = NatureColors.forestGreen
            )
            
            // 카드 상태 통계
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = cards.size.toString(),
                        style = NatureTypography.titleMedium,
                        color = NatureColors.earthBrown
                    )
                    Text(
                        text = "전체",
                        style = NatureTypography.labelSmall,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = activeCards.size.toString(),
                        style = NatureTypography.titleMedium,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "활성",
                        style = NatureTypography.labelSmall,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = lockedCards.size.toString(),
                        style = NatureTypography.titleMedium,
                        color = Color(0xFFFF9800)
                    )
                    Text(
                        text = "사용중",
                        style = NatureTypography.labelSmall,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = lostCards.size.toString(),
                        style = NatureTypography.titleMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "제거됨",
                        style = NatureTypography.labelSmall,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
            }

            // 등급별 통계 (기존 유지)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CardTier.entries.forEach { rarity ->
                    val count = rarityGroups[rarity]?.size ?: 0
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = count.toString(),
                            style = NatureTypography.titleMedium,
                            color = Color(android.graphics.Color.parseColor(rarity.color))
                        )
                        Text(
                            text = rarity.displayName,
                            style = NatureTypography.labelSmall,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun TestDataButtonsRow(
    onRetryClick: () -> Unit,
    onDummyDataClick: () -> Unit,
    errorMessage: String?
) {
    NatureComponents.NatureCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = NatureShapes.medium,
        containerColor = NatureColors.whiteTransparent,
        elevation = NatureElevation.small
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (errorMessage != null) {
                Text(
                    text = "⚠️ $errorMessage",
                    style = NatureTypography.bodyMedium,
                    color = Color.Red.copy(alpha = 0.8f)
                )
            } else {
                Text(
                    text = "💡 DB에 카드 컬렉션 데이터가 없습니다.",
                    style = NatureTypography.bodyMedium,
                    color = NatureColors.earthBrown.copy(alpha = 0.7f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NatureComponents.NatureButton(
                    onClick = onRetryClick,
                    modifier = Modifier.weight(1f),
                    backgroundColor = NatureColors.forestGreen,
                    contentColor = Color.White
                ) {
                    Text(
                        text = "🔄 API 재요청",
                        style = NatureTypography.labelLarge
                    )
                }
                
                NatureComponents.NatureButton(
                    onClick = onDummyDataClick,
                    modifier = Modifier.weight(1f),
                    backgroundColor = NatureColors.sunnyYellow,
                    contentColor = NatureColors.earthBrown
                ) {
                    Text(
                        text = "🧪 테스트 데이터",
                        style = NatureTypography.labelLarge
                    )
                }
            }
        }
    }
}
