package com.example.bogoargo.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bogoargo.ui.viewmodels.ClassViewModel
import kotlin.random.Random


object InvitationCodeGenerator {
    private val CHAR_POOL: CharArray = (('A'..'Z').toList() + ('2'..'9').toList())
        .filter {
            it != 'O' && it != 'I' && it != '0' && it != '1'
        }
        .toCharArray()

    fun generateSimpleCode(length: Int = 8): String {
        return (1..length)
            .map { Random.nextInt(0, CHAR_POOL.size) }
            .map(CHAR_POOL::get)
            .joinToString("")
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassCreateScreen(
    navController: NavController,
    viewModel: ClassViewModel = viewModel()
) {
    var schoolName by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var maxStudents by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState) {
        when (uiState) {
            is ClassViewModel.UiState.Success -> {
                navController.popBackStack()
            }
            is ClassViewModel.UiState.Loading -> {
                isLoading = true
            }
            else -> {
                isLoading = false
            }
        }
    }
    

    val regions = remember {
        listOf(
            "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "경기",
            "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("새로운 반 생성", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp),
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
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            OutlinedTextField(
                value = schoolName,
                onValueChange = { schoolName = it },
                label = { Text("학교명") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = className,
                onValueChange = { className = it },
                label = { Text("반 이름 (예: 1학년 1반)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = maxStudents,
                onValueChange = { newValue ->
                    val filteredValue = newValue.filter { it.isDigit() }
                    if (filteredValue.length <= 3) {
                        maxStudents = filteredValue
                    }
                },
                label = { Text("최대 인원 (1-999명)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                supportingText = {
                    Text("1명 이상 999명 이하로 입력해주세요")
                }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("설명 (선택 사항)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                maxLines = 5
            )

            Column {
                Button(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (region.isEmpty()) "지역 선택" else region,
                            color = if (region.isEmpty()) 
                                MaterialTheme.colorScheme.onSurfaceVariant 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "드롭다운"
                        )
                    }
                }

                if (expanded) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        LazyColumn {
                            items(regions) { selectionOption ->
                                TextButton(
                                    onClick = {
                                        region = selectionOption
                                        expanded = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = selectionOption,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start
                                    )
                                }
                                if (selectionOption != regions.last()) {
                                    HorizontalDivider(thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    errorMessage = ""
                    
                    when {
                        schoolName.isBlank() -> {
                            errorMessage = "학교명을 입력해주세요."
                            return@Button
                        }
                        className.isBlank() -> {
                            errorMessage = "반 이름을 입력해주세요."
                            return@Button
                        }
                        maxStudents.isBlank() -> {
                            errorMessage = "최대 인원을 입력해주세요."
                            return@Button
                        }
                        region.isBlank() -> {
                            errorMessage = "지역을 선택해주세요."
                            return@Button
                        }
                    }

                    val maxStudentsInt = maxStudents.toIntOrNull()
                    if (maxStudentsInt == null || maxStudentsInt <= 0 || maxStudentsInt > 999) {
                        errorMessage = "최대 인원은 1명 이상 999명 이하로 입력해주세요."
                        return@Button
                    }

                    viewModel.createClass(
                        schoolName = schoolName.trim(),
                        className = className.trim(),
                        maxStudents = maxStudentsInt,
                        description = description.trim(),
                        region = region
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("생성 중...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("반 생성 완료", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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