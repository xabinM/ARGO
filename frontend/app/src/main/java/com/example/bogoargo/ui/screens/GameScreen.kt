package com.example.bogoargo.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // 위치 권한 상태
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PermissionChecker.PERMISSION_GRANTED
        )
    }
    
    // 위치 권한 요청 launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    
    // 기본 위치 (서울시청)
    val defaultLocation = LatLng(37.5665, 126.9780)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }
    
    var isGameStarted by remember { mutableStateOf(false) }
    
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
                                if (!hasLocationPermission) {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
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
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = hasLocationPermission,
                    zoomControlsEnabled = false
                )
            ) {
                // 게임 마커들이 여기에 추가될 예정
                if (isGameStarted) {
                    // 미션 마커 예시
                    Marker(
                        state = MarkerState(position = defaultLocation),
                        title = "미션 지점",
                        snippet = "여기에 도달하세요!"
                    )
                }
            }
            
            // 게임 UI 오버레이
            if (isGameStarted) {
                GameOverlay(
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            } else {
                GameStartOverlay(
                    modifier = Modifier.align(Alignment.Center),
                    onStartGame = { 
                        if (!hasLocationPermission) {
                            // 위치 권한 요청
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                        isGameStarted = true 
                    }
                )
            }
        }
    }
}

@Composable
fun GameOverlay(
    modifier: Modifier = Modifier
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
                    text = "진행률: 0/5 완료",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "점수: 0",
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