package com.example.bogoargo.ui.screens.classRoom

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bogoargo.data.dto.response.LocationResponseDto
import com.example.bogoargo.ui.viewmodels.classRoom.ClassCreateViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassCreateScreen(
    navController: NavController,
    viewModel: ClassCreateViewModel = hiltViewModel()
) {
    var className by remember { mutableStateOf("") }
    var maxStudents by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf("") }
    var showLocationDropdown by remember { mutableStateOf(false) }
    var selectedGrade by remember { mutableStateOf("") }
    var showGradeDropdown by remember { mutableStateOf(false) }
    var activityDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
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
                    label = { Text("반 이름 (예: 경복궁 탐사 반)", style = NatureTypography.bodyMedium) },
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

                // Location Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedLocation,
                        onValueChange = { },
                        label = { Text("활동 장소", style = NatureTypography.bodyMedium) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !uiState.isLoading) { 
                                showLocationDropdown = !showLocationDropdown 
                            },
                        singleLine = true,
                        readOnly = true,
                        shape = NatureShapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NatureColors.forestGreen,
                            focusedLabelColor = NatureColors.forestGreen,
                            unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                        ),
                        enabled = !uiState.isLoading,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "위치 선택",
                                tint = NatureColors.forestGreen,
                                modifier = Modifier.clickable(enabled = !uiState.isLoading) { 
                                    showLocationDropdown = !showLocationDropdown 
                                }
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = showLocationDropdown,
                        onDismissRequest = { showLocationDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        if (uiState.isLoadingLocations) {
                            DropdownMenuItem(
                                text = {
                                    Row {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .padding(end = 8.dp)
                                                .size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Text("로딩 중...")
                                    }
                                },
                                onClick = { }
                            )
                        } else {
                            uiState.locations.forEach { location ->
                                DropdownMenuItem(
                                    text = { Text(location.name) },
                                    onClick = {
                                        selectedLocation = location.name
                                        showLocationDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Grade Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedGrade,
                        onValueChange = { },
                        label = { Text("학년 (1-9)", style = NatureTypography.bodyMedium) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !uiState.isLoading) { 
                                showGradeDropdown = !showGradeDropdown 
                            },
                        singleLine = true,
                        readOnly = true,
                        shape = NatureShapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NatureColors.forestGreen,
                            focusedLabelColor = NatureColors.forestGreen,
                            unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                        ),
                        enabled = !uiState.isLoading,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "학년 선택",
                                tint = NatureColors.forestGreen,
                                modifier = Modifier.clickable(enabled = !uiState.isLoading) { 
                                    showGradeDropdown = !showGradeDropdown 
                                }
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = showGradeDropdown,
                        onDismissRequest = { showGradeDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        (1..9).forEach { grade ->
                            DropdownMenuItem(
                                text = { Text("${grade}학년") },
                                onClick = {
                                    selectedGrade = "${grade}학년"
                                    showGradeDropdown = false
                                }
                            )
                        }
                    }
                }

                // TODO : 입력창의 윗부분을 눌러야 작동됨 > 수정 필요
                OutlinedTextField(
                    value = activityDate,
                    onValueChange = { },
                    label = { Text("활동 날짜", style = NatureTypography.bodyMedium) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    singleLine = true,
                    readOnly = true,
                    shape = NatureShapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NatureColors.forestGreen,
                        focusedLabelColor = NatureColors.forestGreen,
                        unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                    ),
                    enabled = !uiState.isLoading,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "날짜 선택",
                            tint = NatureColors.forestGreen
                        )
                    }
                )


                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState()
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val date = java.time.Instant.ofEpochMilli(millis)
                                            .atZone(java.time.ZoneId.systemDefault())
                                            .toLocalDate()
                                        activityDate = date.toString()
                                    }
                                    showDatePicker = false
                                }
                            ) {
                                Text("확인", color = NatureColors.forestGreen)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDatePicker = false }
                            ) {
                                Text("취소", color = NatureColors.earthBrown)
                            }
                        }
                    ) {
                        DatePicker(
                            state = datePickerState,
                            colors = androidx.compose.material3.DatePickerDefaults.colors(
                                selectedDayContainerColor = NatureColors.forestGreen,
                                todayDateBorderColor = NatureColors.forestGreen
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.isLoading) {
                    NatureComponents.NatureLoadingIndicator()
                } else {
                    NatureComponents.NatureButton(
                        onClick = {
                            val maxStudentsInt = maxStudents.toIntOrNull()
                            val gradeInt = selectedGrade.replace("학년", "").toIntOrNull()
                            if (className.trim().isNotEmpty() &&
                                maxStudentsInt != null &&
                                maxStudentsInt > 0 &&
                                selectedLocation.trim().isNotEmpty() &&
                                selectedGrade.trim().isNotEmpty() &&
                                gradeInt != null &&
                                activityDate.isNotBlank()) {
                                viewModel.createClass(
                                    className = className,
                                    description = description,
                                    location = selectedLocation,
                                    activityDate = activityDate,
                                    maxStudents = maxStudentsInt,
                                    grade = gradeInt
                                )
                            }
                        },
                        text = "🏫 반 생성 완료",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = className.isNotBlank() && 
                                 maxStudents.isNotBlank() &&
                                selectedLocation.isNotBlank() &&
                                selectedGrade.isNotBlank() &&
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