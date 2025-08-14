package com.example.bogoargo.ui.screens.team

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.ui.viewmodels.team.TeamCreateViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCreateScreen(
    navController: NavController,
    classId: Long,
    viewModel: TeamCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var teamName by remember { mutableStateOf("") }
    var maxMembers by remember { mutableStateOf("4") }
    var teamNameError by remember { mutableStateOf<String?>(null) }
    var maxMembersError by remember { mutableStateOf<String?>(null) }
    
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
                            text = "팀 이름과 최대 인원을 입력해주세요.",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.earthBrown.copy(alpha = 0.8f)
                            )
                        )
                        
                        // 팀 이름 입력
                        Column {
                            Text(
                                text = "팀 이름",
                                style = NatureTypography.bodyMedium.copy(
                                    color = NatureColors.earthBrown
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = teamName,
                                onValueChange = { 
                                    teamName = it
                                    teamNameError = null
                                },
                                placeholder = { 
                                    Text(
                                        "예: 자연탐험대",
                                        style = NatureTypography.bodySmall.copy(
                                            color = NatureColors.earthBrown.copy(alpha = 0.6f)
                                        )
                                    ) 
                                },
                                isError = teamNameError != null,
                                enabled = !uiState.isLoading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NatureColors.forestGreen,
                                    unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.3f),
                                    errorBorderColor = NatureColors.softOrange
                                ),
                                shape = NatureShapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (teamNameError != null) {
                                Text(
                                    text = teamNameError!!,
                                    color = NatureColors.softOrange,
                                    style = NatureTypography.bodySmall,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }
                        }

                        // 최대 인원 입력
                        Column {
                            Text(
                                text = "최대 인원",
                                style = NatureTypography.bodyMedium.copy(
                                    color = NatureColors.earthBrown
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = maxMembers,
                                onValueChange = { 
                                    maxMembers = it.filter { char -> char.isDigit() }
                                    maxMembersError = null
                                },
                                placeholder = { 
                                    Text(
                                        "4",
                                        style = NatureTypography.bodySmall.copy(
                                            color = NatureColors.earthBrown.copy(alpha = 0.6f)
                                        )
                                    ) 
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = maxMembersError != null,
                                enabled = !uiState.isLoading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NatureColors.forestGreen,
                                    unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.3f),
                                    errorBorderColor = NatureColors.softOrange
                                ),
                                shape = NatureShapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (maxMembersError != null) {
                                Text(
                                    text = maxMembersError!!,
                                    color = NatureColors.softOrange,
                                    style = NatureTypography.bodySmall,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }
                        }
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
                            // 유효성 검사
                            teamNameError = when {
                                teamName.isBlank() -> "팀 이름을 입력해주세요"
                                teamName.length > 20 -> "팀 이름은 20자 이내로 입력해주세요"
                                else -> null
                            }
                            
                            val maxMembersInt = maxMembers.toIntOrNull()
                            maxMembersError = when {
                                maxMembers.isBlank() -> "최대 인원을 입력해주세요"
                                maxMembersInt == null -> "올바른 숫자를 입력해주세요"
                                maxMembersInt < 1 -> "최대 인원은 1명 이상이어야 합니다"
                                maxMembersInt > 50 -> "최대 인원은 50명 이하로 설정해주세요"
                                else -> null
                            }
                            
                            if (teamNameError == null && maxMembersError == null) {
                                viewModel.createTeam(classId, teamName, maxMembersInt!!)
                            }
                        },
                        text = "🏆 팀 생성하기",
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = NatureColors.forestGreen,
                        enabled = true
                    )
                }
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun TeamCreateScreenPreview() {
//    MaterialTheme {
//        TeamCreateScreen(navController = rememberNavController())
//    }
//}