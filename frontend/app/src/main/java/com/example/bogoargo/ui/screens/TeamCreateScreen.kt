package com.example.bogoargo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.ui.viewmodels.TeamViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCreateScreen(
    navController: NavController,
    classId: String = "",
    viewModel: TeamViewModel = viewModel()
) {
    var teamName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var maxMembers by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is TeamViewModel.UiState.Success -> {
                navController.popBackStack()
            }
            is TeamViewModel.UiState.Loading -> {
                isLoading = true
            }
            else -> {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "팀 만들기",
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
                NatureComponents.NatureCard(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        NatureComponents.SectionHeader(
                            text = "새로운 팀 만들기",
                            emoji = "🌟"
                        )
                        
                        OutlinedTextField(
                            value = teamName,
                            onValueChange = { teamName = it },
                            label = { Text("🏆 팀 이름", style = NatureTypography.bodyMedium) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.People, contentDescription = null, tint = NatureColors.forestGreen) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NatureColors.forestGreen,
                                focusedLabelColor = NatureColors.forestGreen
                            ),
                            shape = NatureShapes.medium
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("📝 팀 설명 (선택사항)", style = NatureTypography.bodyMedium) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NatureColors.forestGreen,
                                focusedLabelColor = NatureColors.forestGreen
                            ),
                            shape = NatureShapes.medium
                        )

                        OutlinedTextField(
                            value = maxMembers,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                                    maxMembers = newValue
                                }
                            },
                            label = { Text("👥 최대 인원 (1-99명)", style = NatureTypography.bodyMedium) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            supportingText = {
                                Text("🌱 팀의 최대 인원을 설정해주세요", style = NatureTypography.bodySmall)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NatureColors.forestGreen,
                                focusedLabelColor = NatureColors.forestGreen
                            ),
                            shape = NatureShapes.medium
                        )

                        if (errorMessage.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = NatureColors.softOrange.copy(alpha = 0.1f)
                                ),
                                shape = NatureShapes.medium
                            ) {
                                Text(
                                    text = "⚠️ $errorMessage",
                                    style = NatureTypography.bodyMedium.copy(color = NatureColors.earthBrown),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        NatureComponents.NatureButton(
                            onClick = {
                                errorMessage = ""
                                
                                when {
                                    teamName.isBlank() -> errorMessage = "팀 이름을 입력해주세요."
                                    maxMembers.isBlank() -> errorMessage = "최대 인원을 입력해주세요."
                                    maxMembers.toIntOrNull()?.let { it < 1 || it > 99 } == true -> 
                                        errorMessage = "최대 인원은 1명 이상 99명 이하로 입력해주세요."
                                    else -> {
                                        viewModel.createTeam(
                                            classId = classId,
                                            name = teamName.trim(),
                                            description = description.trim(),
                                            maxMembers = maxMembers.toIntOrNull() ?: 1,
                                            color = "#66BB6A"
                                        )
                                    }
                                }
                            },
                            text = if (isLoading) "🌱 생성 중..." else "🏆 팀 만들기",
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = NatureColors.leafGreen,
                            enabled = !isLoading
                        )
                    }
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