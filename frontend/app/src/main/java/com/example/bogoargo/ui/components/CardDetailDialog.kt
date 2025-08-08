package com.example.bogoargo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.bogoargo.domain.model.GameCard

/**
 * 카드 상세보기 Dialog 컴포넌트
 * 앱 전체에서 일관된 카드 상세보기 경험을 제공합니다.
 * 
 * @param card 표시할 카드 (null이면 Dialog가 표시되지 않음)
 * @param isVisible Dialog 표시 여부
 * @param onDismiss Dialog 닫기 콜백
 */
@Composable
fun CardDetailDialog(
    card: GameCard?,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (isVisible && card != null) {
        Dialog(onDismissRequest = onDismiss) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDismiss() }
            ) {
                GameCardComponent(
                    card = card,
                    viewMode = ViewMode.DETAILED,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * 카드 상세보기 상태를 관리하는 데이터 클래스
 */
data class CardDetailState(
    val isVisible: Boolean,
    val selectedCard: GameCard?,
    val show: (GameCard) -> Unit,
    val hide: () -> Unit
)