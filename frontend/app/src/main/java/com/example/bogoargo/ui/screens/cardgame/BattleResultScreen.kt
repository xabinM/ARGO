package com.example.bogoargo.ui.screens.cardgame

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bogoargo.domain.model.BattleCard
import com.example.bogoargo.domain.model.BattleStance
import com.example.bogoargo.ui.components.GameCardComponent
import com.example.bogoargo.ui.components.ViewMode
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import kotlin.random.Random

enum class BattleAnimationPhase {
    INITIAL,
    ENTERING,
    STANCE_SHOWING,
    COLLIDING,
    RESULT_SHOWING,
    SCORE_SHOWING,
    COMPLETED
}

@Composable
fun BattleResultScreen(
    navController: NavController,
    myBattleCard: BattleCard,
    opponentBattleCard: BattleCard,
    isWin: Boolean,
    isDraw: Boolean = false,
    myTeamName: String = "우리 팀",
    opponentTeamName: String = "상대 팀"
) {
    var animationPhase by remember { mutableStateOf(BattleAnimationPhase.INITIAL) }

    // 게임 규칙에 따른 점수 계산
    val scoreChange = remember {
        calculateScore(myBattleCard.battleStance, opponentBattleCard.battleStance, isWin, isDraw)
    }

    // 카드 파괴 여부 계산
    val isMyCardDestroyed = remember {
        isCardDestroyed(myBattleCard.battleStance, opponentBattleCard.battleStance, isWin, isDraw)
    }

    val isOpponentCardDestroyed = remember {
        val isOpponentWin = when {
            isDraw -> false  // 무승부는 아무도 이기지 않음
            else -> !isWin   // 내가 패배 = 상대 승리
        }
        isCardDestroyed(
            opponentBattleCard.battleStance,
            myBattleCard.battleStance,
            isOpponentWin,
            isDraw
        )
    }

    var displayedScore by remember { mutableStateOf(0) }
    var showParticles by remember { mutableStateOf(false) }

    // 스탠스 팝업 애니메이션 상태
    val stancePopupScale = animateFloatAsState(
        targetValue = when (animationPhase) {
            BattleAnimationPhase.STANCE_SHOWING -> 1.5f
            else -> 0f
        },
        animationSpec = if (animationPhase == BattleAnimationPhase.STANCE_SHOWING) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        } else {
            tween(300)
        },
        label = "stancePopupScale"
    )

    val stancePopupAlpha = animateFloatAsState(
        targetValue = when (animationPhase) {
            BattleAnimationPhase.STANCE_SHOWING -> 1f
            else -> 0f
        },
        animationSpec = tween(
            durationMillis = if (animationPhase == BattleAnimationPhase.STANCE_SHOWING) 300 else 500
        ),
        label = "stancePopupAlpha"
    )

    // 카드 정보 알파 (STANCE_SHOWING부터 표시)
    val cardInfoAlpha = animateFloatAsState(
        targetValue = when (animationPhase) {
            BattleAnimationPhase.STANCE_SHOWING,
            BattleAnimationPhase.COLLIDING,
            BattleAnimationPhase.RESULT_SHOWING,
            BattleAnimationPhase.SCORE_SHOWING,
            BattleAnimationPhase.COMPLETED -> 1f

            else -> 0f
        },
        animationSpec = tween(500),
        label = "cardInfoAlpha"
    )

    val myCardOffset = animateFloatAsState(
        targetValue = when (animationPhase) {
            BattleAnimationPhase.INITIAL -> -1000f
            BattleAnimationPhase.ENTERING -> 0f
            else -> 0f
        },
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "myCardOffset"
    )

    val opponentCardOffset = animateFloatAsState(
        targetValue = when (animationPhase) {
            BattleAnimationPhase.INITIAL -> 1000f
            BattleAnimationPhase.ENTERING -> 0f
            else -> 0f
        },
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "opponentCardOffset"
    )

    val collisionScale = animateFloatAsState(
        targetValue = when (animationPhase) {
            BattleAnimationPhase.COLLIDING -> 1.2f
            BattleAnimationPhase.RESULT_SHOWING -> if (isWin) 1.3f else 0.8f
            BattleAnimationPhase.SCORE_SHOWING, BattleAnimationPhase.COMPLETED -> if (isWin) 1.2f else 0.9f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "collisionScale"
    )

    val opponentScale = animateFloatAsState(
        targetValue = when (animationPhase) {
            BattleAnimationPhase.COLLIDING -> 1.2f
            BattleAnimationPhase.RESULT_SHOWING -> if (!isWin) 1.3f else 0.8f
            BattleAnimationPhase.SCORE_SHOWING, BattleAnimationPhase.COMPLETED -> if (!isWin) 1.2f else 0.9f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "opponentScale"
    )

    val resultAlpha = animateFloatAsState(
        targetValue = when (animationPhase) {
            BattleAnimationPhase.RESULT_SHOWING, BattleAnimationPhase.SCORE_SHOWING, BattleAnimationPhase.COMPLETED -> 1f
            else -> 0f
        },
        animationSpec = tween(500),
        label = "resultAlpha"
    )

    val scoreAlpha = animateFloatAsState(
        targetValue = when (animationPhase) {
            BattleAnimationPhase.SCORE_SHOWING, BattleAnimationPhase.COMPLETED -> 1f
            else -> 0f
        },
        animationSpec = tween(500),
        label = "scoreAlpha"
    )

    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(100)
        animationPhase = BattleAnimationPhase.ENTERING
        delay(1000)
        animationPhase = BattleAnimationPhase.STANCE_SHOWING
        delay(1500)  // 스탠스 표시 시간
        animationPhase = BattleAnimationPhase.COLLIDING

        shakeOffset.snapTo(10f)
        shakeOffset.animateTo(
            targetValue = 0f,
            animationSpec = spring<Float>(
                dampingRatio = 0.3f,
                stiffness = 5000f
            )
        )

        delay(500)
        animationPhase = BattleAnimationPhase.RESULT_SHOWING
        showParticles = isWin
        delay(1500)
        animationPhase = BattleAnimationPhase.SCORE_SHOWING

        val step = if (scoreChange > 0) 5 else -2
        while (displayedScore != scoreChange) {
            delay(30)
            displayedScore =
                if ((scoreChange - displayedScore).absoluteValue < step.absoluteValue) {
                    scoreChange
                } else {
                    displayedScore + step
                }
        }

        delay(500)
        animationPhase = BattleAnimationPhase.COMPLETED
    }

    NatureComponents.NatureBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = shakeOffset.value
                }
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = NatureColors.earthBrown
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "⚔️ 배틀 결과 ⚔️",
                    style = NatureTypography.titleLarge,
                    color = NatureColors.earthBrown,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 24.dp)
                    ) {
                        Text(
                            text = myTeamName,
                            style = NatureTypography.titleMedium,
                            color = NatureColors.forestGreen,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // 카드 이름 - STANCE_SHOWING 단계부터 페이드인
                        AnimatedVisibility(
                            visible = animationPhase >= BattleAnimationPhase.STANCE_SHOWING,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            NatureComponents.NatureCard(
                                containerColor = NatureColors.forestGreen,
                                shape = NatureShapes.small,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = myBattleCard.gameCard.name,
                                    style = NatureTypography.titleMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            GameCardComponent(
                                card = myBattleCard.gameCard,
                                modifier = Modifier
                                    .width(140.dp)
                                    .graphicsLayer {
                                        translationX = myCardOffset.value
                                        scaleX = collisionScale.value
                                        scaleY = collisionScale.value
                                        // 카드 파괴 시 알파값 감소
                                        alpha =
                                            if (isMyCardDestroyed && animationPhase >= BattleAnimationPhase.RESULT_SHOWING) {
                                                0.3f
                                            } else 1f
                                    },
                                viewMode = ViewMode.SIMPLE
                            )

                            // 카드 파괴 이펙트
                            if (isMyCardDestroyed && animationPhase >= BattleAnimationPhase.RESULT_SHOWING) {
                                DestroyedCardEffect(
                                    modifier = Modifier.matchParentSize()
                                )
                            }

                            // 스탠스 팝업 애니메이션
                            if (animationPhase == BattleAnimationPhase.STANCE_SHOWING) {
                                Text(
                                    text = myBattleCard.battleStance.emoji,
                                    fontSize = 24.sp,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .scale(stancePopupScale.value)
                                        .alpha(stancePopupAlpha.value)
                                )
                            }

                            if (showParticles && isWin) {
                                ParticleEffect(
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                        }

                        // 스탯 정보 - STANCE_SHOWING 단계부터 페이드인
                        AnimatedVisibility(
                            visible = animationPhase >= BattleAnimationPhase.STANCE_SHOWING,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            NatureComponents.NatureCard(
                                containerColor = Color.Black.copy(alpha = 0.7f),
                                shape = NatureShapes.medium,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "⚔️",
                                            fontSize = 20.sp
                                        )
                                        Text(
                                            text = "${myBattleCard.gameCard.attack}",
                                            style = NatureTypography.titleMedium,
                                            color = Color.White
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "🛡️",
                                            fontSize = 20.sp
                                        )
                                        Text(
                                            text = "${myBattleCard.gameCard.defense}",
                                            style = NatureTypography.titleMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "VS",
                            style = NatureTypography.titleLarge.copy(
                                fontSize = 32.sp
                            ),
                            color = NatureColors.sunnyYellow,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 24.dp)
                    ) {
                        Text(
                            text = opponentTeamName,
                            style = NatureTypography.titleMedium,
                            color = NatureColors.earthBrown,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // 카드 이름 - STANCE_SHOWING 단계부터 페이드인
                        AnimatedVisibility(
                            visible = animationPhase >= BattleAnimationPhase.STANCE_SHOWING,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            NatureComponents.NatureCard(
                                containerColor = NatureColors.earthBrown,
                                shape = NatureShapes.small,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = opponentBattleCard.gameCard.name,
                                    style = NatureTypography.titleMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            GameCardComponent(
                                card = opponentBattleCard.gameCard,
                                modifier = Modifier
                                    .width(140.dp)
                                    .graphicsLayer {
                                        translationX = opponentCardOffset.value
                                        scaleX = opponentScale.value
                                        scaleY = opponentScale.value
                                        // 카드 파괴 시 알파값 감소
                                        alpha =
                                            if (isOpponentCardDestroyed && animationPhase >= BattleAnimationPhase.RESULT_SHOWING) {
                                                0.3f
                                            } else 1f
                                    },
                                viewMode = ViewMode.SIMPLE
                            )

                            // 카드 파괴 이펙트
                            if (isOpponentCardDestroyed && animationPhase >= BattleAnimationPhase.RESULT_SHOWING) {
                                DestroyedCardEffect(
                                    modifier = Modifier.matchParentSize()
                                )
                            }

                            // 스탠스 팝업 애니메이션
                            if (animationPhase == BattleAnimationPhase.STANCE_SHOWING) {
                                Text(
                                    text = opponentBattleCard.battleStance.emoji,
                                    fontSize = 32.sp,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .scale(stancePopupScale.value)
                                        .alpha(stancePopupAlpha.value)
                                )
                            }

                            if (showParticles && !isWin) {
                                ParticleEffect(
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                        }

                        // 스탯 정보 - STANCE_SHOWING 단계부터 페이드인
                        AnimatedVisibility(
                            visible = animationPhase >= BattleAnimationPhase.STANCE_SHOWING,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            NatureComponents.NatureCard(
                                containerColor = Color.Black.copy(alpha = 0.7f),
                                shape = NatureShapes.medium,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "⚔️",
                                            fontSize = 20.sp
                                        )
                                        Text(
                                            text = "${opponentBattleCard.gameCard.attack}",
                                            style = NatureTypography.titleMedium,
                                            color = Color.White
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "🛡️",
                                            fontSize = 20.sp
                                        )
                                        Text(
                                            text = "${opponentBattleCard.gameCard.defense}",
                                            style = NatureTypography.titleMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = animationPhase >= BattleAnimationPhase.RESULT_SHOWING,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    NatureComponents.NatureCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = when {
                            isDraw -> Color(0xFF9E9E9E)  // 무승부 - 회색
                            isWin -> Color(0xFF4CAF50)   // 승리 - 초록
                            else -> Color(0xFFF44336)    // 패배 - 빨강
                        },
                        shape = NatureShapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val resultText = when {
                                isDraw -> "🤝 무승부 🤝"
                                isWin -> "🏆 승리! 🏆"
                                else -> "💔 패배 💔"
                            }

                            Text(
                                text = resultText,
                                style = NatureTypography.titleLarge.copy(
                                    fontSize = 24.sp
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            AnimatedVisibility(
                                visible = animationPhase >= BattleAnimationPhase.SCORE_SHOWING,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${if (scoreChange > 0) "+" else ""}$displayedScore 점",
                                            style = NatureTypography.titleMedium.copy(
                                                fontSize = 20.sp
                                            ),
                                            color = Color.White
                                        )

                                        // 카드 파괴 메시지 표시
                                        val destroyMessage = when {
                                            isMyCardDestroyed && isOpponentCardDestroyed -> "💥 양쪽 카드가 모두 파괴되었습니다"
                                            isMyCardDestroyed -> "💔 내 카드가 파괴되었습니다"
                                            isOpponentCardDestroyed -> "⚡ 상대 카드가 파괴되었습니다"
                                            else -> null
                                        }

                                        destroyMessage?.let { message ->
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = message,
                                                style = NatureTypography.bodyMedium.copy(
                                                    fontSize = 14.sp
                                                ),
                                                color = Color.White.copy(alpha = 0.9f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = animationPhase == BattleAnimationPhase.COMPLETED,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(24.dp))
                        NatureComponents.NatureButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = NatureColors.forestGreen,
                            contentColor = Color.White
                        ) {
                            Text(
                                text = "돌아가기",
                                style = NatureTypography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DestroyedCardEffect(modifier: Modifier = Modifier) {
    val particles = remember {
        List(8) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                emoji = listOf("💥", "💔", "❌", "🔥", "💀").random()
            )
        }
    }

    Box(modifier = modifier) {
        particles.forEach { particle ->
            val infiniteTransition = rememberInfiniteTransition(label = "destroyParticle")
            val animatedY = infiniteTransition.animateFloat(
                initialValue = particle.y,
                targetValue = particle.y + 0.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "destroyParticleY"
            )

            val animatedAlpha = infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "destroyParticleAlpha"
            )

            val animatedScale = infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "destroyParticleScale"
            )

            Text(
                text = particle.emoji,
                fontSize = 20.sp,
                modifier = Modifier
                    .offset(
                        x = (particle.x * 140).dp,
                        y = (animatedY.value * 200).dp
                    )
                    .alpha(animatedAlpha.value)
                    .scale(animatedScale.value)
            )
        }
    }
}

@Composable
fun ParticleEffect(modifier: Modifier = Modifier) {
    val particles = remember {
        List(12) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                emoji = listOf("✨", "⭐", "🌟", "💫", "🎉", "🎊").random()
            )
        }
    }

    Box(modifier = modifier) {
        particles.forEach { particle ->
            val infiniteTransition = rememberInfiniteTransition(label = "particle")
            val animatedY = infiniteTransition.animateFloat(
                initialValue = particle.y,
                targetValue = particle.y - 0.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "particleY"
            )

            val animatedAlpha = infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "particleAlpha"
            )

            Text(
                text = particle.emoji,
                fontSize = 24.sp,
                modifier = Modifier
                    .offset(
                        x = (particle.x * 140).dp,
                        y = (animatedY.value * 200).dp
                    )
                    .alpha(animatedAlpha.value)
            )
        }
    }
}

data class Particle(
    val x: Float,
    val y: Float,
    val emoji: String
)

// 게임 규칙에 따른 점수 계산 함수
private fun calculateScore(
    myStance: BattleStance,
    opponentStance: BattleStance,
    isMyWin: Boolean,
    isDraw: Boolean
): Int {
    return when {
        // 방 vs 방 특수 조건
        myStance == BattleStance.DEFENSE && opponentStance == BattleStance.DEFENSE -> 50
        // 공격 선택 시
        myStance == BattleStance.ATTACK -> when {
            isMyWin -> 100
            isDraw -> 100  // 비김 = 카드제거 + 100점
            else -> 0      // 짐 = 카드제거
        }
        // 방어 선택 시
        myStance == BattleStance.DEFENSE -> when {
            isMyWin -> 50
            else -> 0      // 비김/짐 = pass
        }

        else -> 0
    }
}

// 카드 파괴 여부 계산 함수
private fun isCardDestroyed(
    myStance: BattleStance,
    opponentStance: BattleStance,
    isMyWin: Boolean,
    isDraw: Boolean
): Boolean {
    return when {
        // 방 vs 방 = 양쪽 카드 제거 (무승부 특수 케이스)
        myStance == BattleStance.DEFENSE && opponentStance == BattleStance.DEFENSE -> true
        // 공격 선택 시 비김/짐 = 카드 제거
        myStance == BattleStance.ATTACK && !isMyWin -> true
        else -> false
    }
}