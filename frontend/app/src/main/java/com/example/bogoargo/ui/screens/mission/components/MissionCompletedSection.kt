package com.example.bogoargo.ui.screens.mission.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bogoargo.domain.model.CardTier
import com.example.bogoargo.domain.model.GameCard
import com.example.bogoargo.domain.model.MissionSubmitResult
import com.example.bogoargo.ui.components.GameCardComponent
import com.example.bogoargo.ui.components.ViewMode
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureTypography
import kotlinx.coroutines.delay

@Composable
fun MissionCompletedSection(
    submitResult: MissionSubmitResult?,
    isMissionSuccessful: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToGame: () -> Unit
) {
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 성공/실패에 따른 이모지와 제목
            if (isMissionSuccessful) {
                Text(
                    text = "🎉",
                    style = NatureTypography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "미션 성공!",
                    style = NatureTypography.titleLarge,
                    color = NatureColors.forestGreen
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "축하합니다! 미션을 성공적으로 완료했어요 🎊",
                    style = NatureTypography.bodyMedium,
                    color = NatureColors.earthBrown,
                    textAlign = TextAlign.Center
                )
                
                // 카드 획득 애니메이션 (성공 시에만)
                submitResult?.cardId?.let { cardId ->
                    submitResult.tier?.let { tierString ->
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // 카드 등장 애니메이션 상태
                        var showCard by remember { mutableStateOf(false) }
                        
                        // 컴포넌트가 렌더링되면 카드 표시 시작
                        LaunchedEffect(Unit) {
                            delay(300) // 약간의 지연 후 카드 등장
                            showCard = true
                        }
                        
                        // 애니메이션 값들
                        val infiniteTransition = rememberInfiniteTransition(label = "card_glow")
                        
                        // 카드 등장 애니메이션
                        val cardScale by animateFloatAsState(
                            targetValue = if (showCard) 1f else 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "card_scale"
                        )
                        
                        val cardRotation by animateFloatAsState(
                            targetValue = if (showCard) 360f else 0f,
                            animationSpec = tween(
                                durationMillis = 1000,
                                easing = FastOutSlowInEasing
                            ),
                            label = "card_rotation"
                        )
                        
                        val cardAlpha by animateFloatAsState(
                            targetValue = if (showCard) 1f else 0f,
                            animationSpec = tween(durationMillis = 500),
                            label = "card_alpha"
                        )
                        // 빛나는 효과 애니메이션
                        val glowAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.5f, // 변경: 0.3f -> 0.5f
                            targetValue = 1.0f, // 변경: 0.7f -> 1.0f
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 1000, // 변경: 1500 -> 1000
                                    easing = FastOutSlowInEasing // 추가
                                ),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "glow_alpha"
                        )
                        
                        // GameCard 생성
                        val tier = CardTier.fromString(tierString)
                        val gameCard = remember(cardId, tier) {
                            GameCard.create(cardId, tier)
                        }
                        
                        // 레어도별 글로우 색상 (CardTier enum의 color 필드 사용)
                        val glowColor = Color(android.graphics.Color.parseColor(tier.color))
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🎴 획득한 카드",
                                style = NatureTypography.titleSmall,
                                color = NatureColors.forestGreen,
                                modifier = Modifier.alpha(cardAlpha)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // 카드 컨테이너 with 글로우 효과
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(width = 180.dp, height = 270.dp)
                            ) {
                                // 글로우 배경 효과
                                if (showCard && tier != CardTier.COMMON) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .scale(1.2f) // 예시: 1.2f -> 1.3f
                                            .blur(radius = 15.dp) // 예시: 20.dp -> 25.dp
                                            .background(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(
                                                        glowColor.copy(alpha = glowAlpha),
                                                        Color.Transparent
                                                    )
                                                ),
                                                shape = CircleShape
                                            )
                                    )
                                }


                                // 게임 카드 컴포넌트
                                GameCardComponent(
                                    card = gameCard,
                                    viewMode = ViewMode.SIMPLE,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .scale(cardScale)
                                        .graphicsLayer {
                                            rotationY = cardRotation
                                            cameraDistance = 12f * density
                                        }
                                        .alpha(cardAlpha)
                                )
                            }
                            
                            // 카드 정보 텍스트
                            if (showCard) {
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = NatureColors.leafGreen.copy(alpha = 0.1f)
                                    ),
                                    modifier = Modifier.alpha(cardAlpha)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 8.dp
                                        ),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = gameCard.name,
                                            style = NatureTypography.titleSmall,
                                            color = NatureColors.forestGreen
                                        )
                                        Text(
                                            text = tier.displayName,
                                            style = NatureTypography.bodyMedium,
                                            color = Color(android.graphics.Color.parseColor(tier.color))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // 실패 케이스
                Text(
                    text = "😔",
                    style = NatureTypography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "미션 실패!",
                    style = NatureTypography.titleLarge,
                    color = NatureColors.earthBrown
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "아쉽게도 이번 미션은 실패했어요.\n다른 미션을 찾아보세요! 🎯",
                    style = NatureTypography.bodyMedium,
                    color = NatureColors.earthBrown.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 성공/실패에 따른 다른 버튼 구성
            if (isMissionSuccessful) {
                NatureComponents.NatureButton(
                    onClick = onNavigateToGame,
                    text = "🎉 완료",
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = NatureColors.forestGreen
                )
            } else {
                NatureComponents.NatureButton(
                    onClick = onNavigateToGame,
                    text = "나가기",
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = NatureColors.earthBrown.copy(alpha = 0.7f)
                )
            }
        }
    }
}