package com.example.bogoargo.ui.screens.cardgame

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation
import com.example.bogoargo.ui.components.GameCardComponent
import com.example.bogoargo.ui.components.CardDetailDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CardCollectionScreen(
    navController: NavController,
    teamId: Long
) {
    var showCardDetail by remember { mutableStateOf(false) }
    var selectedCardForDetail by remember { mutableStateOf<GameCard?>(null) }
    
    val teamCardCollection = remember {
        TeamCardCollection(
            teamId = teamId,
            cards = listOf(
                GameCard(
                    cardId = 1,
                    name = "불사조 🔥",
                    attack = 85,
                    defense = 70,
                    rarity = CardTier.LEGENDARY,
                    description = "불타는 날개로 적을 소멸시키는 전설의 새"
                ),
                GameCard(
                    cardId = 2,
                    name = "그림자 늑대 🐺",
                    attack = 75,
                    defense = 60,
                    rarity = CardTier.EPIC,
                    description = "어둠 속에서 빠르게 움직이는 늑대"
                ),
                GameCard(
                    cardId = 3,
                    name = "치유의 요정 🧚",
                    attack = 40,
                    defense = 90,
                    rarity = CardTier.RARE,
                    description = "아군을 치유하는 신비한 요정"
                ),
                GameCard(
                    cardId = 4,
                    name = "바위 골렘 🗿",
                    attack = 60,
                    defense = 95,
                    rarity = CardTier.EPIC,
                    description = "단단한 바위로 만들어진 수호자"
                ),
                GameCard(
                    cardId = 5,
                    name = "번개 마법사 ⚡",
                    attack = 80,
                    defense = 50,
                    rarity = CardTier.RARE,
                    description = "번개를 조종하는 강력한 마법사"
                ),
                GameCard(
                    cardId = 6,
                    name = "숲의 수호자 🌳",
                    attack = 65,
                    defense = 75,
                    rarity = CardTier.COMMON,
                    description = "자연을 보호하는 고대의 수호자"
                ),
                GameCard(
                    cardId = 7,
                    name = "얼음 용 🐉",
                    attack = 90,
                    defense = 80,
                    rarity = CardTier.LEGENDARY,
                    description = "차가운 얼음 브레스를 내뿜는 고대 용"
                ),
                GameCard(
                    cardId = 8,
                    name = "기사 ⚔️",
                    attack = 70,
                    defense = 85,
                    rarity = CardTier.COMMON,
                    description = "정의를 위해 싸우는 용감한 기사"
                )
            )
        )
    }

    val rarityFilter = remember { mutableStateOf<CardTier?>(null) }
    val filteredCards = teamCardCollection.cards.filter { card ->
        rarityFilter.value?.let { it == card.rarity } ?: true
    }

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
                CollectionStats(cards = teamCardCollection.cards)
                
                RarityFilterRow(
                    selectedRarity = rarityFilter.value,
                    onRaritySelected = { rarityFilter.value = it }
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCards) { card ->
                        Box(
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
                                        selectedCardForDetail = card
                                        showCardDetail = true
                                    },
                                    onClick = { /* 일반 클릭 처리 */ }
                                )
                        ) {
                            GameCardComponent(card = card)
                        }
                    }
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
            
            Text(
                text = "총 카드 수: ${cards.size}장",
                style = NatureTypography.bodyLarge
            )

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
fun RarityFilterRow(
    selectedRarity: CardTier?,
    onRaritySelected: (CardTier?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            onClick = { onRaritySelected(null) },
            label = { Text("전체", style = NatureTypography.labelMedium) },
            selected = selectedRarity == null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NatureColors.forestGreen,
                selectedLabelColor = Color.White
            )
        )
        CardTier.entries.forEach { rarity ->
            FilterChip(
                onClick = { onRaritySelected(rarity) },
                label = { Text(rarity.displayName, style = NatureTypography.labelMedium) },
                selected = selectedRarity == rarity
            )
        }
    }
}

