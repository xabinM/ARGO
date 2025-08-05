package com.example.bogoargo.ui.screens.team

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.ui.viewmodels.team.TeamCreateViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCreateScreen(
    navController: NavController,
    classId: String = "",
    viewModel: TeamCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "팀 생성",
                emoji = "🏆",
                onNavigationClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.errorMessage != null) {
                    NatureComponents.NatureCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = NatureColors.softOrange.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "⚠️ ${uiState.errorMessage}",
                            modifier = Modifier.padding(16.dp),
                            style = NatureTypography.bodyMedium.copy(color = NatureColors.earthBrown)
                        )
                    }
                }
                
                NatureComponents.NatureCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "🏆 새로운 팀 만들기",
                            style = NatureTypography.titleMedium
                        )
                        
                        Text(
                            text = "이 반에 새로운 팀을 만들어보세요.",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.earthBrown.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = NatureColors.forestGreen
                        )
                    }
                } else {
                    NatureComponents.NatureButton(
                        onClick = {
                            val classIdLong = classId.toLongOrNull() ?: 0L
                            viewModel.createTeam(classIdLong)
                        },
                        text = "🏆 팀 생성하기",
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = NatureColors.forestGreen,
                        enabled = classId.isNotEmpty()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeamCreateScreenPreview() {
    MaterialTheme {
        TeamCreateScreen(navController = rememberNavController())
    }
}