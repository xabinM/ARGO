package com.example.bogoargo.ui.screens.cardgame

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.ui.theme.NatureColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardCollectionScreen(
    navController: NavController,
    teamId: Long
) {
    val teamCardCollection = remember {
        TeamCardCollection(
            teamId = teamId,
            cards = listOf(
                GameCard(
                    cardId = 1,
                    name = "불사조 🔥",
                    attack = 85,
                    defense = 70,
                    rarity = CardRarity.LEGENDARY,
                    imageUrl = "",
                    description = "불타는 날개로 적을 소멸시키는 전설의 새"
                ),
                GameCard(
                    cardId = 2,
                    name = "그림자 늑대 🐺",
                    attack = 75,
                    defense = 60,
                    rarity = CardRarity.EPIC,
                    imageUrl = "",
                    description = "어둠 속에서 빠르게 움직이는 늑대"
                ),
                GameCard(
                    cardId = 3,
                    name = "치유의 요정 🧚",
                    attack = 40,
                    defense = 90,
                    rarity = CardRarity.RARE,
                    imageUrl = "",
                    description = "아군을 치유하는 신비한 요정"
                ),
                GameCard(
                    cardId = 4,
                    name = "바위 골렘 🗿",
                    attack = 60,
                    defense = 95,
                    rarity = CardRarity.EPIC,
                    imageUrl = "",
                    description = "단단한 바위로 만들어진 수호자"
                ),
                GameCard(
                    cardId = 5,
                    name = "번개 마법사 ⚡",
                    attack = 80,
                    defense = 50,
                    rarity = CardRarity.RARE,
                    imageUrl = "",
                    description = "번개를 조종하는 강력한 마법사"
                ),
                GameCard(
                    cardId = 6,
                    name = "숲의 수호자 🌳",
                    attack = 65,
                    defense = 75,
                    rarity = CardRarity.COMMON,
                    imageUrl = "",
                    description = "자연을 보호하는 고대의 수호자"
                ),
                GameCard(
                    cardId = 7,
                    name = "얼음 용 🐉",
                    attack = 90,
                    defense = 80,
                    rarity = CardRarity.LEGENDARY,
                    imageUrl = "",
                    description = "차가운 얼음 브레스를 내뿜는 고대 용"
                ),
                GameCard(
                    cardId = 8,
                    name = "기사 ⚔️",
                    attack = 70,
                    defense = 85,
                    rarity = CardRarity.COMMON,
                    imageUrl = "",
                    description = "정의를 위해 싸우는 용감한 기사"
                )
            )
        )
    }

    val rarityFilter = remember { mutableStateOf<CardRarity?>(null) }
    val filteredCards = teamCardCollection.cards.filter { card ->
        rarityFilter.value?.let { it == card.rarity } ?: true
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("우리 팀 카드 컬렉션 🃏") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(NatureColors.lightBeige)
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
                        GameCardItem(card = card)
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionStats(cards: List<GameCard>) {
    val rarityGroups = cards.groupBy { it.rarity }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NatureColors.whiteTransparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 컬렉션 현황",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = NatureColors.forestGreen
                )
            )
            
            Text(
                text = "총 카드 수: ${cards.size}장",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CardRarity.values().forEach { rarity ->
                    val count = rarityGroups[rarity]?.size ?: 0
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(android.graphics.Color.parseColor(rarity.color))
                            )
                        )
                        Text(
                            text = rarity.displayName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RarityFilterRow(
    selectedRarity: CardRarity?,
    onRaritySelected: (CardRarity?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            onClick = { onRaritySelected(null) },
            label = { Text("전체") },
            selected = selectedRarity == null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NatureColors.forestGreen,
                selectedLabelColor = Color.White
            )
        )
        CardRarity.values().forEach { rarity ->
            FilterChip(
                onClick = { onRaritySelected(rarity) },
                label = { Text(rarity.displayName) },
                selected = selectedRarity == rarity,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(android.graphics.Color.parseColor(rarity.color)),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun GameCardItem(card: GameCard) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(android.graphics.Color.parseColor(card.rarity.color)).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NatureColors.earthBrown
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(android.graphics.Color.parseColor(card.rarity.color))
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = card.rarity.displayName,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = card.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray.copy(alpha = 0.7f)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatChip(
                    label = "공격",
                    value = card.attack,
                    color = Color(0xFFF44336)
                )
                StatChip(
                    label = "방어",
                    value = card.defense,
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
fun StatChip(
    label: String,
    value: Int,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            )
        }
    }
}