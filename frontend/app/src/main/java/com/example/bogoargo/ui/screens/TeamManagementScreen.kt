package com.example.bogoargo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.data.model.Team
import com.example.bogoargo.ui.viewmodels.TeamViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(
    navController: NavController,
    classId: String = "",
    viewModel: TeamViewModel = viewModel()
) {
    val teams by viewModel.teams.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(classId) {
        viewModel.loadTeamsByClassId(classId)
    }

    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            println("Error: $errorMessage")
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "팀 관리",
                emoji = "👥",
                onNavigationClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("teamCreate/$classId")
                },
                containerColor = NatureColors.leafGreen,
                shape = NatureShapes.large
            ) {
                Icon(Icons.Default.Add, contentDescription = "팀 추가", tint = androidx.compose.ui.graphics.Color.White)
            }
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
            ) {
                // Statistics Card
                NatureComponents.StatsCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "팀 현황",
                    emoji = "👥"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        NatureComponents.StatItem(
                            label = "전체",
                            value = teams.size.toString(),
                            emoji = "🌿",
                            color = NatureColors.forestGreen
                        )
                        NatureComponents.StatItem(
                            label = "활성",
                            value = teams.count { it.currentMembers < it.maxMembers }.toString(),
                            emoji = "🌱",
                            color = NatureColors.leafGreen
                        )
                        NatureComponents.StatItem(
                            label = "완성",
                            value = teams.count { it.currentMembers >= it.maxMembers }.toString(),
                            emoji = "🌳",
                            color = NatureColors.earthBrown
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                NatureComponents.SectionHeader(
                    text = "팀 목록",
                    emoji = "👥"
                )

                if (isLoading) {
                    NatureComponents.NatureLoadingIndicator()
                } else if (teams.isEmpty()) {
                    NatureComponents.EmptyStateCard(
                        emoji = "👥",
                        title = "등록된 팀이 없어요",
                        description = "새로운 팀을 만들어\n친구들과 함께 모험을 떠나보세요!"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                    ) {
                        items(teams) { team ->
                            TeamCard(
                                team = team,
                                onClick = {
                                    navController.navigate("teamDetail/${team.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamCard(
    team: Team,
    onClick: () -> Unit
) {
    NatureComponents.NatureCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = NatureShapes.card
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Team Icon
            NatureComponents.ProfileAvatar(
                emoji = "👥",
                backgroundColor = if (team.currentMembers < team.maxMembers) NatureColors.leafGreen else NatureColors.earthBrown,
                size = 60.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Team Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = team.name,
                    style = NatureTypography.titleMedium
                )

                if (team.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = team.description,
                        style = NatureTypography.bodySmall.copy(
                            color = NatureColors.earthBrown.copy(alpha = 0.8f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NatureComponents.StatusBadge(
                        text = "👥 ${team.currentMembers}/${team.maxMembers}",
                        backgroundColor = NatureColors.sunnyYellow.copy(alpha = 0.2f),
                        textColor = NatureColors.earthBrown
                    )

                    NatureComponents.StatusBadge(
                        text = if (team.currentMembers < team.maxMembers) "🟢 모집중" else "🔴 모집완료",
                        backgroundColor = if (team.currentMembers < team.maxMembers)
                            NatureColors.leafGreen.copy(alpha = 0.2f)
                        else
                            NatureColors.earthBrown.copy(alpha = 0.2f),
                        textColor = if (team.currentMembers < team.maxMembers)
                            NatureColors.leafGreen
                        else
                            NatureColors.earthBrown
                    )

                    NatureComponents.StatusBadge(
                        text = "📅 ${team.createdAt}",
                        backgroundColor = NatureColors.softOrange.copy(alpha = 0.2f),
                        textColor = NatureColors.earthBrown
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeamManagementScreenPreview() {
    MaterialTheme {
        TeamManagementScreen(navController = rememberNavController())
    }
}
