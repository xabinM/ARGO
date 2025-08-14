package com.example.bogoargo.ui.screens.classRoom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.ui.viewmodels.classRoom.StudentLocationViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLngBounds
import com.example.bogoargo.domain.model.UserCoordinates
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLocationScreen(
    navController: NavController,
    classId: Long,
    viewModel: StudentLocationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(classId) {
        viewModel.loadCoordinatesByClass(classId)
    }

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "학생 위치 보기",
                emoji = "📍",
                onNavigationClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    NatureComponents.NatureCard(
                        elevation = NatureElevation.medium,
                        shape = NatureShapes.large,
                        containerColor = NatureColors.sunnyYellow.copy(alpha = 0.1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
//                            Text(
//                                text = "📍 학생 위치 정보",
//                                style = NatureTypography.titleMedium.copy(
//                                    color = NatureColors.forestGreen
//                                )
//                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "반 이름: ${uiState.className}",
                                style = NatureTypography.bodyMedium.copy(
                                    color = NatureColors.earthBrown
                                )
                            )
                            if (uiState.userCoordinates.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${uiState.userCoordinates.size}명의 위치 정보",
                                    style = NatureTypography.bodyMedium.copy(
                                        color = NatureColors.forestGreen
                                    )
                                )
                            }
                        }
                    }
                }

                when {
                    uiState.isLoading -> {
                        item {
                            NatureComponents.NatureLoadingIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                    }
                    uiState.errorMessage != null -> {
                        item {
                            NatureComponents.NatureCard(
                                containerColor = NatureColors.sunnyYellow.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "⚠️ ${uiState.errorMessage}",
                                    style = NatureTypography.bodyMedium.copy(
                                        color = NatureColors.earthBrown
                                    ),
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    uiState.userCoordinates.isEmpty() -> {
                        item {
                            NatureComponents.NatureCard(
                                containerColor = NatureColors.leafGreen.copy(alpha = 0.1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "👥",
                                        fontSize = 48.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "아직 위치를 공유한 학생이 없습니다",
                                        style = NatureTypography.bodyMedium.copy(
                                            color = NatureColors.earthBrown
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        // Google Maps
                        item {
                            NatureComponents.NatureCard(
                                elevation = NatureElevation.medium,
                                shape = NatureShapes.large
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "🗺️ 지도에서 위치 보기",
                                        style = NatureTypography.titleMedium.copy(
                                            color = NatureColors.forestGreen
                                        ),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    
                                    GoogleMapView(
                                        coordinates = uiState.userCoordinates,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp)
                                    )
                                }
                            }
                        }
                        
                        // 좌표 리스트
                        items(uiState.userCoordinates) { coordinates ->
                            NatureComponents.NatureCard(
                                elevation = NatureElevation.medium,
                                shape = NatureShapes.medium
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 학생 정보
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "👤 사용자 ID: ${coordinates.userId}",
                                            style = NatureTypography.titleMedium.copy(
                                                color = NatureColors.earthBrown
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "📍 위도: ${String.format("%.6f", coordinates.latitude)}",
                                            style = NatureTypography.bodySmall.copy(
                                                color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                            )
                                        )
                                        Text(
                                            text = "📍 경도: ${String.format("%.6f", coordinates.longitude)}",
                                            style = NatureTypography.bodySmall.copy(
                                                color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                            )
                                        )
                                    }
                                    
                                    // 위치 상태 표시
                                    NatureComponents.NatureCard(
                                        shape = NatureShapes.small,
                                        containerColor = NatureColors.leafGreen.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "🟢",
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleMapView(
    coordinates: List<UserCoordinates>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var map by remember { mutableStateOf<GoogleMap?>(null) }
    
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                onCreate(null)
                onResume()
                getMapAsync { googleMap ->
                    map = googleMap
                    setupMap(googleMap, coordinates)
                }
            }
        },
        update = { mapView ->
            map?.let { googleMap ->
                setupMap(googleMap, coordinates)
            }
        }
    )
}

private fun setupMap(googleMap: GoogleMap, coordinates: List<UserCoordinates>) {
    googleMap.clear()
    
    if (coordinates.isEmpty()) return
    
    val boundsBuilder = LatLngBounds.Builder()
    
    coordinates.forEach { coordinate ->
        val latLng = LatLng(coordinate.latitude, coordinate.longitude)
        
        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("사용자 ID: ${coordinate.userId}")
                .snippet("위도: ${String.format("%.6f", coordinate.latitude)}, 경도: ${String.format("%.6f", coordinate.longitude)}")
        )
        
        boundsBuilder.include(latLng)
    }
    
    if (coordinates.size == 1) {
        // 단일 마커인 경우 해당 위치로 카메라 이동
        val latLng = LatLng(coordinates[0].latitude, coordinates[0].longitude)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    } else {
        // 여러 마커인 경우 모든 마커가 보이도록 카메라 조정
        val bounds = boundsBuilder.build()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }
}

private fun formatTimestamp(timestamp: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")
    return timestamp.format(formatter)
}

@Preview(showBackground = true)
@Composable
fun StudentLocationScreenPreview() {
    MaterialTheme {
        StudentLocationScreen(
            navController = rememberNavController(),
            classId = 1L
        )
    }
}