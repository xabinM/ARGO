package com.example.bogoargo.ui.screens.classRoom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bogoargo.ui.viewmodels.classRoom.ClassCreateViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassCreateScreen(
    navController: NavController,
    viewModel: ClassCreateViewModel = viewModel()
) {
    var className by remember { mutableStateOf("") }
    var maxStudents by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var activityDate by remember { mutableStateOf("2024-12-31") }
    
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "새로운 반 생성",
                emoji = "🏫",
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

                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("반 이름 (예: 1학년 1반)", style = NatureTypography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = NatureShapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NatureColors.forestGreen,
                        focusedLabelColor = NatureColors.forestGreen,
                        unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                    ),
                    enabled = !uiState.isLoading
                )

                OutlinedTextField(
                    value = maxStudents,
                    onValueChange = { newValue ->
                        val filteredValue = newValue.filter { it.isDigit() }
                        if (filteredValue.length <= 3) {
                            maxStudents = filteredValue
                        }
                    },
                    label = { Text("최대 인원 (1-999명)", style = NatureTypography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = NatureShapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NatureColors.forestGreen,
                        focusedLabelColor = NatureColors.forestGreen,
                        unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                    ),
                    supportingText = {
                        Text(
                            "1명 이상 999명 이하로 입력해주세요",
                            style = NatureTypography.bodySmall.copy(
                                color = NatureColors.earthBrown.copy(alpha = 0.7f)
                            )
                        )
                    },
                    enabled = !uiState.isLoading
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("설명 (선택 사항)", style = NatureTypography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    maxLines = 5,
                    shape = NatureShapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NatureColors.forestGreen,
                        focusedLabelColor = NatureColors.forestGreen,
                        unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                    ),
                    enabled = !uiState.isLoading
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("활동 장소", style = NatureTypography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = NatureShapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NatureColors.forestGreen,
                        focusedLabelColor = NatureColors.forestGreen,
                        unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                    ),
                    enabled = !uiState.isLoading
                )

                OutlinedTextField(
                    value = activityDate,
                    onValueChange = { activityDate = it },
                    label = { Text("활동 날짜 (YYYY-MM-DD)", style = NatureTypography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = NatureShapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NatureColors.forestGreen,
                        focusedLabelColor = NatureColors.forestGreen,
                        unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                    ),
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.isLoading) {
                    NatureComponents.NatureLoadingIndicator()
                } else {
                    NatureComponents.NatureButton(
                        onClick = {
                            val maxStudentsInt = maxStudents.toIntOrNull()
                            if (className.isNotBlank() && 
                                maxStudentsInt != null && 
                                maxStudentsInt > 0 && 
                                location.isNotBlank() && 
                                activityDate.isNotBlank()) {
                                viewModel.createClass(
                                    className = className,
                                    description = description,
                                    location = location,
                                    activityDate = activityDate,
                                    maxStudents = maxStudentsInt
                                )
                            }
                        },
                        text = "🏫 반 생성 완료",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = className.isNotBlank() && 
                                 maxStudents.isNotBlank() && 
                                 location.isNotBlank() && 
                                 activityDate.isNotBlank(),
                        backgroundColor = NatureColors.leafGreen
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun PreviewClassCreateScreen() {
    val navController = rememberNavController()
    ClassCreateScreen(navController = navController)
}