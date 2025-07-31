package com.example.bogoargo.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bogoargo.ui.viewmodels.ClassViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import kotlin.random.Random

// 장소 데이터 클래스
data class Location(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String = ""
)

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
    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    // 샘플 장소 데이터 (실제로는 DB에서 조회)
    val availableLocations = remember {
        listOf(
            Location("1", "서울대공원", 37.4276, 127.0111, "경기도 과천시 막계동"),
            Location("2", "롯데월드", 37.5117, 127.0980, "서울특별시 송파구 잠실동"),
            Location("3", "에버랜드", 37.2946, 127.2014, "경기도 용인시 처인구 포곡읍"),
            Location("4", "국립과천과학관", 37.4340, 127.0177, "경기도 과천시 상하벌동"),
            Location("5", "한강시민공원", 37.5326, 126.9540, "서울특별시 영등포구 여의도동"),
            Location("6", "남산타워", 37.5512, 126.9882, "서울특별시 중구 용산동2가"),
            Location("7", "경복궁", 37.5796, 126.9770, "서울특별시 종로구 사직로"),
            Location("8", "창덕궁", 37.5796, 126.9910, "서울특별시 종로구 율곡로")
        )
    }
    
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
                    value = schoolName,
                    onValueChange = { schoolName = it },
                    label = { Text("학교명", style = NatureTypography.bodyMedium) },
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
                    )
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
                    }
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
                    )
                )

                Column {
                    Button(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NatureColors.earthBrown
                        ),
                        border = BorderStroke(1.dp, NatureColors.earthBrown.copy(alpha = 0.5f)),
                        shape = NatureShapes.medium
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "장소",
                                    tint = NatureColors.forestGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedLocation?.name ?: "장소 선택",
                                    style = NatureTypography.bodyMedium.copy(
                                        color = if (selectedLocation == null) 
                                            NatureColors.earthBrown.copy(alpha = 0.6f)
                                        else 
                                            NatureColors.earthBrown
                                    )
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "드롭다운",
                                tint = NatureColors.earthBrown
                            )
                        }
                    }

                    if (expanded) {
                        NatureComponents.NatureCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            elevation = 8.dp,
                            containerColor = NatureColors.whiteTransparent
                        ) {
                            LazyColumn {
                                items(availableLocations) { location ->
                                    TextButton(
                                        onClick = {
                                            selectedLocation = location
                                            expanded = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = NatureColors.forestGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.Start
                                            ) {
                                                Text(
                                                    text = location.name,
                                                    style = NatureTypography.bodyMedium.copy(
                                                        color = NatureColors.earthBrown
                                                    )
                                                )
                                                Text(
                                                    text = location.address,
                                                    style = NatureTypography.bodySmall.copy(
                                                        color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    if (location != availableLocations.last()) {
                                        HorizontalDivider(
                                            thickness = 1.dp,
                                            color = NatureColors.earthBrown.copy(alpha = 0.2f)
                                        )
                                    }
                                }
                            }
                        }
                    }
            }

                // 지도 표시
                selectedLocation?.let { location ->
                    NatureComponents.NatureCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        containerColor = NatureColors.whiteTransparent
                    ) {
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(
                                LatLng(location.latitude, location.longitude), 15f
                            )
                        }
                        
                        // 선택된 장소가 변경될 때마다 카메라 이동
                        LaunchedEffect(location) {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLngZoom(
                                    LatLng(location.latitude, location.longitude), 15f
                                ),
                                durationMs = 1000
                            )
                        }
                        
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState
                        ) {
                            Marker(
                                state = MarkerState(
                                    position = LatLng(location.latitude, location.longitude)
                                ),
                                title = location.name,
                                snippet = location.address
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                NatureComponents.NatureButton(
                    onClick = {
                        errorMessage = ""
                        
                        when {
                            schoolName.isBlank() -> {
                                errorMessage = "학교명을 입력해주세요."
                                return@NatureButton
                            }
                            className.isBlank() -> {
                                errorMessage = "반 이름을 입력해주세요."
                                return@NatureButton
                            }
                            maxStudents.isBlank() -> {
                                errorMessage = "최대 인원을 입력해주세요."
                                return@NatureButton
                            }
                            selectedLocation == null -> {
                                errorMessage = "장소를 선택해주세요."
                                return@NatureButton
                            }
                        }

                        val maxStudentsInt = maxStudents.toIntOrNull()
                        if (maxStudentsInt == null || maxStudentsInt <= 0 || maxStudentsInt > 999) {
                            errorMessage = "최대 인원은 1명 이상 999명 이하로 입력해주세요."
                            return@NatureButton
                        }

                        viewModel.createClass(
                            schoolName = schoolName.trim(),
                            className = className.trim(),
                            maxStudents = maxStudentsInt,
                            description = description.trim(),
                            region = selectedLocation?.name ?: ""
                        )
                    },
                    text = if (isLoading) "생성 중..." else "🏫 반 생성 완료",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading,
                    backgroundColor = NatureColors.leafGreen
                )
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