package com.example.bogoargo.ui.screens.cardgame

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.navigation.NavController
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.components.GameCardComponent
import com.example.bogoargo.ui.components.StatChip
import com.example.bogoargo.ui.components.ViewMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CardSelectionScreen(
    navController: NavController,
    teamId: Long,
    targetTeamId: Long
) {
    val availableCards = remember {
        listOf(
            GameCard(
                cardId = 1,
                name = "불사조 🔥",
                attack = 85,
                defense = 70,
                rarity = CardRarity.LEGENDARY,
                description = "불타는 날개로 적을 소멸시키는 전설의 새"
            ),
            GameCard(
                cardId = 2,
                name = "그림자 늑대 🐺",
                attack = 75,
                defense = 60,
                rarity = CardRarity.EPIC,
                description = "어둠 속에서 빠르게 움직이는 늑대"
            ),
            GameCard(
                cardId = 3,
                name = "치유의 요정 🧚",
                attack = 40,
                defense = 90,
                rarity = CardRarity.RARE,
                description = "아군을 치유하는 신비한 요정"
            ),
            GameCard(
                cardId = 4,
                name = "바위 골렘 🗿",
                attack = 60,
                defense = 95,
                rarity = CardRarity.EPIC,
                description = "단단한 바위로 만들어진 수호자"
            ),
            GameCard(
                cardId = 5,
                name = "번개 마법사 ⚡",
                attack = 80,
                defense = 50,
                rarity = CardRarity.RARE,
                description = "번개를 조종하는 강력한 마법사"
            ),
            GameCard(
                cardId = 6,
                name = "숲의 수호자 🌳",
                attack = 65,
                defense = 75,
                rarity = CardRarity.COMMON,
                description = "자연을 보호하는 고대의 수호자"
            )
        )
    }

    var selectedCard by remember { mutableStateOf<Long?>(null) }
    var showCardDetail by remember { mutableStateOf(false) }
    var selectedCardForDetail by remember { mutableStateOf<GameCard?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val targetTeamName = remember {
        when (targetTeamId) {
            2L -> "불사조 팀 🔥"
            3L -> "그리핀 팀 🦅"
            4L -> "유니콘 팀 🦄"
            5L -> "드래곤 팀 🐉"
            else -> "상대팀"
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("카드 선택 ${if (selectedCard != null) "(1/1)" else "(0/1)"}") },
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
            },
            bottomBar = {
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
                            text = "vs $targetTeamName",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NatureColors.forestGreen
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Button(
                            onClick = { showConfirmDialog = true },
                            enabled = selectedCard != null,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NatureColors.leafGreen,
                                disabledContainerColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (selectedCard != null) 
                                    "대전 신청 보내기 ⚔️" 
                                else 
                                    "카드를 선택해주세요",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
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
                            text = "🃏 대전용 카드를 선택하세요",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = NatureColors.forestGreen
                            )
                        )
                        Text(
                            text = "대전에서 사용할 카드 1장을 선택해주세요. 선택한 카드의 능력치가 승부에 영향을 줍니다.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                        )
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(availableCards) { card ->
                        Box(
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        selectedCard = if (selectedCard == card.cardId) {
                                            null // 이미 선택된 카드 클릭 시 선택 해제
                                        } else {
                                            card.cardId // 새 카드 선택
                                        }
                                    },
                                    onLongClick = {
                                        selectedCardForDetail = card
                                        showCardDetail = true
                                    }
                                )
                        ) {
                            GameCardComponent(card = card)
                            
                            // 선택 체크마크
                            if (selectedCard == card.cardId) {
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = NatureColors.leafGreen),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "선택됨",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showConfirmDialog) {
            BattleConfirmDialog(
                selectedCards = listOfNotNull(availableCards.find { it.cardId == selectedCard }),
                targetTeamName = targetTeamName,
                onDismiss = { showConfirmDialog = false },
                onConfirm = {
                    showConfirmDialog = false
                    navController.popBackStack()
                    navController.popBackStack()
                    navController.popBackStack()
                }
            )
        }
        
        // 카드 상세보기 Dialog
        if (showCardDetail && selectedCardForDetail != null) {
            Dialog(onDismissRequest = { showCardDetail = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showCardDetail = false }
                ) {
                    GameCardComponent(
                        card = selectedCardForDetail!!,
                        viewMode = ViewMode.DETAILED,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun SelectableCardItem(
    card: GameCard,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    isSelectable: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = NatureColors.leafGreen,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(enabled = isSelectable) {
                onSelectionChanged(!isSelected)
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(android.graphics.Color.parseColor(card.rarity.color)).copy(alpha = 0.2f)
            } else if (isSelectable) {
                Color(android.graphics.Color.parseColor(card.rarity.color)).copy(alpha = 0.1f)
            } else {
                Color.Gray.copy(alpha = 0.3f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
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
                                color = if (isSelectable) 
                                    NatureColors.earthBrown
                                else 
                                    NatureColors.earthBrown.copy(alpha = 0.5f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Card(
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
                            color = if (isSelectable)
                                Color.Gray.copy(alpha = 0.7f)
                            else
                                Color.Gray.copy(alpha = 0.4f)
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
                        color = Color(0xFFF44336).copy(alpha = if (isSelectable) 1f else 0.5f)
                    )
                    StatChip(
                        label = "방어",
                        value = card.defense,
                        color = Color(0xFF2196F3).copy(alpha = if (isSelectable) 1f else 0.5f)
                    )
                }
            }

            if (isSelected) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = NatureColors.leafGreen),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "선택됨",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(16.dp)
                    )
                }
            }

            if (!isSelectable && !isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
fun BattleConfirmDialog(
    selectedCards: List<GameCard>,
    targetTeamName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val totalAttack = selectedCards.sumOf { it.attack }
    val totalDefense = selectedCards.sumOf { it.defense }
    val totalPower = totalAttack + totalDefense

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
                    text = "대전 신청 확인",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NatureColors.forestGreen
                    )
                )

                Text(
                    text = "$targetTeamName 에게 대전 신청을 보내시겠습니까?",
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "선택한 카드",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NatureColors.forestGreen
                            )
                        )
                        
                        selectedCards.forEach { card ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = card.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${card.attack}/${card.defense}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium
                                    )
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
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF44336)
                                    )
                                )
                                Text(
                                    text = "총 공격력",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = totalDefense.toString(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2196F3)
                                    )
                                )
                                Text(
                                    text = "총 방어력",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = totalPower.toString(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NatureColors.leafGreen
                                    )
                                )
                                Text(
                                    text = "종합 전투력",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
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
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NatureColors.leafGreen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("신청 보내기")
                    }
                }
            }
        }
    }
}