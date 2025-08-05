package com.example.bogoargo.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bogoargo.data.model.User
import com.example.bogoargo.data.model.UserRole

/**
 * 초등학생 친화적인 자연 테마 색상 팔레트
 * 갈색, 노란색, 초록색 등 자연스러운 색상으로 구성
 */
object NatureColors {
    val warmBeige = Color(0xFFF5E6D3)
    val forestGreen = Color(0xFF7CB342)
    val sunnyYellow = Color(0xFFFFD54F)
    val earthBrown = Color(0xFF8D6E63)
    val leafGreen = Color(0xFF66BB6A)
    val softOrange = Color(0xFFFFB74D)
    val lightBeige = Color(0xFFF8F3E8)
    val whiteTransparent = Color.White.copy(alpha = 0.95f)
    val whiteTransparent90 = Color.White.copy(alpha = 0.9f)
}

/**
 * 자연 테마 타이포그래피 스타일
 */
object NatureTypography {
    val titleLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = NatureColors.forestGreen
    )

    val titleMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = NatureColors.earthBrown
    )

    val titleSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = NatureColors.earthBrown
    )

    val headlineSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = NatureColors.forestGreen
    )

    val bodyLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = NatureColors.earthBrown
    )

    val bodyMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = NatureColors.earthBrown
    )

    val bodySmall = androidx.compose.ui.text.TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = NatureColors.forestGreen
    )

    val labelLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )

    val labelMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
    )

    val labelSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    )
}

/**
 * 자연 테마 모양 정의
 */
object NatureShapes {
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(12.dp)
    val large = RoundedCornerShape(16.dp)
    val extraLarge = RoundedCornerShape(20.dp)
    val card = RoundedCornerShape(18.dp)
    val button = RoundedCornerShape(12.dp)
    val statsCard = RoundedCornerShape(16.dp)
    val circle = CircleShape
}

/**
 * 자연 테마 고도 및 그림자 정의
 */
object NatureElevation {
    val small = 4.dp
    val medium = 6.dp
    val large = 8.dp
    val extraLarge = 12.dp
}

/**
 * 자연 테마 공통 컴포넌트들
 */
object NatureComponents {

    /**
     * 자연 테마의 TopAppBar
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NatureTopAppBar(
        title: String,
        emoji: String = "🌱",
        onNavigationClick: () -> Unit
    ) {
        TopAppBar(
            title = {
                Text(
                    "$emoji $title",
                    style = NatureTypography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = NatureColors.forestGreen
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = NatureColors.warmBeige
            )
        )
    }

    /**
     * 자연 테마의 배경 그라데이션
     */
    @Composable
    fun NatureBackground(
        content: @Composable BoxScope.() -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            NatureColors.warmBeige,
                            NatureColors.lightBeige
                        )
                    )
                ),
            content = content
        )
    }

    /**
     * 자연 테마의 기본 카드
     */
    @Composable
    fun NatureCard(
        modifier: Modifier = Modifier,
        elevation: androidx.compose.ui.unit.Dp = NatureElevation.large,
        shape: androidx.compose.foundation.shape.RoundedCornerShape = NatureShapes.card,
        containerColor: Color = NatureColors.whiteTransparent,
        content: @Composable ColumnScope.() -> Unit
    ) {
        Card(
            modifier = modifier,
            shape = shape,
            //elevation = CardDefaults.cardElevation(elevation),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            content = { Column(content = content) }
        )
    }

    /**
     * 통계 표시용 카드
     */
    @Composable
    fun StatsCard(
        modifier: Modifier = Modifier,
        title: String,
        emoji: String,
        content: @Composable ColumnScope.() -> Unit
    ) {
        NatureCard(
            modifier = modifier,
            elevation = NatureElevation.extraLarge,
            shape = NatureShapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "$emoji $title",
                    style = NatureTypography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                content()
            }
        }
    }

    /**
     * 개별 통계 아이템
     */
    @Composable
    fun StatItem(
        label: String,
        value: String,
        emoji: String,
        color: Color
    ) {
        Card(
            modifier = Modifier.size(90.dp),
            shape = NatureShapes.statsCard,
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.15f)
            ),
            elevation = CardDefaults.cardElevation(NatureElevation.small)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
//                Text(
//                    text = emoji,
//                    fontSize = 20.sp
//                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = color.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    /**
     * 로딩 인디케이터
     */
    @Composable
    fun NatureLoadingIndicator(
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = NatureColors.forestGreen,
                strokeWidth = 5.dp,
                modifier = Modifier.size(50.dp)
            )
        }
    }

    /**
     * 빈 상태 표시 카드
     */
    @Composable
    fun EmptyStateCard(
        emoji: String = "🌿",
        title: String,
        description: String,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            NatureCard(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(16.dp),
                shape = NatureShapes.extraLarge,
                containerColor = NatureColors.whiteTransparent90
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = emoji,
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NatureColors.earthBrown
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = NatureColors.earthBrown.copy(alpha = 0.8f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    /**
     * 자연 테마 버튼
     */
    @Composable
    fun NatureButton(
        onClick: () -> Unit,
        text: String,
        modifier: Modifier = Modifier,
        backgroundColor: Color = NatureColors.leafGreen,
        contentColor: Color = Color.White,
        enabled: Boolean = true
    ) {
        Button(
            onClick = onClick,
            modifier = modifier.height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = contentColor
            ),
            shape = NatureShapes.button,
            enabled = enabled
        ) {
            Text(
                text,
                style = NatureTypography.labelLarge
            )
        }
    }

    /**
     * 자연 테마 아웃라인 버튼
     */
    @Composable
    fun NatureOutlinedButton(
        onClick: () -> Unit,
        text: String,
        modifier: Modifier = Modifier,
        borderColor: Color = NatureColors.forestGreen,
        contentColor: Color = NatureColors.forestGreen
    ) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
            shape = NatureShapes.medium,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = contentColor
            )
        ) {
            Text(text, style = NatureTypography.bodyMedium.copy(color = contentColor))
        }
    }

    /**
     * 프로필 아바타 (다양한 색상)
     */
    @Composable
    fun ProfileAvatar(
        emoji: String,
        backgroundColor: Color,
        size: androidx.compose.ui.unit.Dp = 60.dp,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier.size(size),
            shape = NatureShapes.circle,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(NatureElevation.medium)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = (size.value * 0.4f).sp
                )
            }
        }
    }

    /**
     * 상태 배지 (팀 소속, 역할 등)
     */
    @Composable
    fun StatusBadge(
        text: String,
        backgroundColor: Color,
        textColor: Color,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            shape = NatureShapes.small,
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = NatureTypography.bodySmall.copy(color = textColor)
            )
        }
    }

    /**
     * 섹션 헤더
     */
    @Composable
    fun SectionHeader(
        text: String,
        emoji: String = "",
        modifier: Modifier = Modifier
    ) {
        Text(
            text = if (emoji.isNotEmpty()) "$emoji $text" else text,
            style = NatureTypography.titleMedium,
            modifier = modifier.padding(bottom = 12.dp)
        )
    }

    /**
     * 회원 정보 카드
     */
    @Composable
    fun MemberCard(
        member: User,
        forestGreen: Color = NatureColors.forestGreen,
        sunnyYellow: Color = NatureColors.sunnyYellow,
        leafGreen: Color = NatureColors.leafGreen,
        earthBrown: Color = NatureColors.earthBrown,
        softOrange: Color = NatureColors.softOrange
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 프로필 아이콘
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (member.role == UserRole.TEACHER)
                                earthBrown
                            else
                                when ((member.name.length) % 4) {
                                    0 -> forestGreen
                                    1 -> sunnyYellow
                                    2 -> leafGreen
                                    else -> softOrange
                                }
                        ),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (member.role == UserRole.TEACHER) "👩‍🏫" else "👦",
                                fontSize = 24.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // 사용자 정보
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // 이름 (강조)
                    Text(
                        text = member.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = earthBrown,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    /* 사용자 ID
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = forestGreen.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = "🆔 ${member.username}",
                            fontSize = 14.sp,
                            color = forestGreen,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
*/
                    Spacer(modifier = Modifier.height(6.dp))

                    // 역할
                    Card(
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (member.role) {
                                UserRole.TEACHER -> earthBrown.copy(alpha = 0.15f)
                                UserRole.STUDENT -> sunnyYellow.copy(alpha = 0.2f)
                            }
                        )
                    ) {
                        Text(
                            text = when (member.role) {
                                UserRole.TEACHER -> "📚 선생님"
                                UserRole.STUDENT -> "✏️ 학생"
                            },
                            fontSize = 12.sp,
                            color = when (member.role) {
                                UserRole.TEACHER -> earthBrown
                                UserRole.STUDENT -> sunnyYellow.copy(alpha = 0.8f)
                            },
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // 소속 팀 (강조)
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (member.team != null)
                                leafGreen.copy(alpha = 0.2f)
                            else
                                Color.Gray.copy(alpha = 0.15f)
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (member.team != null) "🏆" else "⏳",
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (member.team != null) "팀 소속" else "대기중",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (member.team != null)
                                    leafGreen
                                else
                                    Color.Gray,
                                textAlign = TextAlign.Center
                            )

                            if (member.team != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Card(
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = leafGreen.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Text(
                                        text = member.team!!.name,
                                        fontSize = 10.sp,
                                        color = leafGreen,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

/**
 * 자연 테마 색상 팔레트 유틸리티
 */
object NatureColorUtils {
    /**
     * 사용자별로 다른 색상 반환 (해시 기반)
     */
    fun getUserColor(nickname: String, userId: Long): Color {
        val colors = listOf(
            NatureColors.forestGreen,
            NatureColors.sunnyYellow,
            NatureColors.leafGreen,
            NatureColors.softOrange
        )
        return colors[(nickname.length + userId.toString().length) % colors.size]
    }

    /**
     * 역할별 색상 반환
     */
    fun getRoleColor(isTeacher: Boolean): Color {
        return if (isTeacher) NatureColors.earthBrown else NatureColors.leafGreen
    }

    /**
     * 상태별 색상 반환
     */
    fun getStatusColor(isActive: Boolean): Color {
        return if (isActive) NatureColors.leafGreen else Color.Gray
    }
}