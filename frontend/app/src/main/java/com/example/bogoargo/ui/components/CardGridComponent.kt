package com.example.bogoargo.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bogoargo.domain.model.GameCard
import com.example.bogoargo.ui.theme.NatureColors

enum class CardDisplayMode {
    VIEW_ONLY,      // 컬렉션 화면 (보기만)
    SINGLE_SELECT   // 선택 화면 (단일 선택)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardGridComponent(
    cards: List<GameCard>,
    selectedCardId: Long? = null,
    displayMode: CardDisplayMode = CardDisplayMode.VIEW_ONLY,
    onCardClick: ((GameCard) -> Unit)? = null,
    onCardLongClick: ((GameCard) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cards) { card ->
            CardItem(
                card = card,
                isSelected = selectedCardId == card.cardId,
                displayMode = displayMode,
                onClick = { onCardClick?.invoke(card) },
                onLongClick = { onCardLongClick?.invoke(card) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CardItem(
    card: GameCard,
    isSelected: Boolean,
    displayMode: CardDisplayMode,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        GameCardComponent(card = card)
        
        // 선택 모드일 때만 체크마크 표시
        if (displayMode == CardDisplayMode.SINGLE_SELECT && isSelected) {
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