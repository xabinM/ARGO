package com.example.bogoargo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.bogoargo.domain.model.MissionSpot
import com.example.bogoargo.ui.viewmodels.MapViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    navController: NavHostController,
    classId: Long, // 기본 클래스 ID
    teamId: Long, // 팀 ID 추가
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // ViewModel 초기화
    val viewModel: MapViewModel = hiltViewModel()
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
    var showMissionList by remember { mutableStateOf(false) }
    var hasMovedToUserLocation by remember { mutableStateOf(false) }
    var showMissionBlockedDialog by remember { mutableStateOf(false) }
    var blockedMissionMessage by remember { mutableStateOf("") }
    var checkingMissionId by remember { mutableStateOf<Long?>(null) }
    
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
    
    // 사용자 위치가 업데이트되면 처음에만 카메라 이동
    LaunchedEffect(uiState.userLocation) {
        val location = uiState.userLocation
        if (location != null && !hasMovedToUserLocation && isGameStarted) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude),
                    16f
                ),
                durationMs = 1000
            )
            hasMovedToUserLocation = true
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
            NatureComponents.NatureTopAppBar(
                title = "Argo 체험학습",
                emoji = "🎓",
                onNavigationClick = { navController.popBackStack() }
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
                                    position = LatLng(spot.coordinates.latitude, spot.coordinates.longitude)
                                ),
                                title = if (isNearby) "🎯 ${spot.name} (활성화됨)" else "📍 ${spot.name}",
                                snippet = if (isNearby) "미션을 시작할 수 있습니다!" else "가까이 이동하세요 (${spot.spotId})"
                            )
                        }
                        
                        // 근처 미션 범위 표시 (Circle)
                        uiState.nearbyMissionSpots.forEach { spot ->
                            Circle(
                                center = LatLng(spot.coordinates.latitude, spot.coordinates.longitude),
                                radius = 50.0, // 50미터 범위
                                strokeColor = androidx.compose.ui.graphics.Color.Green,
                                strokeWidth = 3f,
                                fillColor = androidx.compose.ui.graphics.Color.Green.copy(alpha = 0.2f)
                            )
                        }
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
                    modifier = Modifier.align(Alignment.TopCenter)
                )
                
                // 근처 미션 AR 버튼들 (포켓몬GO 스타일)
                if (uiState.nearbyMissionSpots.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        // 진동 효과 및 알림 텍스트
                        NatureComponents.NatureCard(
                            containerColor = NatureColors.leafGreen.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp),
                            elevation = NatureElevation.medium
                        ) {
                            Text(
                                text = "🎯 미션 지점에 도착했습니다!",
                                modifier = Modifier.padding(12.dp),
                                style = NatureTypography.titleMedium,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                        
                        // 근처 미션 선택 버튼
                        NatureComponents.NatureButton(
                            onClick = { 
                                showMissionList = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            backgroundColor = NatureColors.forestGreen
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "🎯",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = "근처 미션 보기 (${uiState.nearbyMissionSpots.size}개)",
                                    style = NatureTypography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = androidx.compose.ui.graphics.Color.White
                                )
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
            
            // 미션 리스트 다이얼로그
            if (showMissionList && uiState.nearbyMissionSpots.isNotEmpty()) {
                MissionListDialog(
                    missions = uiState.nearbyMissionSpots,
                    classId = classId,
                    teamId = teamId,
                    checkingMissionId = checkingMissionId,
                    onMissionSelect = { spot ->
                        // 미션 체크 시작
                        checkingMissionId = spot.spotId
                        
                        // 미션 가능 여부 확인 후 처리
                        viewModel.checkMissionPossibility(teamId, spot.spotId) { isSuccess, message ->
                            checkingMissionId = null
                            if (isSuccess) {
                                // 미션 진행 가능 - AR 화면으로 이동
                                showMissionList = false
                                navController.navigate("ar/${spot.spotId}/${spot.coordinates.latitude}/${spot.coordinates.longitude}?classId=$classId&teamId=$teamId")
                            } else {
                                // 미션 진행 불가이고 메시지가 있을 때만 경고 다이얼로그 표시
                                showMissionList = false
                                if (message.isNotBlank()) {
                                    blockedMissionMessage = message
                                    showMissionBlockedDialog = true
                                }
                            }
                        }
                    },
                    onDismiss = { 
                        checkingMissionId = null
                        showMissionList = false 
                    }
                )
            }

            // 미션 차단 다이얼로그
            if (showMissionBlockedDialog) {
                MissionBlockedDialog(
                    message = blockedMissionMessage,
                    onDismiss = { showMissionBlockedDialog = false }
                )
            }
        }
    }
}

@Composable
fun GameOverlay(
    modifier: Modifier = Modifier
) {
    NatureComponents.NatureCard(
        modifier = modifier.padding(16.dp),
        containerColor = NatureColors.whiteTransparent,
        elevation = NatureElevation.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📍 지도에서 미션 지점을 찾아보세요",
                style = NatureTypography.titleMedium,
                color = NatureColors.forestGreen,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "가까이 다가가면 미션을 시작할 수 있습니다",
                style = NatureTypography.bodyMedium,
                color = NatureColors.earthBrown,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun GameStartOverlay(
    modifier: Modifier = Modifier,
    onStartGame: () -> Unit
) {
    NatureComponents.NatureCard(
        modifier = modifier.padding(16.dp),
        containerColor = NatureColors.whiteTransparent90,
        elevation = NatureElevation.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎓 Argo 현장체험학습",
                style = NatureTypography.headlineMedium,
                color = NatureColors.forestGreen
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // 게임 설명
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "📍 게임 방법:",
                    style = NatureTypography.titleLarge,
                    color = NatureColors.earthBrown,
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
            NatureComponents.NatureButton(
                onClick = onStartGame,
                text = "체험학습 시작하기 🌱",
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NatureColors.leafGreen
            )
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
            style = NatureTypography.titleMedium,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = text,
            style = NatureTypography.bodyMedium,
            color = NatureColors.earthBrown
        )
    }
}

@Composable
fun CurrentLocationOverlay(
    modifier: Modifier = Modifier,
    userLocation: android.location.Location?
) {
    NatureComponents.NatureCard(
        modifier = modifier.padding(16.dp),
        containerColor = NatureColors.whiteTransparent,
        elevation = NatureElevation.small
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "📍 현재 위치",
                style = NatureTypography.titleSmall,
                color = NatureColors.forestGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            val location = userLocation
            if (location != null) {
                Text(
                    text = "위도: ${String.format("%.6f", location.latitude)}",
                    style = NatureTypography.bodySmall
                )
                Text(
                    text = "경도: ${String.format("%.6f", location.longitude)}",
                    style = NatureTypography.bodySmall
                )
                Text(
                    text = "정확도: ${location.accuracy.toInt()}m",
                    style = NatureTypography.bodySmall,
                    color = NatureColors.leafGreen
                )
            } else {
                Text(
                    text = "위치 정보 없음",
                    style = NatureTypography.bodySmall,
                    color = androidx.compose.ui.graphics.Color.Red
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

@Composable
fun MissionListDialog(
    missions: List<MissionSpot>,
    classId: Long,
    teamId: Long,
    checkingMissionId: Long? = null,
    onMissionSelect: (MissionSpot) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯 근처 미션 선택",
                    style = NatureTypography.titleLarge,
                    color = NatureColors.forestGreen
                )
                Text(
                    text = "${missions.size}개",
                    style = NatureTypography.bodyMedium,
                    color = NatureColors.earthBrown
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "참여하고 싶은 미션을 선택하세요",
                    style = NatureTypography.bodyMedium,
                    color = NatureColors.earthBrown,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(missions) { mission ->
                        MissionListItem(
                            mission = mission,
                            isCheckingPossibility = checkingMissionId == mission.spotId,
                            onClick = {
                                if (checkingMissionId == null) {
                                    onMissionSelect(mission)
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            NatureComponents.NatureButton(
                onClick = onDismiss,
                text = "취소",
                backgroundColor = NatureColors.earthBrown
            )
        },
        containerColor = NatureColors.whiteTransparent90,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun MissionListItem(
    mission: MissionSpot,
    isCheckingPossibility: Boolean = false,
    onClick: () -> Unit
) {
    NatureComponents.NatureCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        containerColor = NatureColors.whiteTransparent,
        elevation = NatureElevation.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = mission.name,
                        style = NatureTypography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = NatureColors.forestGreen
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (isCheckingPossibility) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = NatureColors.leafGreen
                    )
                } else {
                    Text(
                        text = "📱",
                        fontSize = 24.sp
                    )
                }
                Text(
                    text = if (isCheckingPossibility) "확인중..." else "AR 시작",
                    style = NatureTypography.bodySmall,
                    color = if (isCheckingPossibility) NatureColors.earthBrown else NatureColors.leafGreen
                )
            }
        }
    }
}

@Composable
fun MissionBlockedDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚠️ 미션 진행 불가",
                    style = NatureTypography.titleLarge,
                    color = NatureColors.earthBrown
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    style = NatureTypography.bodyMedium,
                    color = NatureColors.earthBrown,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Text(
                    text = "다른 미션을 선택하시거나 나중에 다시 시도해주세요.",
                    style = NatureTypography.bodySmall,
                    color = NatureColors.earthBrown.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            NatureComponents.NatureButton(
                onClick = onDismiss,
                text = "확인",
                backgroundColor = NatureColors.leafGreen
            )
        },
        containerColor = NatureColors.whiteTransparent90,
        modifier = Modifier.padding(16.dp)
    )
}