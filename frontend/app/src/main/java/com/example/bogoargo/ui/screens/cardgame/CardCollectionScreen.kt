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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.navigation.Screen
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
    classId: Long,
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
                
                // 빈 카드 컬렉션일 때 미션 안내 카드 표시
                if (!uiState.isLoading && teamCardCollection?.cards?.isEmpty() == true && uiState.errorMessage == null) {
                    EmptyCardCollectionCard(
                        onStartMission = {
                            navController.navigate(Screen.StudentClassDetail.createRoute(classId))
                        }
                    )
                }
                
                // API 오류 시 재시도 카드 표시
                if (uiState.errorMessage != null) {
                    ErrorCollectionStateCard(
                        errorMessage = uiState.errorMessage,
                        onRetry = {
                            viewModel.clearErrorMessage()
                            viewModel.loadTeamCardCollection(teamId)
                        }
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
fun EmptyCardCollectionCard(
    onStartMission: () -> Unit
) {
    NatureComponents.NatureCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = NatureShapes.large,
        containerColor = NatureColors.whiteTransparent,
        elevation = NatureElevation.medium
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🃏",
                style = MaterialTheme.typography.displayMedium
            )
            
            Text(
                text = "아직 카드가 없어요!",
                style = NatureTypography.titleLarge,
                color = NatureColors.forestGreen,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "미션을 수행하여 카드를 수집해보세요.\n다양한 카드를 모아서 대전에서 승리하세요!",
                style = NatureTypography.bodyMedium,
                color = NatureColors.earthBrown.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            NatureComponents.NatureButton(
                onClick = onStartMission,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NatureColors.leafGreen
            ) {
                Text(
                    text = "🎯 미션 시작하기",
                    style = NatureTypography.labelLarge
                )
            }
        }
    }
}

@Composable
fun ErrorCollectionStateCard(
    errorMessage: String?,
    onRetry: () -> Unit
) {
    NatureComponents.NatureCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = NatureShapes.large,
        containerColor = NatureColors.whiteTransparent,
        elevation = NatureElevation.medium
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.displayMedium
            )
            
            Text(
                text = "카드 컬렉션을 불러올 수 없어요",
                style = NatureTypography.titleLarge,
                color = Color.Red.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            errorMessage?.let {
                Text(
                    text = it,
                    style = NatureTypography.bodyMedium,
                    color = NatureColors.earthBrown.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
            
            NatureComponents.NatureButton(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NatureColors.forestGreen
            ) {
                Text(
                    text = "🔄 다시 시도",
                    style = NatureTypography.labelLarge
                )
            }
        }
    }
}
