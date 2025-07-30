package com.example.bogoargo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.ar.core.*
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import com.example.bogoargo.ui.components.DebugInfoCard

/**
 * AR 미션을 수행하는 메인 화면
 * SceneView를 사용하여 실시간 AR 환경을 제공하고, 사용자가 3D 객체를 배치할 수 있는 기능 제공
 *
 * @param spotId 미션 스팟의 고유 ID
 * @param onNavigateBack 뒤로 가기 버튼 클릭 시 호출되는 콜백 함수
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARScreen(
    spotId: Long,
    latitude: Double,
    longitude: Double,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // AR 관련 상태 변수들
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasCoarseLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasFineLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var needsPreciseLocation by remember { mutableStateOf(false) }
    var missionCompleted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // SceneView 상태
    var arSceneView by remember { mutableStateOf<ArSceneView?>(null) }
    var modelNode by remember { mutableStateOf<ArModelNode?>(null) }
    var isEarthTracking by remember { mutableStateOf(false) }
    var currentLatitude by remember { mutableStateOf(0.0) }
    var currentLongitude by remember { mutableStateOf(0.0) }
    var currentAltitude by remember { mutableStateOf(0.0) }
    var currentAccuracy by remember { mutableStateOf(0.0) }
    var geospatialError by remember { mutableStateOf<String?>(null) }
    var distanceToObject by remember { mutableStateOf<Float?>(null) }
    var lastVibrationDistance by remember { mutableStateOf<Float?>(null) }
    var showDebugInfo by remember { mutableStateOf(false) }

    // 진동 서비스
    val vibrator = remember { context.getSystemService(Vibrator::class.java) }

    // 권한 요청을 위한 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: false
        hasCoarseLocationPermission =
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        hasFineLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false

        hasLocationPermission = hasCoarseLocationPermission || hasFineLocationPermission

        if (hasCoarseLocationPermission && !hasFineLocationPermission) {
            needsPreciseLocation = true
        }
    }

    // 위치 권한 상태 업데이트
    LaunchedEffect(hasCoarseLocationPermission, hasFineLocationPermission) {
        hasLocationPermission = hasCoarseLocationPermission || hasFineLocationPermission
        if (hasCoarseLocationPermission && !hasFineLocationPermission) {
            needsPreciseLocation = true
        }
    }

    // 거리 계산 함수
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val earthRadius = 6371000f // 지구 반지름 (미터)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return (earthRadius * c).toFloat()
    }

    // Material 3 기반 UI 구성
    Scaffold(
        topBar = {
            // 상단 앱바 - 제목과 뒤로 가기 버튼
            TopAppBar(
                title = { Text("AR 미션") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!hasCameraPermission || !hasLocationPermission) {
                // 권한이 없는 경우 - 권한 요청 UI 표시
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (!hasCameraPermission && !hasLocationPermission) "권한이 필요합니다"
                        else if (!hasCameraPermission) "카메라 권한이 필요합니다"
                        else "위치 권한이 필요합니다",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!hasCameraPermission) {
                        Text(
                            text = "• 카메라: AR 환경을 보기 위해 필요합니다.",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (!hasLocationPermission) {
                        Text(
                            text = "• 위치: 미션 지점에 AR 객체를 배치하기 위해 필요합니다.",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "• 정확한 위치 사용 시 더 정확한 AR 경험을 제공합니다.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            // 권한 요청 실행
                            val permissionsToRequest = mutableListOf<String>()

                            if (!hasCameraPermission) {
                                permissionsToRequest.add(Manifest.permission.CAMERA)
                            }

                            if (!hasLocationPermission) {
                                permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
                            }

                            if (permissionsToRequest.isNotEmpty()) {
                                permissionLauncher.launch(permissionsToRequest.toTypedArray())
                            }
                        }
                    ) {
                        Text("권한 허용")
                    }
                }
            } else if (errorMessage != null) {
                // 에러 발생 시
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "⚠️ AR 오류",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = errorMessage!!,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            errorMessage = null
                        }
                    ) {
                        Text("다시 시도")
                    }
                }
            } else {
                // SceneView AR 화면
                AndroidView(
                    factory = { context ->
                        ArSceneView(context).apply {
                            arSceneView = this
                            
                            // Geospatial API 활성화
                            geospatialEnabled = true
                            
                            // 라이프사이클 수동 관리
                            val lifecycleObserver = LifecycleEventObserver { owner, event ->
                                when (event) {
                                    Lifecycle.Event.ON_RESUME -> onResume(owner)
                                    Lifecycle.Event.ON_PAUSE -> onPause(owner)
                                    Lifecycle.Event.ON_DESTROY -> onDestroy(owner)
                                    else -> {}
                                }
                            }
                            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
                            
                            // AR 프레임 업데이트 콜백
                            onArFrame = { arFrame ->
                                try {
                                    val session = arFrame.session
                                    val earth = session.earth
                                    
                                    if (earth?.trackingState == TrackingState.TRACKING) {
                                        isEarthTracking = true
                                        val pose = earth.cameraGeospatialPose
                                        currentLatitude = pose.latitude
                                        currentLongitude = pose.longitude
                                        currentAltitude = pose.altitude
                                        currentAccuracy = pose.horizontalAccuracy
                                        
                                        // 미션 위치까지의 거리 계산
                                        val distance = calculateDistance(
                                            currentLatitude, currentLongitude,
                                            latitude, longitude
                                        )
                                        distanceToObject = distance
                                        
                                        // 진동 피드백
                                        val shouldVibrate = when {
                                            distance <= 1.0f -> {
                                                lastVibrationDistance?.let { it > 1.0f } ?: true
                                            }
                                            distance <= 2.0f -> {
                                                lastVibrationDistance?.let { it > 2.0f || it <= 1.0f } ?: true
                                            }
                                            else -> false
                                        }
                                        
                                        if (shouldVibrate && vibrator?.hasVibrator() == true) {
                                            val vibrationEffect = when {
                                                distance <= 1.0f -> VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                                                distance <= 2.0f -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                                                else -> null
                                            }
                                            vibrationEffect?.let { vibrator.vibrate(it) }
                                            lastVibrationDistance = distance
                                        }
                                        
                                        // 가까우면 3D 객체 생성 (지형 앵커 사용)
                                        if (distance <= 10.0f && modelNode == null) {
                                            try {
                                                // Terrain 앵커 생성
                                                val anchor = earth.resolveAnchorOnTerrainAsync(
                                                    latitude, longitude, 0.5,
                                                    0f, 0f, 0f, 1f
                                                ) { anchor, state ->
                                                    when (state) {
                                                        Anchor.TerrainAnchorState.SUCCESS -> {
                                                            Log.d("ARScreen", "Terrain anchor created successfully")
                                                            
                                                            // 앵커 성공 시 3D 객체 생성
                                                            val node = ArModelNode().apply {
                                                                this.anchor = anchor
                                                                position = Position(0f, 0f, 0f)
                                                                scale = Position(0.5f, 0.5f, 0.5f)
                                                                
                                                                // 터치 이벤트 설정
                                                                isSelectable = true
                                                                onTap = { _, _ ->
                                                                    if (!missionCompleted) {
                                                                        missionCompleted = true
                                                                        Log.d("ARScreen", "Mission completed!")
                                                                    }
                                                                }
                                                            }
                                                            
                                                            addChild(node)
                                                            modelNode = node
                                                            Log.d("ARScreen", "Model node created with terrain anchor")
                                                        }
                                                        Anchor.TerrainAnchorState.ERROR_UNSUPPORTED_LOCATION -> {
                                                            Log.w("ARScreen", "Terrain anchor not supported, creating fallback")
                                                            // 폴백 노드 즉시 생성
                                                            val fallbackNode = ArModelNode().apply {
                                                                position = Position(0f, 0f, -3f)
                                                                scale = Position(0.4f, 0.4f, 0.4f)
                                                                isSelectable = true
                                                                onTap = { _, _ ->
                                                                    if (!missionCompleted) {
                                                                        missionCompleted = true
                                                                        Log.d("ARScreen", "Mission completed (fallback)!")
                                                                    }
                                                                }
                                                            }
                                                            addChild(fallbackNode)
                                                            modelNode = fallbackNode
                                                            geospatialError = "폴백 모드: 카메라 앞에 객체 배치"
                                                        }
                                                        else -> {
                                                            Log.w("ARScreen", "Terrain anchor failed: $state")
                                                            geospatialError = "지형 앵커 생성 실패: $state"
                                                        }
                                                    }
                                                }
                                                
                                            } catch (e: Exception) {
                                                Log.e("ARScreen", "Failed to create terrain anchor", e)
                                                // 폴백 노드 즉시 생성
                                                val fallbackNode = ArModelNode().apply {
                                                    position = Position(0f, 0f, -3f)
                                                    scale = Position(0.4f, 0.4f, 0.4f)
                                                    isSelectable = true
                                                    onTap = { _, _ ->
                                                        if (!missionCompleted) {
                                                            missionCompleted = true
                                                            Log.d("ARScreen", "Mission completed (fallback)!")
                                                        }
                                                    }
                                                }
                                                addChild(fallbackNode)
                                                modelNode = fallbackNode
                                                geospatialError = "폴백 모드: 카메라 앞에 객체 배치"
                                            }
                                        }
                                        
                                    } else {
                                        isEarthTracking = false
                                        geospatialError = when (earth?.trackingState) {
                                            TrackingState.PAUSED -> "GPS 신호를 찾고 있습니다..."
                                            TrackingState.STOPPED -> "위치 서비스를 사용할 수 없습니다"
                                            else -> "Geospatial API 초기화 중..."
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("ARScreen", "Error in AR frame update", e)
                                    geospatialError = "AR 업데이트 오류: ${e.message}"
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 디버그 토글 버튼 (좌측 상단)
                Button(
                    onClick = { showDebugInfo = !showDebugInfo },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showDebugInfo) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = "DEBUG",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 거리 정보 표시 (화면 상단 중앙)
                distanceToObject?.let { distance ->
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 80.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                distance <= 2.0f -> androidx.compose.ui.graphics.Color.Green.copy(alpha = 0.9f)
                                distance <= 5.0f -> androidx.compose.ui.graphics.Color.Blue.copy(alpha = 0.9f)
                                else -> androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.9f)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📏 목표까지 거리",
                                fontSize = 12.sp,
                                color = androidx.compose.ui.graphics.Color.White,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", distance)}m",
                                fontSize = 20.sp,
                                color = androidx.compose.ui.graphics.Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when {
                                    distance <= 1.0f -> "매우 가까움"
                                    distance <= 2.0f -> "가까움"
                                    distance <= 5.0f -> "보통"
                                    distance <= 10.0f -> "멀음"
                                    else -> "매우 멀음"
                                },
                                fontSize = 10.sp,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // 디버그 정보 표시 (조건부)
                if (showDebugInfo) {
                    DebugInfoCard(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        spotId = spotId.toString(),
                        latitude = latitude,
                        longitude = longitude,
                        distanceToObject = distanceToObject,
                        geospatialError = geospatialError,
                        terrainAnchorError = false,
                        isFallbackMode = false,
                        hasCameraPermission = hasCameraPermission,
                        hasFineLocationPermission = hasFineLocationPermission,
                        hasCoarseLocationPermission = hasCoarseLocationPermission,
                        isArSessionReady = true,
                        isEarthTracking = isEarthTracking,
                        currentLatitude = currentLatitude,
                        currentLongitude = currentLongitude,
                        currentAltitude = currentAltitude,
                        currentAccuracy = currentAccuracy
                    )
                } else {
                    // 기본 미션 상태 카드
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "미션 스팟 ID: $spotId",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            // 미션 상태에 따른 메시지 표시
                            Text(
                                text = when {
                                    missionCompleted -> "🎉 미션 완료!"
                                    distanceToObject != null -> {
                                        val distance = distanceToObject!!
                                        when {
                                            distance <= 2.0f -> "✅ 터치하여 미션 완료!"
                                            distance <= 5.0f -> "🚶 조금 더 가까이 가세요"
                                            distance <= 10.0f -> "🏃 객체 방향으로 이동하세요"
                                            else -> "🔍 객체를 찾아 이동하세요"
                                        }
                                    }
                                    else -> "🎯 AR 객체를 찾아보세요"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    missionCompleted -> androidx.compose.ui.graphics.Color.Green
                                    distanceToObject != null && distanceToObject!! <= 2.0f -> androidx.compose.ui.graphics.Color.Green
                                    distanceToObject != null && distanceToObject!! <= 5.0f -> androidx.compose.ui.graphics.Color.Blue
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }

                // 미션 완료 버튼
                if (missionCompleted) {
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .offset(y = (-80).dp)
                    ) {
                        Text("미션 완료")
                    }
                }

                // 정확한 위치 업그레이드 안내
                if (needsPreciseLocation && hasCoarseLocationPermission && !hasFineLocationPermission) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🎯 더 정확한 AR 경험",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "정확한 위치 권한을 허용하면 더 정밀한 AR 객체 배치가 가능합니다.",
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("정확한 위치 사용")
                            }

                            TextButton(
                                onClick = {
                                    needsPreciseLocation = false
                                }
                            ) {
                                Text("나중에 하기", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}