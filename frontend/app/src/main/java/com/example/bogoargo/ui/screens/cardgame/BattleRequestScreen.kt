package com.example.bogoargo.ui.screens.cardgame

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.navigation.Screen
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleRequestScreen(
    navController: NavController,
    teamId: Long,
    leaderId: Long
) {
    val availableTeams = remember {
        listOf(
            BattleTeam(
                teamId = 2,
                teamName = "불사조 팀 🔥",
                memberCount = 4,
                averageScore = 2100,
                isAvailable = true
            ),
            BattleTeam(
                teamId = 3,
                teamName = "그리핀 팀 🦅",
                memberCount = 3,
                averageScore = 1850,
                isAvailable = true
            ),
            BattleTeam(
                teamId = 4,
                teamName = "유니콘 팀 🦄",
                memberCount = 5,
                averageScore = 2300,
                isAvailable = false
            ),
            BattleTeam(
                teamId = 5,
                teamName = "드래곤 팀 🐉",
                memberCount = 4,
                averageScore = 2450,
                isAvailable = true
            )
        )
    }

    var showBattleRequestModal by remember { mutableStateOf(false) }
    var selectedTeam by remember { mutableStateOf<BattleTeam?>(null) }

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "대전 상대 선택",
                emoji = "⚔️",
                onNavigationClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    NatureComponents.NatureCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = NatureShapes.large,
                        containerColor = NatureColors.whiteTransparent,
                        elevation = NatureElevation.small
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⚔️ 대전 상대를 선택하세요",
                                style = NatureTypography.titleLarge,
                                color = NatureColors.forestGreen
                            )
                            Text(
                                text = "현재 온라인인 팀들을 대상으로 대전을 신청할 수 있습니다.",
                                style = NatureTypography.bodyMedium,
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                items(availableTeams) { team ->
                    BattleTeamItem(
                        team = team,
                        onClick = {
                            if (team.isAvailable) {
                                selectedTeam = team
                                showBattleRequestModal = true
                            }
                        }
                    )
                }
            }
        }

        if (showBattleRequestModal && selectedTeam != null) {
            BattleRequestModal(
                targetTeam = selectedTeam!!,
                onDismiss = {
                    showBattleRequestModal = false
                    selectedTeam = null
                },
                onConfirm = { targetTeam ->
                    showBattleRequestModal = false
                    selectedTeam = null
                    navController.navigate(
                        Screen.CardSelection.createRoute(teamId, targetTeam.teamId)
                    )
                }
            )
        }
    }
}

@Composable
fun BattleTeamItem(
    team: BattleTeam,
    onClick: () -> Unit
) {
    NatureComponents.NatureCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = team.isAvailable) { onClick() },
        shape = NatureShapes.medium,
        containerColor = if (team.isAvailable) 
            NatureColors.whiteTransparent
        else 
            NatureColors.whiteTransparent.copy(alpha = 0.5f),
        elevation = if (team.isAvailable) NatureElevation.medium else NatureElevation.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = team.teamName,
                        style = NatureTypography.titleMedium,
                        color = if (team.isAvailable) 
                            NatureColors.earthBrown
                        else 
                            NatureColors.earthBrown.copy(alpha = 0.5f)
                    )
                    
                    if (!team.isAvailable) {
                        NatureComponents.NatureCard(
                            containerColor = Color.Gray,
                            shape = NatureShapes.extraSmall
                        ) {
                            Text(
                                text = "대전 중",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = NatureTypography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            tint = NatureColors.forestGreen.copy(
                                alpha = if (team.isAvailable) 1f else 0.5f
                            ),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${team.memberCount}명",
                            style = NatureTypography.labelSmall,
                            color = Color.Gray.copy(
                                alpha = if (team.isAvailable) 0.7f else 0.4f
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFF9800).copy(
                                alpha = if (team.isAvailable) 1f else 0.5f
                            ),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${team.averageScore}점",
                            style = NatureTypography.labelSmall,
                            color = Color.Gray.copy(
                                alpha = if (team.isAvailable) 0.7f else 0.4f
                            )
                        )
                    }
                }
            }

            if (team.isAvailable) {
                NatureComponents.NatureCard(
                    containerColor = NatureColors.leafGreen,
                    shape = NatureShapes.small
                ) {
                    Text(
                        text = "도전하기 ⚔️",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = NatureTypography.labelMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun BattleRequestModal(
    targetTeam: BattleTeam,
    onDismiss: () -> Unit,
    onConfirm: (BattleTeam) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        NatureComponents.NatureCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = NatureShapes.large,
            containerColor = NatureColors.whiteTransparent,
            elevation = NatureElevation.high
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
                    text = "대전 신청",
                    style = NatureTypography.titleLarge,
                    color = NatureColors.forestGreen
                )

                Text(
                    text = "${targetTeam.teamName}에게 대전을 신청하시겠습니까?",
                    style = NatureTypography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                NatureComponents.NatureCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = NatureColors.forestGreen.copy(alpha = 0.1f),
                    shape = NatureShapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "상대팀 정보",
                            style = NatureTypography.titleSmall,
                            color = NatureColors.forestGreen
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "팀원 수",
                                style = NatureTypography.bodyMedium
                            )
                            Text(
                                text = "${targetTeam.memberCount}명",
                                style = NatureTypography.bodyMedium
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "평균 점수",
                                style = NatureTypography.bodyMedium
                            )
                            Text(
                                text = "${targetTeam.averageScore}점",
                                style = NatureTypography.bodyMedium
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NatureComponents.NatureOutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("취소")
                    }
                    
                    NatureComponents.NatureButton(
                        onClick = { onConfirm(targetTeam) },
                        modifier = Modifier.weight(1f),
                        containerColor = NatureColors.leafGreen,
                        contentColor = Color.White
                    ) {
                        Text("신청하기")
                    }
                }
            }
        }
    }
}