package com.example.bogoargo.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import kotlin.random.Random

// 디버그 정보를 담는 데이터 클래스
data class ARDebugInfo(
    val currentLatitude: Double = 0.0,
    val currentLongitude: Double = 0.0,
    val targetLatitude: Double = 0.0,
    val targetLongitude: Double = 0.0,
    val gpsAccuracy: Double = 0.0,
    val earthTrackingState: String = "UNKNOWN",
    val anchorMethod: String = "NONE",
    val actualAnchorType: String = "NONE", // 실제 사용 중인 앵커 타입
    val isSessionInitialized: Boolean = false,
    val geospatialApiStatus: String = "DISABLED",
    val modelLoadingStatus: String = "NOT_STARTED",
    val terrainAnchorState: String = "NONE",
    val availableModels: List<String> = emptyList(),
    val frameCount: Long = 0,
    val gpsEnabled: Boolean = false,
    val locationServicesEnabled: Boolean = false,
    val networkConnected: Boolean = false,
    val googlePlayServicesAvailable: Boolean = false,
    val planesDetected: Int = 0,
    val planeAnchorUsed: Boolean = false,
    val animationStatus: String = "NONE" // NONE, READY, PLAYED
)

// 애니메이션 실행 함수 (한번만 실행)
private fun playAnimationOnce(
    modelNode: ModelNode, 
    onAnimationPlayed: () -> Unit,
    onGlobalAnimationPlayed: () -> Unit = {}
) {
    try {
        // 애니메이션 한번만 재생 (loop = false)
        modelNode.playAnimation(animationIndex = 0, loop = false)
        onAnimationPlayed() // 로컬 애니메이션 재생 상태 업데이트
        onGlobalAnimationPlayed() // 글로벌 애니메이션 재생 상태 업데이트
        Log.d("ARScreen", "Animation started - will play once")
    } catch (e: Exception) {
        Log.w("ARScreen", "No animations available for this model: ${e.message}")
    }
}

// GPS 및 위치 서비스 상태 확인 함수
private fun checkLocationServicesStatus(context: Context): Triple<Boolean, Boolean, Boolean> {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    
    // GPS 활성화 상태
    val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    
    // 네트워크 위치 서비스 활성화 상태  
    val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    
    // 전체 위치 서비스 활성화 상태
    val locationServicesEnabled = try {
        Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_OFF
    } catch (e: Exception) {
        false
    }
    
    return Triple(gpsEnabled, networkEnabled, locationServicesEnabled)
}

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
    
    // 3D 객체 상태 관리 (컴포넌트 레벨)
    var currentModelNode by remember { mutableStateOf<ModelNode?>(null) }
    var isAnimationPlayed by remember { mutableStateOf(false) }
    var touchDownPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    
    // 디버그 관련 상태
    var showDebugInfo by remember { mutableStateOf(false) }
    var debugInfo by remember { 
        mutableStateOf(ARDebugInfo(
            targetLatitude = latitude,
            targetLongitude = longitude
        )) 
    }
    
    // 애니메이션 상태 확인 함수 (컴포넌트 레벨)
    fun getAnimationStatus(): String {
        return when {
            currentModelNode == null -> "NONE"
            isAnimationPlayed -> "PLAYED"
            else -> "READY"
        }
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
                                                debugInfo = newDebugInfo.copy(
                                                    animationStatus = getAnimationStatus()
                                                )
                                            },
                                            onModelNodeUpdate = { modelNode ->
                                                currentModelNode = modelNode
                                                isAnimationPlayed = false // 새 모델이므로 초기화
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
                                                    
                                                    if (isClick && currentModelNode != null) {
                                                        // 간단한 거리 기반 터치 감지 (레이캐스팅 대안)
                                                        // 실제 앱에서는 더 정교한 충돌 감지를 구현할 수 있습니다
                                                        Log.d("ARScreen", "Processing click on 3D object")
                                                        
                                                        // 이미 애니메이션이 재생된 경우 무시
                                                        if (isAnimationPlayed) {
                                                            Log.d("ARScreen", "Animation already played - ignoring click")
                                                            view.performClick()
                                                            touchDownPosition = null
                                                            return@setOnTouchListener true
                                                        }
                                                        
                                                        // 햅틱 피드백과 애니메이션 동시 실행
                                                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                                        
                                                        // 애니메이션 실행
                                                        playAnimationOnce(
                                                            currentModelNode!!,
                                                            onAnimationPlayed = { /* 로컬 상태는 setupARScene에서 관리 */ },
                                                            onGlobalAnimationPlayed = { isAnimationPlayed = true }
                                                        )
                                                        
                                                        Log.d("ARScreen", "3D object clicked - animation and haptic triggered")
                                                        view.performClick()
                                                        touchDownPosition = null
                                                        return@setOnTouchListener true
                                                    } else if (currentModelNode == null) {
                                                        Log.d("ARScreen", "Click detected but no 3D object available")
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

@Composable
fun DebugInfoPanel(
    debugInfo: ARDebugInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(280.dp)
            .heightIn(max = 400.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "🐛 AR Debug Info",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            HorizontalDivider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // GPS 정보
            DebugInfoItem("📍 Target GPS", "${String.format("%.6f", debugInfo.targetLatitude)}, ${String.format("%.6f", debugInfo.targetLongitude)}")
            DebugInfoItem("📍 Current GPS", "${String.format("%.6f", debugInfo.currentLatitude)}, ${String.format("%.6f", debugInfo.currentLongitude)}")
            DebugInfoItem("🎯 GPS Accuracy", "${String.format("%.1f", debugInfo.gpsAccuracy)}m")
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // AR 상태 정보
            DebugInfoItem("🌍 Earth Tracking", debugInfo.earthTrackingState)
            DebugInfoItem("⚓ Anchor Method", debugInfo.anchorMethod)
            DebugInfoItem("✅ Active Anchor", debugInfo.actualAnchorType)
            DebugInfoItem("🔗 Geospatial API", debugInfo.geospatialApiStatus)
            DebugInfoItem("📦 Model Loading", debugInfo.modelLoadingStatus)
            DebugInfoItem("🏔️ Terrain Anchor", debugInfo.terrainAnchorState)
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 세션 정보
            DebugInfoItem("🔧 Session Init", if (debugInfo.isSessionInitialized) "✅ Ready" else "⏳ Pending")
            DebugInfoItem("🎬 Frame Count", debugInfo.frameCount.toString())
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 시스템 상태 정보
            DebugInfoItem("📍 GPS Service", if (debugInfo.gpsEnabled) "✅ Enabled" else "❌ Disabled")
            DebugInfoItem("📍 Location Service", if (debugInfo.locationServicesEnabled) "✅ Enabled" else "❌ Disabled")
            DebugInfoItem("🌐 Network Location", if (debugInfo.networkConnected) "✅ Available" else "❌ Unavailable")
            DebugInfoItem("🛡️ Play Services", if (debugInfo.googlePlayServicesAvailable) "✅ Available" else "❌ Unavailable")
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 평면 감지 정보
            DebugInfoItem("🔍 Planes Detected", debugInfo.planesDetected.toString())
            DebugInfoItem("📐 Plane Anchor Used", if (debugInfo.planeAnchorUsed) "✅ Yes" else "❌ No")
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 애니메이션 정보
            DebugInfoItem("🎬 Animation Status", debugInfo.animationStatus)
            when (debugInfo.animationStatus) {
                "READY" -> DebugInfoItem("💡 Touch Tip", "Tap 3D object to open")
                "PLAYED" -> DebugInfoItem("📦 Box Status", "Opened ✅")
                else -> {}
            }
            
            if (debugInfo.availableModels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📁 Models:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                debugInfo.availableModels.forEach { model ->
                    Text(
                        text = "  • $model",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DebugInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun LoadingScreen(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚠️ 오류",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("다시 시도")
                }
            }
        }
    }
}

@Composable
fun AROverlay(
    isSessionInitialized: Boolean,
    missionCompleted: Boolean,
    spotId: Long,
    onMissionComplete: () -> Unit,
    modifier: Modifier = Modifier,
    debugInfo: ARDebugInfo = ARDebugInfo()
) {
    Box(modifier = modifier) {
        // 상단 정보 카드
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isSessionInitialized) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AR 세션을 초기화하는 중...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else if (missionCompleted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "완료",
                            tint = Color.Green,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "미션 완료!",
                            color = Color.Green,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // GPS 상태에 따른 안내 메시지
                    when {
                        !debugInfo.locationServicesEnabled -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "⚠️ 위치 서비스 필요",
                                    color = Color.Yellow,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "설정에서 위치 서비스를 활성화해주세요",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                        !debugInfo.gpsEnabled -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "📍 GPS 필요",
                                    color = Color.Yellow,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "정확한 위치 측정을 위해 GPS를 활성화해주세요",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                        debugInfo.earthTrackingState == "STOPPED" -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🌍 위치 정보 확인 중...",
                                    color = Color(0xFFFFA500),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "잠시 기다려주세요. 네트워크 연결을 확인해주세요.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = "🎯 미션 지점을 찾아보세요",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "카메라를 천천히 움직여 주변을 스캔하세요",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // 하단 미션 완료 버튼 (테스트용)
        if (isSessionInitialized && !missionCompleted) {
            Button(
                onClick = onMissionComplete,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
                    .fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = "미션 완료 (테스트)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun setupARScene(
    arSceneView: ARSceneView,
    session: Session,
    spotId: Long,
    latitude: Double,
    longitude: Double,
    onMissionComplete: () -> Unit,
    onDebugInfoUpdate: (ARDebugInfo) -> Unit,
    onModelNodeUpdate: (ModelNode?) -> Unit
) {
    Log.d("ARScreen", "Setting up AR scene for mission spot $spotId at GPS($latitude, $longitude)")
    
    // 모델 파일 목록 수집
    val availableModels = try {
        arSceneView.context.assets.list("models")?.toList() ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    
    var frameCount = 0L
    
    // AR 장면 설정
    arSceneView.onFrame = { frame ->
        frameCount++
        
        // 매 프레임마다 Geospatial API 상태 확인 및 디버그 정보 업데이트
        try {
            val earth = session.earth
            val (gpsEnabled, networkEnabled, locationServicesEnabled) = checkLocationServicesStatus(arSceneView.context)
            
            val debugInfo = if (earth != null) {
                val cameraGeospatialPose = earth.cameraGeospatialPose
                ARDebugInfo(
                    currentLatitude = cameraGeospatialPose.latitude,
                    currentLongitude = cameraGeospatialPose.longitude,
                    targetLatitude = latitude,
                    targetLongitude = longitude,
                    gpsAccuracy = cameraGeospatialPose.horizontalAccuracy,
                    earthTrackingState = earth.trackingState.name,
                    anchorMethod = when {
                        earth.trackingState == TrackingState.TRACKING && cameraGeospatialPose.horizontalAccuracy <= 10.0f -> "TERRAIN_ANCHOR"
                        else -> "FALLBACK_FIXED"
                    },
                    isSessionInitialized = true,
                    geospatialApiStatus = if (earth.trackingState == TrackingState.TRACKING) "ENABLED_TRACKING" else "ENABLED_${earth.trackingState.name}",
                    modelLoadingStatus = "LOADED",
                    terrainAnchorState = "READY",
                    availableModels = availableModels,
                    frameCount = frameCount,
                    gpsEnabled = gpsEnabled,
                    locationServicesEnabled = locationServicesEnabled,
                    networkConnected = networkEnabled,
                    googlePlayServicesAvailable = true // ARCore가 작동한다면 true
                )
            } else {
                ARDebugInfo(
                    targetLatitude = latitude,
                    targetLongitude = longitude,
                    earthTrackingState = "EARTH_NULL",
                    anchorMethod = "FALLBACK_FIXED",
                    isSessionInitialized = true,
                    geospatialApiStatus = "DISABLED",
                    modelLoadingStatus = "LOADED",
                    availableModels = availableModels,
                    frameCount = frameCount,
                    gpsEnabled = gpsEnabled,
                    locationServicesEnabled = locationServicesEnabled,
                    networkConnected = networkEnabled,
                    googlePlayServicesAvailable = true
                )
            }
            
            // 30프레임마다 디버그 정보 업데이트 (성능 고려)
            if (frameCount % 30 == 0L) {
                onDebugInfoUpdate(debugInfo)
            }
            
            if (earth != null) {
                val cameraGeospatialPose = earth.cameraGeospatialPose
                
                // 상세한 진단 로깅 (Earth tracking 상태가 변경될 때만)
                if (frameCount % 60 == 0L) { // 2초마다 상세 로그
                    Log.d("ARScreen", "=== Earth Tracking Diagnosis ===")
                    Log.d("ARScreen", "Earth tracking state: ${earth.trackingState}")
                    Log.d("ARScreen", "GPS accuracy: ${cameraGeospatialPose.horizontalAccuracy}m")
                    Log.d("ARScreen", "Current position: ${cameraGeospatialPose.latitude}, ${cameraGeospatialPose.longitude}")
                    Log.d("ARScreen", "Altitude: ${cameraGeospatialPose.altitude}m")
                    Log.d("ARScreen", "GPS enabled: $gpsEnabled, Location services: $locationServicesEnabled, Network: $networkEnabled")
                    
                    when (earth.trackingState) {
                        TrackingState.STOPPED -> {
                            Log.w("ARScreen", "Earth tracking STOPPED - possible causes:")
                            Log.w("ARScreen", "  - Location services disabled: ${!locationServicesEnabled}")
                            Log.w("ARScreen", "  - GPS disabled: ${!gpsEnabled}")
                            Log.w("ARScreen", "  - Network location disabled: ${!networkEnabled}")
                            Log.w("ARScreen", "  - Insufficient permissions or Google Play Services issue")
                        }
                        TrackingState.PAUSED -> {
                            Log.i("ARScreen", "Earth tracking PAUSED - initializing or temporary loss")
                        }
                        TrackingState.TRACKING -> {
                            Log.i("ARScreen", "Earth tracking ACTIVE - GPS accuracy: ${cameraGeospatialPose.horizontalAccuracy}m")
                        }
                    }
                    Log.d("ARScreen", "=== End Diagnosis ===")
                }
                
                Log.v("ARScreen", "GPS accuracy: ${cameraGeospatialPose.horizontalAccuracy}m, " +
                        "Earth tracking: ${earth.trackingState}")
            } else {
                if (frameCount % 60 == 0L) {
                    Log.e("ARScreen", "Earth object is NULL - Geospatial API not properly initialized")
                    Log.e("ARScreen", "  - Check ARCore installation and Google Play Services")
                    Log.e("ARScreen", "  - GPS enabled: $gpsEnabled, Location services: $locationServicesEnabled")
                }
            }
        } catch (e: Exception) {
            Log.v("ARScreen", "Earth tracking check failed: ${e.message}")
            
            // 오류 상태도 디버그 정보로 업데이트
            if (frameCount % 30 == 0L) {
                onDebugInfoUpdate(ARDebugInfo(
                    targetLatitude = latitude,
                    targetLongitude = longitude,
                    earthTrackingState = "ERROR: ${e.message}",
                    anchorMethod = "FALLBACK_FIXED",
                    isSessionInitialized = true,
                    geospatialApiStatus = "ERROR",
                    modelLoadingStatus = "ERROR",
                    availableModels = availableModels,
                    frameCount = frameCount
                ))
            }
        }
    }
    
    // 모델 파일 존재 확인
    val modelPath = "models/cube.glb"
    val assetList = arSceneView.context.assets.list("models")
    
    if (assetList?.contains("cube.glb") != true) {
        Log.e("ARScreen", "Model file not found: $modelPath")
        Log.d("ARScreen", "Available models: ${assetList?.joinToString()}")
        
        // 디버그 정보 업데이트 - 모델 파일 없음
        onDebugInfoUpdate(ARDebugInfo(
            targetLatitude = latitude,
            targetLongitude = longitude,
            anchorMethod = "MODEL_NOT_FOUND",
            modelLoadingStatus = "FILE_NOT_FOUND",
            availableModels = availableModels
        ))
        
        createFallbackNode(arSceneView, modelPath, "Model file not found") { _, _ ->
            // 모델 파일이 없는 경우이므로 상태 업데이트는 생략
        }
        return
    }
    
    // 초기 디버그 정보 업데이트
    onDebugInfoUpdate(ARDebugInfo(
        targetLatitude = latitude,
        targetLongitude = longitude,
        anchorMethod = "INITIALIZING",
        modelLoadingStatus = "MODEL_FOUND",
        availableModels = availableModels
    ))
    
    // Earth tracking이 안정화되기까지 대기 (90프레임 = 약 3초)
    var waitFrameCount = 0
    var hasTriedTerrainAnchor = false
    
    // 현재 앵커 상태 추적 (mutable state로 관리)
    var currentAnchorType = "NONE" // 실제 사용 중인 앵커 타입
    var localModelNode: ModelNode? = null // 로컬 ModelNode 참조
    var localAnimationPlayed = false // 로컬 애니메이션 상태
    
    // 상태 업데이트 함수 (중앙화된 상태 관리)
    fun updateAnchorState(anchorType: String, modelNode: ModelNode?) {
        currentAnchorType = anchorType
        localModelNode = modelNode
        localAnimationPlayed = false // 새 객체이므로 애니메이션 상태 초기화
        onModelNodeUpdate(modelNode) // 컴포넌트 레벨로 상태 전달
        Log.d("ARScreen", "Anchor state updated: $anchorType, Model: ${modelNode != null}, Animation: $localAnimationPlayed")
    }
    
    // 현재 애니메이션 상태 확인 함수 (중앙화된 상태 관리)
    fun getAnimationStatus(): String {
        return when {
            localModelNode == null -> "NONE"
            localAnimationPlayed -> "PLAYED"
            else -> "READY"
        }
    }
    
    
    arSceneView.onFrame = { frame ->
        frameCount++
        waitFrameCount++
        
        // 매 프레임마다 Geospatial API 상태 확인 및 디버그 정보 업데이트
        try {
            val earth = session.earth
            val (gpsEnabled, networkEnabled, locationServicesEnabled) = checkLocationServicesStatus(arSceneView.context)
            
            val debugInfo = if (earth != null) {
                val cameraGeospatialPose = earth.cameraGeospatialPose
                val planes = session.getAllTrackables(Plane::class.java)
                val horizontalPlanes = planes.filter { 
                    it.type == Plane.Type.HORIZONTAL_UPWARD_FACING && 
                    it.trackingState == TrackingState.TRACKING 
                }
                
                ARDebugInfo(
                    currentLatitude = cameraGeospatialPose.latitude,
                    currentLongitude = cameraGeospatialPose.longitude,
                    targetLatitude = latitude,
                    targetLongitude = longitude,
                    gpsAccuracy = cameraGeospatialPose.horizontalAccuracy,
                    earthTrackingState = earth.trackingState.name,
                    anchorMethod = when {
                        !hasTriedTerrainAnchor && waitFrameCount < 90 -> "WAITING_FOR_GPS (${90 - waitFrameCount})"
                        currentAnchorType == "NONE" -> "CREATING_FALLBACK"
                        currentAnchorType == "FALLBACK_FIXED" && horizontalPlanes.isNotEmpty() -> "UPGRADING_TO_PLANE"
                        earth.trackingState == TrackingState.TRACKING && cameraGeospatialPose.horizontalAccuracy <= 10.0f -> "TERRAIN_ANCHOR_READY"
                        else -> "STABLE"
                    },
                    actualAnchorType = currentAnchorType, // 실제 사용 중인 앵커 타입
                    isSessionInitialized = true,
                    geospatialApiStatus = if (earth.trackingState == TrackingState.TRACKING) "ENABLED_TRACKING" else "ENABLED_${earth.trackingState.name}",
                    modelLoadingStatus = "LOADED",
                    terrainAnchorState = "READY",
                    availableModels = availableModels,
                    frameCount = frameCount,
                    gpsEnabled = gpsEnabled,
                    locationServicesEnabled = locationServicesEnabled,
                    networkConnected = networkEnabled,
                    googlePlayServicesAvailable = true,
                    planesDetected = horizontalPlanes.size,
                    planeAnchorUsed = currentAnchorType.contains("PLANE"),
                    animationStatus = getAnimationStatus()
                )
            } else {
                ARDebugInfo(
                    targetLatitude = latitude,
                    targetLongitude = longitude,
                    earthTrackingState = "EARTH_NULL",
                    anchorMethod = when {
                        !hasTriedTerrainAnchor && waitFrameCount < 90 -> "WAITING_FOR_GPS (${90 - waitFrameCount})"
                        currentAnchorType == "NONE" -> "CREATING_FALLBACK"
                        else -> "STABLE"
                    },
                    actualAnchorType = currentAnchorType, // 실제 사용 중인 앵커 타입
                    isSessionInitialized = true,
                    geospatialApiStatus = "DISABLED",
                    modelLoadingStatus = "LOADED",
                    availableModels = availableModels,
                    frameCount = frameCount,
                    gpsEnabled = gpsEnabled,
                    locationServicesEnabled = locationServicesEnabled,
                    networkConnected = networkEnabled,
                    googlePlayServicesAvailable = true,
                    planesDetected = 0,
                    planeAnchorUsed = currentAnchorType.contains("PLANE"),
                    animationStatus = getAnimationStatus()
                )
            }
            
            // 30프레임마다 디버그 정보 업데이트 (성능 고려)
            if (frameCount % 30 == 0L) {
                onDebugInfoUpdate(debugInfo)
            }
            
            // 90프레임 후에 Terrain Anchor 시도
            if (waitFrameCount >= 90 && !hasTriedTerrainAnchor) {
                hasTriedTerrainAnchor = true
                
                // 1단계: Terrain Anchor 시도 (Geospatial API 사용)
                val terrainAnchorSuccess = tryCreateTerrainAnchor(
                    arSceneView, session, latitude, longitude, modelPath, onDebugInfoUpdate
                ) { anchorType, modelNode ->
                    if (anchorType == "TERRAIN_ANCHOR" && modelNode != null) {
                        updateAnchorState(anchorType, modelNode)
                    }
                }
                if (terrainAnchorSuccess) {
                    Log.i("ARScreen", "Successfully created Terrain Anchor at GPS($latitude, $longitude)")
                }
            }
            
            // Terrain Anchor 시도 후 즉시 Fallback (평면 감지 시 자동 업그레이드)
            if (waitFrameCount >= 90 && hasTriedTerrainAnchor && currentAnchorType == "NONE") {
                // Terrain Anchor가 실패했으므로 즉시 Fallback 생성
                Log.i("ARScreen", "Creating fallback anchor - Terrain Anchor not available")
                onDebugInfoUpdate(ARDebugInfo(
                    targetLatitude = latitude,
                    targetLongitude = longitude,
                    anchorMethod = "FALLBACK_FIXED",
                    actualAnchorType = "FALLBACK_FIXED",
                    modelLoadingStatus = "USING_FALLBACK",
                    geospatialApiStatus = "FALLBACK_MODE",
                    availableModels = availableModels
                ))
                createFallbackNode(arSceneView, modelPath, "Terrain Anchor unavailable") { anchorType, modelNode ->
                    if (anchorType == "FALLBACK_FIXED" && modelNode != null) {
                        updateAnchorState(anchorType, modelNode)
                    }
                }
            }
            
            // Fallback에서 Plane Anchor로 업그레이드 시도 (평면이 감지된 경우)
            if (currentAnchorType == "FALLBACK_FIXED" && localModelNode != null && waitFrameCount > 120) {
                val upgraded = tryUpgradeToPlaneAnchor(
                    arSceneView, session, localModelNode!!, onDebugInfoUpdate
                ) { anchorType, modelNode ->
                    if (anchorType == "PLANE_ADJUSTED" && modelNode != null) {
                        updateAnchorState(anchorType, modelNode)
                    }
                }
                
                if (upgraded) {
                    Log.i("ARScreen", "Successfully upgraded Fallback to Plane Anchor")
                }
            }
            
            if (earth != null) {
                val cameraGeospatialPose = earth.cameraGeospatialPose
                
                // 상세한 진단 로깅 (Earth tracking 상태가 변경될 때만)
                if (frameCount % 60 == 0L) { // 2초마다 상세 로그
                    Log.d("ARScreen", "=== Earth Tracking Diagnosis ===")
                    Log.d("ARScreen", "Earth tracking state: ${earth.trackingState}")
                    Log.d("ARScreen", "GPS accuracy: ${cameraGeospatialPose.horizontalAccuracy}m")
                    Log.d("ARScreen", "Current position: ${cameraGeospatialPose.latitude}, ${cameraGeospatialPose.longitude}")
                    Log.d("ARScreen", "Altitude: ${cameraGeospatialPose.altitude}m")
                    Log.d("ARScreen", "GPS enabled: $gpsEnabled, Location services: $locationServicesEnabled, Network: $networkEnabled")
                    Log.d("ARScreen", "Wait frames: $waitFrameCount/60, Has tried terrain anchor: $hasTriedTerrainAnchor")
                    
                    when (earth.trackingState) {
                        TrackingState.STOPPED -> {
                            Log.w("ARScreen", "Earth tracking STOPPED - possible causes:")
                            Log.w("ARScreen", "  - Location services disabled: ${!locationServicesEnabled}")
                            Log.w("ARScreen", "  - GPS disabled: ${!gpsEnabled}")
                            Log.w("ARScreen", "  - Network location disabled: ${!networkEnabled}")
                            Log.w("ARScreen", "  - Insufficient permissions or Google Play Services issue")
                        }
                        TrackingState.PAUSED -> {
                            Log.i("ARScreen", "Earth tracking PAUSED - initializing or temporary loss")
                        }
                        TrackingState.TRACKING -> {
                            Log.i("ARScreen", "Earth tracking ACTIVE - GPS accuracy: ${cameraGeospatialPose.horizontalAccuracy}m")
                        }
                    }
                    Log.d("ARScreen", "=== End Diagnosis ===")
                }
                
                Log.v("ARScreen", "GPS accuracy: ${cameraGeospatialPose.horizontalAccuracy}m, " +
                        "Earth tracking: ${earth.trackingState}")
            } else {
                if (frameCount % 60 == 0L) {
                    Log.e("ARScreen", "Earth object is NULL - Geospatial API not properly initialized")
                    Log.e("ARScreen", "  - Check ARCore installation and Google Play Services")
                    Log.e("ARScreen", "  - GPS enabled: $gpsEnabled, Location services: $locationServicesEnabled")
                    Log.e("ARScreen", "  - Wait frames: $waitFrameCount/60, Has tried terrain anchor: $hasTriedTerrainAnchor")
                }
            }
        } catch (e: Exception) {
            Log.v("ARScreen", "Earth tracking check failed: ${e.message}")
            
            // 오류 상태도 디버그 정보로 업데이트
            if (frameCount % 30 == 0L) {
                onDebugInfoUpdate(ARDebugInfo(
                    targetLatitude = latitude,
                    targetLongitude = longitude,
                    earthTrackingState = "ERROR: ${e.message}",
                    anchorMethod = if (!hasTriedTerrainAnchor && waitFrameCount < 90) "WAITING_FOR_GPS (${90 - waitFrameCount})" else "ERROR_FALLBACK",
                    actualAnchorType = currentAnchorType,
                    isSessionInitialized = true,
                    geospatialApiStatus = "ERROR",
                    modelLoadingStatus = "ERROR",
                    availableModels = availableModels,
                    frameCount = frameCount
                ))
            }
        }
    }
}

// Geospatial API를 통한 Terrain Anchor 생성 시도
private fun tryCreateTerrainAnchor(
    arSceneView: ARSceneView,
    session: Session,
    latitude: Double,
    longitude: Double,
    modelPath: String,
    onDebugInfoUpdate: (ARDebugInfo) -> Unit,
    onAnchorTypeChange: (String, ModelNode?) -> Unit
): Boolean {
    try {
        val earth = session.earth
        if (earth == null) {
            Log.w("ARScreen", "Earth is null - Geospatial API not available")
            return false
        }
        
        // Earth tracking 상태 확인
        if (earth.trackingState != TrackingState.TRACKING) {
            Log.w("ARScreen", "Earth tracking state: ${earth.trackingState} - not ready for anchors")
            return false
        }
        
        // GPS 정확도 확인 (10m 이내)
        val cameraGeospatialPose = earth.cameraGeospatialPose
        val horizontalAccuracy = cameraGeospatialPose.horizontalAccuracy
        
        if (horizontalAccuracy > 10.0f) {
            Log.w("ARScreen", "GPS accuracy too low: ${horizontalAccuracy}m (required: <10m)")
            return false
        }
        
        Log.i("ARScreen", "GPS accuracy: ${horizontalAccuracy}m - creating Terrain Anchor")
        
        // 모델 인스턴스 먼저 생성
        val modelInstance = arSceneView.modelLoader.createModelInstance(modelPath)
        if (modelInstance == null) {
            Log.w("ARScreen", "Failed to create model instance for terrain anchor")
            return false
        }
        
        // Terrain Anchor 비동기 생성 (최신 방식)
        val terrainFuture = earth.resolveAnchorOnTerrainAsync(
            latitude,
            longitude,
            0.0,  // altitude relative to terrain
            0.0f, 0.0f, 0.0f, 1.0f  // quaternion (no rotation)
        ) { anchor, terrainAnchorState ->
            // 콜백에서 처리
            when (terrainAnchorState) {
                com.google.ar.core.Anchor.TerrainAnchorState.SUCCESS -> {
                    try {
                        // AnchorNode 생성 (Terrain Anchor 사용)
                        val anchorNode = AnchorNode(
                            engine = arSceneView.engine,
                            anchor = anchor
                        )
                        
                        // ModelNode 생성 및 AnchorNode에 추가
                        val modelNode = ModelNode(
                            modelInstance = modelInstance,
                            scaleToUnits = 0.5f
                        ).apply {
                            // Terrain Anchor는 바닥에 붙게 배치 (모델 중심점이 중앙이므로 아래로 이동)
                            position = Position(0.0f, 0.0f, 0.0f)
                            // Y축(수직축) 랜덤 회전 (0-360도)
                            rotation = Rotation(0f, Random.nextFloat() * 360f, 0f)
                            // 모든 애니메이션 정지
                            stopAnimation(0)
                        }
                        
                        anchorNode.addChildNode(modelNode)
                        arSceneView.addChildNode(anchorNode)
                        
                        // 앵커 타입 변경 알림
                        onAnchorTypeChange("TERRAIN_ANCHOR", modelNode)
                        
                        Log.d("ARScreen", "Terrain Anchor model loaded - ready for interaction")
                        
                        Log.i("ARScreen", "Terrain Anchor created successfully at GPS($latitude, $longitude)")
                        
                        // 성공 상태 디버그 정보 업데이트
                        onDebugInfoUpdate(ARDebugInfo(
                            targetLatitude = latitude,
                            targetLongitude = longitude,
                            anchorMethod = "TERRAIN_ANCHOR_SUCCESS",
                            actualAnchorType = "TERRAIN_ANCHOR",
                            modelLoadingStatus = "LOADED_WITH_TERRAIN_ANCHOR",
                            terrainAnchorState = "SUCCESS",
                            geospatialApiStatus = "TERRAIN_ANCHOR_CREATED"
                        ))
                        
                    } catch (e: Exception) {
                        Log.e("ARScreen", "Error setting up model with terrain anchor", e)
                        
                        // 오류 상태 디버그 정보 업데이트
                        onDebugInfoUpdate(ARDebugInfo(
                            targetLatitude = latitude,
                            targetLongitude = longitude,
                            anchorMethod = "TERRAIN_ANCHOR_ERROR",
                            modelLoadingStatus = "ERROR_SETTING_UP_MODEL",
                            terrainAnchorState = "ERROR: ${e.message}",
                            geospatialApiStatus = "MODEL_SETUP_FAILED"
                        ))
                    }
                }
                
                else -> {
                    Log.w("ARScreen", "Terrain anchor failed with state: $terrainAnchorState")
                    
                    // 실패 상태 디버그 정보 업데이트
                    onDebugInfoUpdate(ARDebugInfo(
                        targetLatitude = latitude,
                        targetLongitude = longitude,
                        anchorMethod = "TERRAIN_ANCHOR_FAILED",
                        modelLoadingStatus = "TERRAIN_ANCHOR_FAILED",
                        terrainAnchorState = terrainAnchorState.name,
                        geospatialApiStatus = "TERRAIN_ANCHOR_FAILED"
                    ))
                }
            }
        }
        
        if (terrainFuture == null) {
            Log.w("ARScreen", "Failed to initiate terrain anchor creation")
            return false
        }
        
        Log.i("ARScreen", "Terrain Anchor creation initiated at GPS($latitude, $longitude)")
        return true
        
    } catch (e: Exception) {
        Log.e("ARScreen", "Error creating terrain anchor", e)
        return false
    }
}

// 평면 기반 Anchor 생성 시도
private fun tryCreatePlaneAnchor(
    arSceneView: ARSceneView,
    session: Session,
    modelPath: String,
    onDebugInfoUpdate: (ARDebugInfo) -> Unit,
    onAnchorTypeChange: (String, ModelNode?) -> Unit
): Boolean {
    try {
        // 모든 감지된 평면 가져오기
        val planes = session.getAllTrackables(Plane::class.java)
        val horizontalPlanes = planes.filter { 
            it.type == Plane.Type.HORIZONTAL_UPWARD_FACING && 
            it.trackingState == TrackingState.TRACKING 
        }
        
        Log.d("ARScreen", "Detected ${planes.size} total planes, ${horizontalPlanes.size} horizontal planes")
        
        if (horizontalPlanes.isEmpty()) {
            Log.w("ARScreen", "No horizontal planes detected for anchor placement")
            onDebugInfoUpdate(ARDebugInfo(
                anchorMethod = "NO_PLANES_DETECTED",
                modelLoadingStatus = "WAITING_FOR_PLANES",
                planesDetected = planes.size
            ))
            return false
        }
        
        // 카메라에서 가장 가까운 평면 찾기 (첫 번째 평면 선택으로 단순화)
        val nearestPlane = horizontalPlanes.firstOrNull()
        
        if (nearestPlane == null) {
            Log.w("ARScreen", "Failed to find nearest plane")
            return false
        }
        
        // 평면 중심에 Anchor 생성
        val planePose = nearestPlane.centerPose
        val planeAnchor = nearestPlane.createAnchor(planePose)
        
        // 모델 인스턴스 생성
        val modelInstance = arSceneView.modelLoader.createModelInstance(modelPath)
        if (modelInstance == null) {
            Log.w("ARScreen", "Failed to create model instance for plane anchor")
            planeAnchor.detach()
            return false
        }
        
        // AnchorNode 생성
        val anchorNode = AnchorNode(
            engine = arSceneView.engine,
            anchor = planeAnchor
        )
        
        // ModelNode 생성 및 배치
        val modelNode = ModelNode(
            modelInstance = modelInstance,
            scaleToUnits = 0.5f
        ).apply {
            // 평면 위에 바닥에 붙게 배치 (모델 중심점이 중앙이므로 아래로 이동)
            position = Position(0.0f, 0.0f, 0.0f)
            // Y축(수직축) 랜덤 회전
            rotation = Rotation(0f, Random.nextFloat() * 360f, 0f)
            // 모든 애니메이션 정지
            stopAnimation(0)
        }
        
        anchorNode.addChildNode(modelNode)
        arSceneView.addChildNode(anchorNode)
        
        // 앵커 타입 변경 알림
        onAnchorTypeChange("PLANE_ANCHOR", modelNode)
        
        Log.d("ARScreen", "Plane Anchor model loaded - ready for interaction")
        
        Log.i("ARScreen", "Plane-based anchor created successfully on detected ground plane")
        
        // 성공 상태 디버그 정보 업데이트
        onDebugInfoUpdate(ARDebugInfo(
            anchorMethod = "PLANE_ANCHOR_SUCCESS",
            actualAnchorType = "PLANE_ANCHOR",
            modelLoadingStatus = "LOADED_WITH_PLANE_ANCHOR",
            planesDetected = planes.size,
            planeAnchorUsed = true
        ))
        
        return true
        
    } catch (e: Exception) {
        Log.e("ARScreen", "Error creating plane-based anchor", e)
        onDebugInfoUpdate(ARDebugInfo(
            anchorMethod = "PLANE_ANCHOR_ERROR",
            modelLoadingStatus = "PLANE_ANCHOR_FAILED"
        ))
        return false
    }
}

// Fallback에서 Plane Anchor로 부드러운 전환 (XZ 좌표 유지, Y만 조정)
private fun tryUpgradeToPlaneAnchor(
    arSceneView: ARSceneView,
    session: Session,
    currentModelNode: ModelNode,
    onDebugInfoUpdate: (ARDebugInfo) -> Unit,
    onAnchorTypeChange: (String, ModelNode?) -> Unit
): Boolean {
    try {
        // 모든 감지된 평면 가져오기
        val planes = session.getAllTrackables(Plane::class.java)
        val horizontalPlanes = planes.filter { 
            it.type == Plane.Type.HORIZONTAL_UPWARD_FACING && 
            it.trackingState == TrackingState.TRACKING 
        }
        
        if (horizontalPlanes.isEmpty()) {
            return false
        }
        
        // 현재 객체 위치 가져오기
        val currentPosition = currentModelNode.position
        
        // 현재 객체와 가장 가까운 평면 찾기
        var nearestPlane: Plane? = null
        var minDistance = Float.MAX_VALUE
        
        for (plane in horizontalPlanes) {
            val planePose = plane.centerPose
            val planeX = planePose.translation[0]
            val planeZ = planePose.translation[2]
            
            // XZ 평면에서의 거리 계산 (Y는 제외)
            val distance = kotlin.math.sqrt(
                ((currentPosition.x - planeX) * (currentPosition.x - planeX) + 
                 (currentPosition.z - planeZ) * (currentPosition.z - planeZ)).toDouble()
            ).toFloat()
            
            if (distance < minDistance) {
                minDistance = distance
                nearestPlane = plane
            }
        }
        
        if (nearestPlane == null) {
            return false
        }
        
        // 가장 가까운 평면의 높이 계산
        val planePose = nearestPlane.centerPose
        val planeY = planePose.translation[1]
        
        // 기존 XZ 좌표 유지, Y만 평면 높이로 조정 (모델 중심점 고려해서 아래로)
        val newPosition = Position(
            currentPosition.x, 
            planeY,
            currentPosition.z
        )
        
        // 부드럽게 위치 업데이트
        currentModelNode.position = newPosition
        
        Log.i("ARScreen", "Successfully upgraded Fallback to Plane Anchor - Y adjusted from ${currentPosition.y} to ${newPosition.y}")
        
        // 앵커 타입 변경 알림
        onAnchorTypeChange("PLANE_ADJUSTED", currentModelNode)
        
        // 성공 상태 디버그 정보 업데이트
        onDebugInfoUpdate(ARDebugInfo(
            anchorMethod = "PLANE_ADJUSTED",
            actualAnchorType = "PLANE_ADJUSTED",
            modelLoadingStatus = "UPGRADED_TO_PLANE",
            planesDetected = planes.size,
            planeAnchorUsed = true
        ))
        
        return true
        
    } catch (e: Exception) {
        Log.e("ARScreen", "Error upgrading to plane anchor", e)
        return false
    }
}

// Fallback: 고정 위치에 객체 배치 (평면 감지 실패 시)
private fun createFallbackNode(
    arSceneView: ARSceneView, 
    modelPath: String, 
    reason: String,
    onAnchorTypeChange: (String, ModelNode?) -> Unit
) {
    try {
        Log.i("ARScreen", "Creating fallback node - reason: $reason")
        
        val modelInstance = arSceneView.modelLoader.createModelInstance(modelPath)
        if (modelInstance != null) {
            val modelNode = ModelNode(
                modelInstance = modelInstance,
                scaleToUnits = 0.5f
            ).apply {
                // 사용자 앞 2미터, 바닥에 붙게 배치 (모델 중심점이 중앙이므로 아래로 이동)
                position = Position(0.0f, -1.5f, -2.0f)
                // Y축(수직축) 랜덤 회전 (0-360도)
                rotation = Rotation(0f, Random.nextFloat() * 360f, 0f)
                // 모든 애니메이션 정지
                stopAnimation(0)
            }
            
            arSceneView.addChildNode(modelNode)
            Log.i("ARScreen", "Fallback model loaded at fixed position")
            
            // 앵커 타입 변경 알림
            onAnchorTypeChange("FALLBACK_FIXED", modelNode)
            
            Log.d("ARScreen", "Fallback model loaded - ready for interaction")
        } else {
            Log.w("ARScreen", "Failed to create fallback model instance")
            createPrimitiveNode(arSceneView)
        }
        
    } catch (e: Exception) {
        Log.e("ARScreen", "Error creating fallback node", e)
        createPrimitiveNode(arSceneView)
    }
}

// 기본 프리미티브 노드 생성 (최종 대체용)
private fun createPrimitiveNode(arSceneView: ARSceneView) {
    try {
        Log.i("ARScreen", "Creating primitive fallback node")
        
        // 동기적으로 기본 큐브 모델 생성 시도
        val fallbackPath = "models/cube.gltf"
        try {
            val modelInstance = arSceneView.modelLoader.createModelInstance(fallbackPath)
            if (modelInstance != null) {
                val cubeNode = ModelNode(
                    modelInstance = modelInstance,
                    scaleToUnits = 0.5f
                ).apply {
                    position = Position(0.0f, 0.0f, -2.0f)
                    // 모든 애니메이션 정지
                    stopAnimation(0)
                }
                arSceneView.addChildNode(cubeNode)
                Log.i("ARScreen", "Primitive fallback cube model loaded")
            } else {
                Log.w("ARScreen", "Failed to create primitive fallback model instance")
            }
        } catch (e: Exception) {
            Log.w("ARScreen", "Error creating primitive fallback cube node", e)
        }
    } catch (e: Exception) {
        Log.w("ARScreen", "Primitive fallback model creation skipped: ${e.message}")
        // 모델 없이도 AR 세션은 정상 동작
    }
}