package com.example.bogoargo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramDetailScreen(
    navController: NavController,
    programId: String = ""
) {
    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "프로그램 상세",
                emoji = "🌿",
                onNavigationClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
            ) {
                NatureComponents.NatureCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🌿 프로그램 상세 정보",
                            style = NatureTypography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        NatureComponents.NatureCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = NatureColors.lightBeige
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "🏷️ 프로그램 ID",
                                    style = NatureTypography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = programId,
                                    style = NatureTypography.headlineSmall
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        NatureComponents.NatureCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = NatureColors.warmBeige.copy(alpha = 0.3f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🌱",
                                    fontSize = 32.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "자연 학습 프로그램",
                                    style = NatureTypography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "아이들과 함께 자연을 탐험하고\n재미있는 학습 경험을 만들어요!",
                                    style = NatureTypography.bodyMedium.copy(
                                        color = NatureColors.earthBrown.copy(alpha = 0.8f)
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "🚧 현재 개발 중이에요!",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.softOrange
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgramDetailScreenPreview() {
    MaterialTheme {
        ProgramDetailScreen(
            navController = rememberNavController(),
            programId = "program_1"
        )
    }
}