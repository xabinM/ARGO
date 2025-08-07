package com.example.bogoargo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bogoargo.domain.model.BattleHistory
import com.example.bogoargo.domain.model.GameCard
import com.example.bogoargo.domain.model.BattleCard
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.components.GameCardComponent
import com.example.bogoargo.ui.components.ViewMode
import com.example.bogoargo.ui.components.CardDetailDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.*

@Composable
fun BattleDetailDialog(
    battle: BattleHistory,
    onDismiss: () -> Unit
) {
    // 카드 상세보기 상태 관리
    var showCardDetail by remember { mutableStateOf(false) }
    var selectedCardForDetail by remember { mutableStateOf<GameCard?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "대전 상세 정보 🎮",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NatureColors.forestGreen
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 대전 기본 정보
                Card(
                    colors = CardDefaults.cardColors(containerColor = NatureColors.whiteTransparent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "vs ${battle.opponentTeamName}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "대전일: ${battle.endedAt}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = when (battle.isWin) {
                                true -> "결과: 승리 🏆 (+${battle.scoreGained}점)"
                                false -> "결과: 패배 💔 (${battle.scoreGained}점)"
                                null -> "결과: 무승부 🤝"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = when (battle.isWin) {
                                    true -> Color(0xFF4CAF50)
                                    false -> Color(0xFFF44336)
                                    null -> Color(0xFF9E9E9E)
                                },
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                
                // 카드 비교
                if (battle.myCard != null && battle.opponentCard != null) {
                    Text(
                        text = "사용된 카드",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NatureColors.earthBrown
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 내 카드
                        BattleCardDisplay(
                            battleCard = battle.myCard,
                            title = "내 카드",
                            isWinner = battle.isWin == true,
                            onCardLongPress = { card ->
                                selectedCardForDetail = card
                                showCardDetail = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // VS 표시
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = NatureColors.forestGreen
                            )
                        )
                        
                        // 상대 카드
                        BattleCardDisplay(
                            battleCard = battle.opponentCard,
                            title = "상대 카드", 
                            isWinner = battle.isWin == false,
                            onCardLongPress = { card ->
                                selectedCardForDetail = card
                                showCardDetail = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NatureColors.leafGreen
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("닫기")
            }
        },
        containerColor = NatureColors.whiteTransparent,
        shape = RoundedCornerShape(16.dp)
    )
    
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BattleCardDisplay(
    battleCard: BattleCard?,
    title: String,
    isWinner: Boolean,
    onCardLongPress: ((GameCard) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (battleCard != null) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 제목과 승자 표시
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isWinner) Color(0xFFE8F5E8) else Color(0xFFFFF8E1)
                ),
                shape = RoundedCornerShape(8.dp),
                border = if (isWinner) BorderStroke(2.dp, Color(0xFF4CAF50)) else null
            ) {
                Text(
                    text = title + if (isWinner) " 🏆" else "",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isWinner) Color(0xFF4CAF50) else NatureColors.earthBrown
                )
            }
            
            // 실제 게임 카드 컴포넌트 사용 (길게 누르기 지원)
            Box(
                modifier = Modifier.combinedClickable(
                    onClick = { /* 일반 클릭 시 아무 동작 없음 */ },
                    onLongClick = { onCardLongPress?.invoke(battleCard.gameCard) }
                )
            ) {
                GameCardComponent(
                    card = battleCard.gameCard,
                    viewMode = ViewMode.SIMPLE,
                    modifier = Modifier.size(width = 90.dp, height = 135.dp) // 2:3 비율 유지
                )
            }
            
            // 스탠스 표시
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (battleCard.battleStance) {
                        com.example.bogoargo.domain.model.BattleStance.ATTACK -> Color(0xFFF44336).copy(alpha = 0.1f)
                        com.example.bogoargo.domain.model.BattleStance.DEFENSE -> Color(0xFF2196F3).copy(alpha = 0.1f)
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    when (battleCard.battleStance) {
                        com.example.bogoargo.domain.model.BattleStance.ATTACK -> Color(0xFFF44336)
                        com.example.bogoargo.domain.model.BattleStance.DEFENSE -> Color(0xFF2196F3)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = battleCard.battleStance.emoji,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = battleCard.battleStance.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (battleCard.battleStance) {
                                com.example.bogoargo.domain.model.BattleStance.ATTACK -> Color(0xFFF44336)
                                com.example.bogoargo.domain.model.BattleStance.DEFENSE -> Color(0xFF2196F3)
                            }
                        )
                    )
                }
            }
        }
    } else {
        // 카드가 없는 경우 (아직 선택하지 않음)
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Gray
                )
            }
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(width = 90.dp, height = 135.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "선택 안함",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}