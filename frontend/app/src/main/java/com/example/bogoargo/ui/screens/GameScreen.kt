package com.example.bogoargo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bogoargo.data.api.ApiClient
import com.example.bogoargo.data.model.MissionSpot
import com.example.bogoargo.data.repository.MissionRepository
import com.example.bogoargo.ui.viewmodels.MapViewModel
import com.example.bogoargo.util.LocationUtils
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    navController: NavHostController,
    classId: Long = 1, // 기본 클래스 ID
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // ViewModel 초기화
    val viewModel: MapViewModel = viewModel {
        MapViewModel(
            missionRepository = MissionRepository(
                ApiClient.authApiService,
                context
            )
        )
    }
    val uiState by viewModel.uiState.collectAsState()
    
    // 위치 추적을 위한 상태 관리
    var locationCallback by remember { mutableStateOf<LocationCallback?>(null) }
    var fusedLocationClient by remember { mutableStateOf<FusedLocationProviderClient?>(null) }
    
    // 위치 권한 요청
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            // 위치 권한이 승인되면 연속 위치 추적 시작
            val (client, callback) = startContinuousLocationTracking(context, viewModel)
            fusedLocationClient = client
            locationCallback = callback
        }
    }
    
    // 기본 위치 (서울시청)
    val defaultLocation = LatLng(37.5665, 126.9780)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }
    
    var isGameStarted by remember { mutableStateOf(false) }
    
    // 권한 확인 및 위치 추적 시작
    LaunchedEffect(isGameStarted) {
        if (isGameStarted) {
            viewModel.loadMissionSpots(classId)
            
            val hasLocationPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            
            if (hasLocationPermission) {
                val (client, callback) = startContinuousLocationTracking(context, viewModel)
                fusedLocationClient = client
                locationCallback = callback
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
    
    // 화면이 사라질 때 위치 추적 중지
    DisposableEffect(Unit) {
        onDispose {
            locationCallback?.let { callback ->
                fusedLocationClient?.removeLocationUpdates(callback)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Argo Game") },
                actions = {
                    TextButton(
                        onClick = { 
                            if (isGameStarted) {
                                // 게임 종료 후 홈으로 이동
                                navController.navigate("home") {
                                    popUpTo("game") { inclusive = true }
                                }
                            } else {
                                // 게임 시작 로직
                                isGameStarted = true
                            }
                        }
                    ) {
                        Text(if (isGameStarted) "홈으로" else "게임 시작")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Google Map
            if (uiState.missionSpots.isNotEmpty() || !isGameStarted) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = isGameStarted && 
                            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    ),
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = isGameStarted && 
                            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED,
                        zoomControlsEnabled = false
                    )
                ) {
                    // 미션 스팟 마커들 (포켓몬GO 스타일)
                    if (isGameStarted) {
                        uiState.missionSpots.forEach { spot ->
                            val isNearby = uiState.nearbyMissionSpots.contains(spot)
                            Marker(
                                state = MarkerState(
                                    position = LatLng(spot.latitude, spot.longitude)
                                ),
                                title = if (isNearby) "🎯 ${spot.spotName} (활성화됨)" else "📍 ${spot.spotName}",
                                snippet = if (isNearby) "미션을 시작할 수 있습니다!" else "가까이 이동하세요 (${spot.spotId})"
                            )
                        }
                        
                        // 사용자 위치 마커
                        uiState.userLocation?.let { location ->
                            Marker(
                                state = MarkerState(
                                    position = LatLng(location.latitude, location.longitude)
                                ),
                                title = "🚶‍♂️ 내 위치"
                            )
                        }
                        
                        // 근처 미션 범위 표시 (Circle)
                        uiState.nearbyMissionSpots.forEach { spot ->
                            Circle(
                                center = LatLng(spot.latitude, spot.longitude),
                                radius = 50.0, // 50미터 범위
                                strokeColor = androidx.compose.ui.graphics.Color.Green,
                                strokeWidth = 3f,
                                fillColor = androidx.compose.ui.graphics.Color.Green.copy(alpha = 0.2f)
                            )
                        }
                    }
                }

                // 지도 카메라 위치 업데이트
                LaunchedEffect(uiState.missionSpots) {
                    if (uiState.missionSpots.isNotEmpty()) {
                        val firstSpot = uiState.missionSpots.first()
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(firstSpot.latitude, firstSpot.longitude),
                                12f
                            )
                        )
                    }
                }
            }
            
            // 로딩 표시
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // 게임 UI 오버레이
            if (isGameStarted) {
                GameOverlay(
                    modifier = Modifier.align(Alignment.TopCenter),
                    missionSpots = uiState.missionSpots,
                    completedCount = 0
                )
                
                // 근처 미션 AR 버튼들 (포켓몬GO 스타일)
                if (uiState.nearbyMissionSpots.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        // 진동 효과 및 알림 텍스트
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "🎯 미션 지점에 도착했습니다!",
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiary
                            )
                        }
                        
                        uiState.nearbyMissionSpots.forEach { spot ->
                            Button(
                                onClick = { 
                                    // AR 화면으로 이동 (위치 정보 포함)
                                    navController.navigate("ar/${spot.spotId}/${spot.latitude}/${spot.longitude}")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "📱",
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = "${spot.spotName} AR 시작",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 개발용 현재 좌표 표시 창
                if (isGameStarted) {
                    CurrentLocationOverlay(
                        modifier = Modifier.align(Alignment.BottomStart),
                        userLocation = uiState.userLocation
                    )
                }
            } else {
                GameStartOverlay(
                    modifier = Modifier.align(Alignment.Center),
                    onStartGame = { 
                        isGameStarted = true 
                    }
                )
            }
        }
    }
}

@Composable
fun GameOverlay(
    modifier: Modifier = Modifier,
    missionSpots: List<MissionSpot>,
    completedCount: Int
) {
    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "현장체험학습 진행중",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "진행률: $completedCount/${missionSpots.size} 완료",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "점수: ${completedCount * 100}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "🎯 가까운 미션 찾기",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun GameStartOverlay(
    modifier: Modifier = Modifier,
    onStartGame: () -> Unit
) {
    Card(
        modifier = modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎓 Argo 현장체험학습",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // 게임 설명
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "📍 게임 방법:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                GameInstructionItem(
                    icon = "🗺️",
                    text = "지도에서 미션 지점들을 확인하세요"
                )
                GameInstructionItem(
                    icon = "🚶‍♂️",
                    text = "실제로 미션 지점 근처로 이동하세요"
                )
                GameInstructionItem(
                    icon = "🎯",
                    text = "범위 내 도달 시 미션이 활성화됩니다"
                )
                GameInstructionItem(
                    icon = "📝",
                    text = "퀴즈나 과제를 해결하여 점수를 획득하세요"
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onStartGame,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("체험학습 시작하기")
            }
        }
    }
}

@Composable
fun GameInstructionItem(
    icon: String,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun CurrentLocationOverlay(
    modifier: Modifier = Modifier,
    userLocation: android.location.Location?
) {
    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "📍 현재 위치",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (userLocation != null) {
                Text(
                    text = "위도: ${String.format("%.6f", userLocation.latitude)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "경도: ${String.format("%.6f", userLocation.longitude)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "정확도: ${userLocation.accuracy.toInt()}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                Text(
                    text = "위치 정보 없음",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun startContinuousLocationTracking(
    context: android.content.Context,
    viewModel: MapViewModel
): Pair<FusedLocationProviderClient, LocationCallback> {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    // 위치 요청 설정: 높은 정확도, 5초마다 업데이트
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 5000L
    ).apply {
        setMinUpdateDistanceMeters(5f) // 5미터 이상 이동 시 업데이트
        setMinUpdateIntervalMillis(3000L) // 최소 3초 간격
        setMaxUpdateDelayMillis(10000L) // 최대 10초 지연
    }.build()
    
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                viewModel.updateUserLocation(location)
            }
        }
        
        override fun onLocationAvailability(availability: LocationAvailability) {
            if (!availability.isLocationAvailable) {
                // 위치 서비스가 사용 불가능한 경우 처리
                android.util.Log.w("GameScreen", "Location not available")
            }
        }
    }
    
    try {
        // 연속적인 위치 업데이트 시작
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            android.os.Looper.getMainLooper()
        )
        
        // 초기 위치도 한 번 가져오기
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                viewModel.updateUserLocation(it)
            }
        }
    } catch (e: SecurityException) {
        android.util.Log.e("GameScreen", "Location permission denied", e)
    }
    
    return Pair(fusedLocationClient, locationCallback)
}