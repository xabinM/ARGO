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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.navigation.Screen
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation
import com.example.bogoargo.ui.viewmodels.cardgame.BattleRequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleRequestScreen(
    navController: NavController,
    teamId: Long,
    leaderId: Long,
    classId: Long,
    viewModel: BattleRequestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 화면 진입 시 자동 API 호출
    LaunchedEffect(teamId) {
        viewModel.loadBattleOpponents(teamId)
    }
    
    val availableTeams = uiState.battleOpponents

    var showBattleRequestModal by remember { mutableStateOf(false) }
    var selectedTeam by remember { mutableStateOf<BattleOpponent?>(null) }

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
            // 로딩 상태 처리
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NatureColors.forestGreen)
                }
                return@NatureBackground
            }
            
            // 오류 상태 처리
            if (uiState.errorMessage != null || availableTeams.isEmpty()) {
                ErrorStateCard(
                    errorMessage = uiState.errorMessage,
                    onRetry = { viewModel.loadBattleOpponents(teamId) },
                    onUseDummy = { viewModel.loadDummyBattleOpponents(teamId) },
                    modifier = Modifier.padding(paddingValues)
                )
                return@NatureBackground
            }
            
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
                                text = "같은 클래스에 있는 팀들과 대전을 신청할 수 있습니다. (총 ${availableTeams.size}팀)",
                                style = NatureTypography.bodyMedium,
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                items(availableTeams) { team ->
                    BattleOpponentItem(
                        opponent = team,
                        onClick = {
                            selectedTeam = team
                            showBattleRequestModal = true
                        }
                    )
                }
            }
        }

        if (showBattleRequestModal && selectedTeam != null) {
            BattleRequestModal(
                targetOpponent = selectedTeam!!,
                onDismiss = {
                    showBattleRequestModal = false
                    selectedTeam = null
                },
                onConfirm = { targetOpponent ->
                    showBattleRequestModal = false
                    selectedTeam = null
                    navController.navigate(
                        Screen.CardSelection.createRoute(
                            teamId = teamId,
                            targetTeamId = targetOpponent.teamId,
                            classId = classId,
                            matchId = null,
                            isResponse = false,
                            targetTeamName = targetOpponent.teamName
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun ErrorStateCard(
    errorMessage: String?,
    onRetry: () -> Unit,
    onUseDummy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NatureComponents.NatureCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = NatureShapes.large,
            containerColor = NatureColors.whiteTransparent,
            elevation = NatureElevation.medium
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚠️",
                    style = MaterialTheme.typography.displayMedium
                )
                
                Text(
                    text = "대전 상대 목록을 불러올 수 없습니다",
                    style = NatureTypography.titleLarge,
                    color = Color(0xFFF44336),
                    textAlign = TextAlign.Center
                )
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = NatureTypography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NatureComponents.NatureOutlinedButton(
                        onClick = onRetry,
                        text = "재시도",
                        modifier = Modifier.weight(1f)
                    )
                    NatureComponents.NatureButton(
                        onClick = onUseDummy,
                        text = "테스트 데이터",
                        modifier = Modifier.weight(1f),
                        backgroundColor = NatureColors.leafGreen
                    )
                }
            }
        }
    }
}

@Composable
fun BattleOpponentItem(
    opponent: BattleOpponent,
    onClick: () -> Unit
) {
    val winRatePercentage = (opponent.winRate * 100).toInt()
    
    NatureComponents.NatureCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = NatureShapes.medium,
        containerColor = NatureColors.whiteTransparent,
        elevation = NatureElevation.medium
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
                        text = opponent.teamName,
                        style = NatureTypography.titleMedium,
                        color = NatureColors.earthBrown
                    )
                    
                    NatureComponents.NatureCard(
                        containerColor = when {
                            winRatePercentage >= 70 -> Color(0xFFFF5722) // 강함 (빨강)
                            winRatePercentage >= 50 -> Color(0xFFFF9800) // 보통 (주황)
                            else -> Color(0xFF4CAF50) // 약함 (초록)
                        },
                        shape = NatureShapes.extraSmall
                    ) {
                        Text(
                            text = when {
                                winRatePercentage >= 70 -> "강팀"
                                winRatePercentage >= 50 -> "중급"
                                else -> "약팀"
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = NatureTypography.labelSmall,
                            color = Color.White
                        )
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
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = NatureColors.forestGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = opponent.leaderName,
                            style = NatureTypography.labelSmall,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${opponent.averageScore}점",
                            style = NatureTypography.labelSmall,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "승률 ${winRatePercentage}%",
                            style = NatureTypography.labelSmall,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Text(
                    text = "${opponent.wins}승 ${opponent.losses}패 ${opponent.draws}무",
                    style = NatureTypography.labelSmall,
                    color = Color.Gray.copy(alpha = 0.6f)
                )
            }

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

@Composable
fun BattleRequestModal(
    targetOpponent: BattleOpponent,
    onDismiss: () -> Unit,
    onConfirm: (BattleOpponent) -> Unit
) {
    val winRatePercentage = (targetOpponent.winRate * 100).toInt()
    
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
                    text = "${targetOpponent.teamName}에게 대전을 신청하시겠습니까?",
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
                                text = "팀 리더",
                                style = NatureTypography.bodyMedium
                            )
                            Text(
                                text = targetOpponent.leaderName,
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
                                text = "${targetOpponent.averageScore}점",
                                style = NatureTypography.bodyMedium
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "승률",
                                style = NatureTypography.bodyMedium
                            )
                            Text(
                                text = "${winRatePercentage}%",
                                style = NatureTypography.bodyMedium
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "대전 기록",
                                style = NatureTypography.bodyMedium
                            )
                            Text(
                                text = "${targetOpponent.wins}승 ${targetOpponent.losses}패 ${targetOpponent.draws}무",
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
                        text = "취소",
                        modifier = Modifier.weight(1f)
                    )
                    
                    NatureComponents.NatureButton(
                        onClick = { onConfirm(targetOpponent) },
                        text = "신청하기",
                        modifier = Modifier.weight(1f),
                        backgroundColor = NatureColors.leafGreen,
                        contentColor = Color.White
                    )
                }
            }
        }
    }
}