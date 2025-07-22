package com.example.bogoargo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
                                // 게임 종료 로직
                                isGameStarted = false
                            } else {
                                // 게임 시작 로직
                                isGameStarted = true
                            }
                        }
                    ) {
                        Text(if (isGameStarted) "게임 종료" else "게임 시작")
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
                    isMyLocationEnabled = true
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true,
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
                    onStartGame = { isGameStarted = true }
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
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "게임 진행 중",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "미션 지점에 도달하세요!",
                style = MaterialTheme.typography.bodyMedium
            )
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
                text = "Argo 게임",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "지도를 활용한 위치 기반 게임을 시작해보세요!",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onStartGame,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("게임 시작")
            }
        }
    }
}