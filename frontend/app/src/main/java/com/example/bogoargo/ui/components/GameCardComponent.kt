package com.example.bogoargo.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bogoargo.domain.model.GameCard
import com.example.bogoargo.domain.model.CardRarity
import com.example.bogoargo.ui.theme.NatureColors

// 레어도별 캐릭터 이미지 설정
private const val NORMAL_IMAGE_SIZE_RATIO = 0.48f
private const val NORMAL_IMAGE_VERTICAL_BIAS = -0.34f // 중앙

private const val LEGENDARY_IMAGE_SIZE_RATIO = 0.46f
private const val LEGENDARY_IMAGE_VERTICAL_BIAS = -0.12f // 중앙 (조정 가능)

enum class ViewMode {
    SIMPLE,
    DETAILED
}

@Composable
fun GameCardComponent(
    card: GameCard,
    modifier: Modifier = Modifier,
    viewMode: ViewMode = ViewMode.SIMPLE,
    onClick: (() -> Unit)? = null,
    alpha: Float = 1f
) {
    
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(2f / 3f) // 가로:세로 = 2:3
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
    ) {
        val cardWidth = this@BoxWithConstraints.maxWidth
        val cardHeight = this@BoxWithConstraints.maxHeight
        
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 캐릭터 이미지 (뒤) - 레어도별 위치와 크기 조정
            val isLegendary = card.rarity == CardRarity.LEGENDARY
            val imageSize = if (isLegendary) LEGENDARY_IMAGE_SIZE_RATIO else NORMAL_IMAGE_SIZE_RATIO
            val imageVerticalBias = if (isLegendary) LEGENDARY_IMAGE_VERTICAL_BIAS else NORMAL_IMAGE_VERTICAL_BIAS
            
            Image(
                painter = painterResource(card.characterImageRes),
                contentDescription = card.name,
                modifier = Modifier
                    .align(BiasAlignment(0f, imageVerticalBias)) // 가로는 중앙, 세로는 bias로 조정
                    .size(cardHeight * imageSize)
                    .graphicsLayer {
                        transformOrigin = TransformOrigin.Center
                    },
                contentScale = ContentScale.Fit,
                alpha = alpha
            )
            
            // 테두리 이미지 (앞)
            Image(
                painter = painterResource(card.borderImageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        transformOrigin = TransformOrigin.Center
                    },
                contentScale = ContentScale.FillBounds,
                alpha = 1f
            )
                
            // 콘텐츠 레이어
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 상단: 카드 이름과 레어도
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = (cardWidth.value * 0.05f).dp, vertical = 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // 카드 이름은 이미 위에 표시됨
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // 레어도 배지는 DETAILED 모드에서만 표시
                    if (viewMode == ViewMode.DETAILED) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(android.graphics.Color.parseColor(card.rarity.color))
                            ),
                            shape = RoundedCornerShape((cardWidth.value * 0.015f).dp),
                            modifier = Modifier
                                .padding(top = (cardHeight.value * 0.02f).dp)
                                .widthIn(max = cardWidth * 0.25f)
                        ) {
                            Text(
                                text = card.rarity.displayName,
                                modifier = Modifier.padding(
                                    horizontal = (cardWidth.value * 0.025f).dp,
                                    vertical = (cardHeight.value * 0.008f).dp
                                ),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (cardWidth.value * 0.05f).sp
                                )
                            )
                        }
                    }
                }

                // 카드 이름은 항상 표시
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (cardWidth.value * 0.07f).sp,
                        color = NatureColors.earthBrown
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(
                        start = (cardWidth.value * 0.05f).dp,
                        top = (cardHeight.value * 0.02f).dp,
                        end = (cardWidth.value * 0.05f).dp
                    )
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // DETAILED 모드에서만 추가 정보 표시
                if (viewMode == ViewMode.DETAILED) {
                    // 설명 텍스트
                    Text(
                        text = card.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = (cardWidth.value * 0.055f).sp,
                            color = Color.Gray.copy(alpha = 0.7f)
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(
                            horizontal = (cardWidth.value * 0.05f).dp,
                            vertical = (cardHeight.value * 0.015f).dp
                        )
                    )

                    // 하단: 스탯 정보
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = (cardWidth.value * 0.05f).dp,
                                vertical = (cardHeight.value * 0.02f).dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatChip(
                            label = "공격",
                            value = card.attack,
                            color = Color(0xFFF44336),
                            modifier = Modifier.width(cardWidth * 0.4f),
                            textSize = (cardWidth.value * 0.06f).sp
                        )
                        StatChip(
                            label = "방어",
                            value = card.defense,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.width(cardWidth * 0.4f),
                            textSize = (cardWidth.value * 0.06f).sp
                        )
                    }
                }
            }
        }
    }
}