package com.example.bogoargo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.ui.viewmodels.ProgramViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramCreateScreen(
    navController: NavController,
    classId: String = "",
    viewModel: ProgramViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var maxParticipants by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // 지도에서 선택된 위치 정보 받기
    LaunchedEffect(navController.currentBackStackEntry) {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<com.google.android.gms.maps.model.LatLng>("selectedLocation")?.observeForever { selectedLatLng ->
            selectedLatLng?.let {
                latitude = it.latitude
                longitude = it.longitude
            }
        }
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("selectedAddress")?.observeForever { address ->
            address?.let {
                if (it.isNotEmpty()) {
                    location = it
                }
            }
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ProgramViewModel.UiState.Success -> {
                navController.popBackStack()
            }
            is ProgramViewModel.UiState.Loading -> {
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
                title = "체험 학습 만들기",
                emoji = "🌱",
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
                if (errorMessage.isNotEmpty()) {
                    NatureComponents.NatureCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = NatureColors.softOrange.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "⚠️ $errorMessage",
                            modifier = Modifier.padding(16.dp),
                            style = NatureTypography.bodyMedium.copy(color = NatureColors.earthBrown)
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("체험 학습 제목", style = NatureTypography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = NatureShapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NatureColors.forestGreen,
                        focusedLabelColor = NatureColors.forestGreen,
                        unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("체험 학습 설명", style = NatureTypography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    shape = NatureShapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NatureColors.forestGreen,
                        focusedLabelColor = NatureColors.forestGreen,
                        unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                    )
                )

                // 장소 선택 (지도에서만)
                OutlinedTextField(
                    value = location,
                    onValueChange = { },
                    label = { Text("체험 장소", style = NatureTypography.bodyMedium) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("programSpotCreate/$classId") },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.LocationOn, 
                            contentDescription = null,
                            tint = NatureColors.forestGreen
                        ) 
                    },
                    singleLine = true,
                    readOnly = true,
                    shape = NatureShapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NatureColors.forestGreen,
                        focusedLabelColor = NatureColors.forestGreen,
                        unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                    ),
                    placeholder = {
                        Text(
                            "지도에서 장소를 선택해주세요",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.earthBrown.copy(alpha = 0.6f)
                            )
                        )
                    },
                    trailingIcon = {
                        if (latitude != null && longitude != null) {
                            Icon(
                                Icons.Default.Map,
                                contentDescription = "위치 선택됨",
                                tint = NatureColors.leafGreen
                            )
                        }
                    }
                )

                // 시작 날짜
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { },
                    label = { Text("시작 날짜", style = NatureTypography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { 
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(
                                Icons.Default.DateRange, 
                                contentDescription = "시작 날짜 선택",
                                tint = NatureColors.forestGreen
                            )
                        }
                    },
                    singleLine = true,
                    readOnly = true,
                    shape = NatureShapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NatureColors.forestGreen,
                        focusedLabelColor = NatureColors.forestGreen,
                        unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                    ),
                    placeholder = {
                        Text(
                            "시작 날짜를 선택해주세요",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.earthBrown.copy(alpha = 0.6f)
                            )
                        )
                    },
                    trailingIcon = {
                        TextButton(onClick = { showStartDatePicker = true }) {
                            Text(
                                "선택",
                                style = NatureTypography.bodyMedium.copy(
                                    color = NatureColors.forestGreen
                                )
                            )
                        }
                    }
                )

                // 완료 날짜
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { },
                    label = { Text("완료 날짜", style = NatureTypography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { 
                        IconButton(onClick = { showEndDatePicker = true }) {
                            Icon(
                                Icons.Default.DateRange, 
                                contentDescription = "완료 날짜 선택",
                                tint = NatureColors.forestGreen
                            )
                        }
                    },
                    singleLine = true,
                    readOnly = true,
                    shape = NatureShapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NatureColors.forestGreen,
                        focusedLabelColor = NatureColors.forestGreen,
                        unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                    ),
                    placeholder = {
                        Text(
                            "완료 날짜를 선택해주세요",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.earthBrown.copy(alpha = 0.6f)
                            )
                        )
                    },
                    trailingIcon = {
                        TextButton(onClick = { showEndDatePicker = true }) {
                            Text(
                                "선택",
                                style = NatureTypography.bodyMedium.copy(
                                    color = NatureColors.forestGreen
                                )
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = maxParticipants,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                            maxParticipants = newValue
                        }
                    },
                    label = { Text("최대 참가자 수", style = NatureTypography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = NatureShapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NatureColors.forestGreen,
                        focusedLabelColor = NatureColors.forestGreen,
                        unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                    ),
                    placeholder = {
                        Text(
                            "예: 30",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.earthBrown.copy(alpha = 0.6f)
                            )
                        )
                    }
                )

            Spacer(modifier = Modifier.height(16.dp))

            NatureComponents.NatureButton(
                onClick = {
                    errorMessage = ""
                    
                    when {
                        title.isBlank() -> errorMessage = "체험 학습 제목을 입력해주세요."
                        description.isBlank() -> errorMessage = "체험 학습 설명을 입력해주세요."
                        location.isBlank() -> errorMessage = "지도에서 장소를 선택해주세요."
                        startDate.isBlank() -> errorMessage = "시작 날짜를 선택해주세요."
                        endDate.isBlank() -> errorMessage = "완료 날짜를 선택해주세요."
                        maxParticipants.isBlank() -> errorMessage = "최대 참가자 수를 입력해주세요."
                        else -> {
                            viewModel.createProgram(
                                classId = classId,
                                title = title.trim(),
                                description = description.trim(),
                                location = location.trim(),
                                date = startDate.trim(),
                                startTime = startDate.trim(),
                                endTime = endDate.trim(),
                                maxParticipants = maxParticipants.toIntOrNull() ?: 0
                            )
                        }
                    }
                },
                text = if (isLoading) "만드는 중..." else "🌱 체험 학습 만들기",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                backgroundColor = NatureColors.leafGreen
            )
            }
        }

        // Start Date Picker Dialog
        if (showStartDatePicker) {
            val startDatePickerState = rememberDatePickerState()
            
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    NatureComponents.NatureButton(
                        onClick = {
                            startDatePickerState.selectedDateMillis?.let { millis ->
                                startDate = dateFormatter.format(Date(millis))
                            }
                            showStartDatePicker = false
                        },
                        text = "선택",
                        backgroundColor = NatureColors.leafGreen
                    )
                },
                dismissButton = {
                    NatureComponents.NatureOutlinedButton(
                        onClick = { showStartDatePicker = false },
                        text = "취소"
                    )
                },
                colors = DatePickerDefaults.colors(
                    containerColor = NatureColors.whiteTransparent
                )
            ) {
                DatePicker(
                    state = startDatePickerState,
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = NatureColors.leafGreen,
                        todayContentColor = NatureColors.forestGreen,
                        todayDateBorderColor = NatureColors.forestGreen
                    )
                )
            }
        }

        // End Date Picker Dialog
        if (showEndDatePicker) {
            val endDatePickerState = rememberDatePickerState()
            
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    NatureComponents.NatureButton(
                        onClick = {
                            endDatePickerState.selectedDateMillis?.let { millis ->
                                endDate = dateFormatter.format(Date(millis))
                            }
                            showEndDatePicker = false
                        },
                        text = "선택",
                        backgroundColor = NatureColors.leafGreen
                    )
                },
                dismissButton = {
                    NatureComponents.NatureOutlinedButton(
                        onClick = { showEndDatePicker = false },
                        text = "취소"
                    )
                },
                colors = DatePickerDefaults.colors(
                    containerColor = NatureColors.whiteTransparent
                )
            ) {
                DatePicker(
                    state = endDatePickerState,
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = NatureColors.leafGreen,
                        todayContentColor = NatureColors.forestGreen,
                        todayDateBorderColor = NatureColors.forestGreen
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgramCreateScreenPreview() {
    MaterialTheme {
        ProgramCreateScreen(navController = rememberNavController())
    }
}