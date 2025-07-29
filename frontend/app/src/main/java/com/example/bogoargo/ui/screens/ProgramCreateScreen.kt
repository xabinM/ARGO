package com.example.bogoargo.ui.screens

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
    var date by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

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
            TopAppBar(
                title = { Text("프로그램 생성", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("프로그램 제목") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("프로그램 설명") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // 장소 입력 및 지도 버튼
            Column {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("장소") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            navController.navigate("programSpotCreate/$classId")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "지도에서 선택",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("지도에서 선택")
                    }
                    
                    if (latitude != null && longitude != null) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = "위치 선택됨",
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("날짜 (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                singleLine = true
            )

            OutlinedTextField(
                value = maxParticipants,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                        maxParticipants = newValue
                    }
                },
                label = { Text("최대 참가자 수") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    errorMessage = ""
                    
                    when {
                        title.isBlank() -> errorMessage = "프로그램 제목을 입력해주세요."
                        description.isBlank() -> errorMessage = "프로그램 설명을 입력해주세요."
                        location.isBlank() -> errorMessage = "장소를 입력해주세요."
                        date.isBlank() -> errorMessage = "날짜를 입력해주세요."
                        maxParticipants.isBlank() -> errorMessage = "최대 참가자 수를 입력해주세요."
                        else -> {
                            viewModel.createProgram(
                                classId = classId,
                                title = title.trim(),
                                description = description.trim(),
                                location = location.trim(),
                                date = date.trim(),
                                startTime = "",
                                endTime = "",
                                maxParticipants = maxParticipants.toIntOrNull() ?: 0
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("프로그램 생성", fontWeight = FontWeight.Bold)
                }
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