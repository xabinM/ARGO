package com.example.bogoargo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.MotionEvent
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.ar.core.Config
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.node.ModelNode
import com.example.bogoargo.ui.screens.ar.components.AROverlay
import com.example.bogoargo.ui.screens.ar.components.DebugInfoPanel
import com.example.bogoargo.ui.screens.ar.components.ErrorScreen
import com.example.bogoargo.ui.screens.ar.components.LoadingScreen
import com.example.bogoargo.ui.screens.ar.model.ARDebugInfo
import com.example.bogoargo.ui.screens.ar.utils.checkLocationServicesStatus
import com.example.bogoargo.ui.screens.ar.utils.setupARScene




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARScreen(
    spotId: Long,
    latitude: Double,
    longitude: Double,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasARPermissions by remember { mutableStateOf(false) }
    var arSceneView: ARSceneView? by remember { mutableStateOf(null) }
    var isSessionInitialized by remember { mutableStateOf(false) }
    var missionCompleted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // 터치 이벤트 처리
    var touchDownPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    
    // 디버그 관련 상태
    var showDebugInfo by remember { mutableStateOf(false) }
    var debugInfo by remember { 
        mutableStateOf(ARDebugInfo(
            targetLatitude = latitude,
            targetLongitude = longitude
        )) 
    }
    

    // AR 관련 권한 확인
    val arPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasARPermissions = permissions.values.all { it }
        if (!hasARPermissions) {
            errorMessage = "AR 기능을 사용하려면 카메라와 위치 권한이 필요합니다."
        }
    }

    // 권한 확인
    LaunchedEffect(Unit) {
        val permissionResults = arPermissions.map { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionResults.all { it }) {
            hasARPermissions = true
        } else {
            permissionLauncher.launch(arPermissions)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("AR 미션 - Spot $spotId") 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                errorMessage != null -> {
                    // 에러 화면
                    ErrorScreen(
                        message = errorMessage!!,
                        onRetry = {
                            errorMessage = null
                            permissionLauncher.launch(arPermissions)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                !hasARPermissions -> {
                    // 권한 대기 화면
                    LoadingScreen(
                        message = "AR 권한을 확인하는 중...",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                else -> {
                    // AR 화면
                    AndroidView(
                        factory = { context ->
                            ARSceneView(context).apply {
                                this@apply.lifecycle = null
                                arSceneView = this
                                
                                // ARCore 세션 설정
                                configureSession { session, config ->
                                    try {
                                        // 위치 서비스 상태 확인
                                        val (gpsEnabled, networkEnabled, locationServicesEnabled) = checkLocationServicesStatus(context)
                                        
                                        Log.d("ARScreen", "Location services status - GPS: $gpsEnabled, Network: $networkEnabled, Services: $locationServicesEnabled")
                                        
                                        if (!locationServicesEnabled) {
                                            Log.w("ARScreen", "Location services are disabled. Geospatial API may not work properly.")
                                        }
                                        
                                        // Geospatial API 활성화
                                        config.geospatialMode = Config.GeospatialMode.ENABLED
                                        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                                        config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                                        
                                        Log.d("ARScreen", "AR Session configured with Geospatial API enabled")
                                        isSessionInitialized = true
                                        Log.d("ARScreen", "AR Session initialized for spot $spotId at $latitude, $longitude")
                                        
                                    } catch (e: Exception) {
                                        Log.e("ARScreen", "Error configuring AR session", e)
                                        errorMessage = "AR 세션 설정 중 오류가 발생했습니다: ${e.message}"
                                    }
                                }
                                
                                // AR 세션 콜백 설정
                                onSessionCreated = { session ->
                                    try {
                                        setupARScene(this, session, spotId, latitude, longitude, 
                                            onMissionComplete = {
                                                missionCompleted = true
                                            },
                                            onDebugInfoUpdate = { newDebugInfo ->
                                                debugInfo = newDebugInfo
                                            },
                                            onModelNodeUpdate = { modelNode ->
                                                // 모델 노드 업데이트 로직 (필요시 추가)
                                            }
                                        )
                                    } catch (e: Exception) {
                                        Log.e("ARScreen", "Error setting up AR scene", e)
                                        errorMessage = "AR 장면 설정 중 오류가 발생했습니다."
                                    }
                                }
                                
                                // 터치 이벤트 리스너 설정 (개선된 클릭 감지)
                                setOnTouchListener { view, motionEvent ->
                                    when (motionEvent.action) {
                                        MotionEvent.ACTION_DOWN -> {
                                            // 터치 시작 위치 저장
                                            touchDownPosition = Pair(motionEvent.x, motionEvent.y)
                                            true
                                        }
                                        
                                        MotionEvent.ACTION_UP -> {
                                            try {
                                                val downPos = touchDownPosition
                                                if (downPos != null) {
                                                    // 터치 이동 거리 계산 (클릭 vs 드래그 구분)
                                                    val deltaX = kotlin.math.abs(motionEvent.x - downPos.first)
                                                    val deltaY = kotlin.math.abs(motionEvent.y - downPos.second)
                                                    val isClick = deltaX < 50 && deltaY < 50 // 50픽셀 이내면 클릭으로 간주
                                                    
                                                    if (isClick) {
                                                        // 클릭 감지 로그
                                                        Log.d("ARScreen", "Click detected on AR view")
                                                        
                                                        // 햅틱 피드백
                                                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                                        
                                                        // 필요시 다른 클릭 처리 로직 추가 가능
                                                        
                                                        view.performClick()
                                                        touchDownPosition = null
                                                        return@setOnTouchListener true
                                                    }
                                                }
                                                touchDownPosition = null
                                            } catch (e: Exception) {
                                                Log.e("ARScreen", "Error handling click event", e)
                                                touchDownPosition = null
                                            }
                                        }
                                        
                                        MotionEvent.ACTION_CANCEL -> {
                                            // 터치 취소 시 상태 초기화
                                            touchDownPosition = null
                                        }
                                    }
                                    false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // AR UI 오버레이
                    AROverlay(
                        isSessionInitialized = isSessionInitialized,
                        missionCompleted = missionCompleted,
                        spotId = spotId,
                        onMissionComplete = {
                            missionCompleted = true
                        },
                        debugInfo = debugInfo,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // 디버그 토글 버튼 (좌측 상단)
                    FloatingActionButton(
                        onClick = { showDebugInfo = !showDebugInfo },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .size(48.dp),
                        containerColor = Color.Black.copy(alpha = 0.7f),
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = if (showDebugInfo) Icons.Filled.BugReport else Icons.Outlined.BugReport,
                            contentDescription = "디버그 정보 토글",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // 디버그 정보 패널
                    AnimatedVisibility(
                        visible = showDebugInfo,
                        enter = slideInHorizontally(initialOffsetX = { -it }),
                        exit = slideOutHorizontally(targetOffsetX = { -it }),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        DebugInfoPanel(
                            debugInfo = debugInfo,
                            modifier = Modifier
                                .padding(start = 80.dp, top = 16.dp, end = 16.dp)
                        )
                    }
                }
            }
        }
    }

    // 화면이 사라질 때 AR 리소스 정리
    DisposableEffect(Unit) {
        onDispose {
            arSceneView?.destroy()
        }
    }
}


