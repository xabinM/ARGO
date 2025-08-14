package com.example.bogoargo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.ui.viewmodels.SettingsViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "설정",
                emoji = "⚙️",
                onNavigationClick = { /* 뒤로가기 비활성화 */ }
            )
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 사용자 프로필 섹션
                NatureComponents.NatureCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        NatureComponents.ProfileAvatar(
                            emoji = "👤",
                            backgroundColor = NatureColors.leafGreen,
                            size = 64.dp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "사용자 설정",
                            style = NatureTypography.titleLarge,
                            color = NatureColors.forestGreen,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "앱 환경을 관리해보세요",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.earthBrown.copy(alpha = 0.8f)
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // 앱 정보 섹션
                NatureComponents.NatureCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "📱 앱 정보",
                            style = NatureTypography.titleMedium,
                            color = NatureColors.forestGreen
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "버전 정보",
                                style = NatureTypography.bodyMedium,
                                color = NatureColors.earthBrown
                            )
                            Text(
                                text = uiState.version,
                                style = NatureTypography.bodyMedium.copy(
                                    color = NatureColors.forestGreen
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "앱 이름",
                                style = NatureTypography.bodyMedium,
                                color = NatureColors.earthBrown
                            )
                            Text(
                                text = "Argo 체험학습",
                                style = NatureTypography.bodyMedium.copy(
                                    color = NatureColors.forestGreen
                                )
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 로그아웃 섹션
                NatureComponents.NatureButton(
                    onClick = { viewModel.showLogoutDialog() },
                    text = "🚪 로그아웃",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    backgroundColor = NatureColors.softOrange
                )
            }
        }
    }
    
    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideLogoutDialog() },
            title = {
                Text(
                    text = "🚪 로그아웃",
                    style = NatureTypography.titleMedium,
                    color = NatureColors.forestGreen
                )
            },
            text = {
                Text(
                    text = "정말 로그아웃하시겠습니까?\n다시 로그인해야 앱을 사용할 수 있습니다.",
                    style = NatureTypography.bodyMedium,
                    color = NatureColors.earthBrown
                )
            },
            confirmButton = {
                NatureComponents.NatureButton(
                    onClick = { viewModel.clearAllSettings() },
                    text = "로그아웃",
                    backgroundColor = NatureColors.softOrange
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideLogoutDialog() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = NatureColors.earthBrown
                    )
                ) {
                    Text(
                        text = "취소",
                        style = NatureTypography.bodyMedium
                    )
                }
            },
            containerColor = NatureColors.whiteTransparent90,
            shape = NatureShapes.medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen(navController = rememberNavController())
    }
}