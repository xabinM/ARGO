package com.example.bogoargo.ui.screens

import android.Manifest
import android.app.Activity
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
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import com.example.bogoargo.ui.components.DebugInfoCard

/**
 * AR 미션을 수행하는 메인 화면
 * ARCore를 사용하여 실시간 AR 환경을 제공하고, 사용자가 3D 객체를 배치할 수 있는 기능 제공
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

    // AR 관련 상태 변수들
    var hasCameraPermission by remember {
        mutableStateOf(
            // 앱 시작 시 카메라 권한 상태 확인
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
    var arSession by remember { mutableStateOf<Session?>(null) }        // ARCore 세션 객체
    var isArSessionReady by remember { mutableStateOf(false) }          // AR 세션 준비 완료 여부
    var missionCompleted by remember { mutableStateOf(false) }          // 미션 완료 상태
    var errorMessage by remember { mutableStateOf<String?>(null) }      // 에러 메시지

    // Geospatial 상태
    var isEarthTracking by remember { mutableStateOf(false) }
    var currentLatitude by remember { mutableStateOf(0.0) }
    var currentLongitude by remember { mutableStateOf(0.0) }
    var currentAltitude by remember { mutableStateOf(0.0) }
    var currentAccuracy by remember { mutableStateOf(0.0) }
    var geospatialError by remember { mutableStateOf<String?>(null) }
    var isFallbackMode by remember { mutableStateOf(false) }
    var distanceToObject by remember { mutableStateOf<Float?>(null) }
    var lastVibrationDistance by remember { mutableStateOf<Float?>(null) }
    var terrainAnchorError by remember { mutableStateOf(false) }
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

        // 위치 권한 상태 업데이트
        hasLocationPermission = hasCoarseLocationPermission || hasFineLocationPermission

        // 정확한 위치가 필요한지 확인
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

    // 세션 상태 추적
    var sessionInitialized by remember { mutableStateOf(false) }
    var lastPermissionState by remember { mutableStateOf(Pair(false, false)) }

    // 카메라와 위치 권한이 허용되면 ARCore 세션 초기화 (불필요한 재생성 방지)
    LaunchedEffect(hasCameraPermission, hasLocationPermission) {
        val currentPermissionState = Pair(hasCameraPermission, hasLocationPermission)

        // 권한 상태가 변경되지 않았거나 이미 세션이 초기화된 경우 건너뛰기
        if (currentPermissionState == lastPermissionState && sessionInitialized) {
            Log.d(
                "ARScreen",
                "Permission state unchanged and session already initialized, skipping"
            )
            return@LaunchedEffect
        }

        lastPermissionState = currentPermissionState

        if (hasCameraPermission && hasLocationPermission) {
            try {
                // ARCore 설치 상태 확인 (더 안전한 방식)
                val installStatus =
                    ArCoreApk.getInstance().requestInstall(context as Activity, false)
                when (installStatus) {
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        errorMessage = "ARCore 설치가 필요합니다. 설치 후 다시 시도해주세요."
                        return@LaunchedEffect
                    }

                    ArCoreApk.InstallStatus.INSTALLED -> {
                        // ARCore is installed
                    }
                }

                // ARCore 지원 확인
                val availability = ArCoreApk.getInstance().checkAvailability(context)
                if (!availability.isSupported) {
                    errorMessage = "이 기기는 ARCore를 지원하지 않습니다."
                    return@LaunchedEffect
                }

                // ARCore 세션 생성 및 설정
                val session = Session(context)

                // Geospatial 지원 여부 체크
                val isGeospatialSupported =
                    session.isGeospatialModeSupported(Config.GeospatialMode.ENABLED)

                if (!isGeospatialSupported) {
                    errorMessage = "이 기기는 Geospatial API를 지원하지 않습니다."
                    return@LaunchedEffect
                }

                val config = Config(session).apply {
                    // 평면 감지 활성화 (성능 최적화를 위해 수평만 활성화)
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                    // 업데이트 모드 최적화 - IMU 버퍼 오버플로우 방지
                    updateMode = Config.UpdateMode.BLOCKING
                    // 광원 추정 최적화 - 성능 향상을 위해 낮은 단계로 설정
                    lightEstimationMode = Config.LightEstimationMode.AMBIENT_INTENSITY
                    // Geospatial API 활성화
                    geospatialMode = Config.GeospatialMode.ENABLED
                    // 심도 모드 설정 (성능 최적화)
                    depthMode = Config.DepthMode.DISABLED
                    // 인스턴트 배치 모드 비활성화 (메모리 절약)
                    instantPlacementMode = Config.InstantPlacementMode.DISABLED
                }
                session.configure(config)

                // VPS 가용성 체크
                try {
                    session.checkVpsAvailabilityAsync(latitude, longitude) { availability ->
                        when (availability.toString()) {
                            "UNAVAILABLE" -> {
                                Log.w(
                                    "ARScreen",
                                    "VPS unavailable in this area, will use fallback mode"
                                )
                                // VPS 불가능한 지역에서는 직접 fallback 모드 안내
                                // (실제 fallback은 ARCameraView에서 처리)
                            }

                            "INSUFFICIENT_GPS_SIGNAL" -> {
                                Log.w("ARScreen", "GPS signal insufficient for VPS")
                            }

                            "AVAILABLE" -> {
                                Log.d("ARScreen", "VPS available in this area")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w("ARScreen", "VPS availability check failed", e)
                }

                arSession = session
                isArSessionReady = true
                sessionInitialized = true
                errorMessage = null
                Log.d("ARScreen", "AR session initialized successfully")

            } catch (e: Exception) {
                Log.e("ARScreen", "Failed to initialize AR session", e)
                when (e) {
                    is UnavailableArcoreNotInstalledException -> {
                        errorMessage = "ARCore가 설치되지 않았습니다."
                    }

                    is UnavailableUserDeclinedInstallationException -> {
                        errorMessage = "ARCore 설치가 거부되었습니다."
                    }

                    is UnavailableApkTooOldException -> {
                        errorMessage = "ARCore 버전이 너무 오래되었습니다."
                    }

                    is UnavailableDeviceNotCompatibleException -> {
                        errorMessage = "이 기기는 ARCore와 호환되지 않습니다."
                    }

                    else -> {
                        errorMessage = "AR 초기화 실패: ${e.message}"
                    }
                }
                isArSessionReady = false
                sessionInitialized = false
            }
        } else {
            // 권한이 없으면 세션 정리
            if (sessionInitialized || arSession != null) {
                Log.d("ARScreen", "Permissions revoked, cleaning up session")
                arSession?.close()
                arSession = null
                isArSessionReady = false
                sessionInitialized = false
            }
        }
    }

    // ARCameraView 참조 저장
    var currentArCameraView: ARCameraView? by remember { mutableStateOf(null) }

    // 컴포넌트가 해제될 때 ARCore 세션 정리
    DisposableEffect(Unit) {
        onDispose {
            Log.d("ARScreen", "Disposing AR session and cleaning up resources")

            // ARCameraView 리소스 정리
            currentArCameraView?.cleanup()
            currentArCameraView = null

            // AR 세션 정리
            arSession?.close()  // 메모리 누수 방지를 위해 세션 종료
            arSession = null
            sessionInitialized = false
        }
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
                            // 권한 요청 실행 - Android 가이드라인에 따라 둘 다 요청
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
            } else if (!isArSessionReady || errorMessage != null) {
                // AR 세션이 준비되지 않은 경우 또는 에러 발생 시
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (errorMessage != null) {
                        // 에러 발생 시 에러 메시지 표시
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
                                // 재시도 버튼 - 세션 상태 완전 리셋
                                Log.d("ARScreen", "Retry button clicked, resetting session state")
                                errorMessage = null
                                isArSessionReady = false
                                sessionInitialized = false
                                arSession?.close()
                                arSession = null
                            }
                        ) {
                            Text("다시 시도")
                        }
                    } else {
                        // 로딩 상태
                        CircularProgressIndicator()

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "AR 세션을 준비중입니다...",
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                // AR 준비 완료 - 실제 AR 화면 표시
                AndroidView(
                    factory = { context ->
                        // 커스텀 ARCameraView 생성 (안전성 강화)
                        val arCameraView = ARCameraView(
                            context = context,
                            session = arSession!!,
                            targetLatitude = latitude,
                            targetLongitude = longitude,
                            onMissionComplete = { completed ->
                                Log.d("ARScreen", "Mission completed: $completed")
                                missionCompleted = completed  // 미션 완료 상태 업데이트
                            }
                        ).apply {
                            // Earth 상태 변경 콜백 설정
                            onEarthStateChanged = { tracking, pose ->
                                isEarthTracking = tracking
                                pose?.let {
                                    currentLatitude = it.latitude
                                    currentLongitude = it.longitude
                                    currentAltitude = it.altitude
                                    currentAccuracy = it.horizontalAccuracy
                                }
                            }

                            // Geospatial 에러 콜백 설정
                            onGeospatialError = { error ->
                                geospatialError = error
                                // 폴백 모드 감지 (다양한 폴백 상황 대응)
                                isFallbackMode = error.contains("폴백 모드") ||
                                        error.contains("카메라 전방 모드") ||
                                        error.contains("GPS 고도 기반")
                            }

                            // 거리 업데이트 콜백 설정
                            onDistanceUpdate = { distance ->
                                distanceToObject = distance

                                // 거리 기반 진동 피드백
                                val shouldVibrate = when {
                                    distance <= 1.0f -> {
                                        // 매우 가까우면 강한 진동
                                        lastVibrationDistance?.let { it > 1.0f } ?: true
                                    }

                                    distance <= 2.0f -> {
                                        // 가까우면 약한 진동
                                        lastVibrationDistance?.let { it > 2.0f || it <= 1.0f }
                                            ?: true
                                    }

                                    else -> false
                                }

                                if (shouldVibrate && vibrator?.hasVibrator() == true) {
                                    val vibrationEffect = when {
                                        distance <= 1.0f -> VibrationEffect.createOneShot(
                                            200,
                                            VibrationEffect.DEFAULT_AMPLITUDE
                                        )

                                        distance > 1.0f && distance <= 2.0f -> VibrationEffect.createOneShot(
                                            100,
                                            VibrationEffect.DEFAULT_AMPLITUDE
                                        )

                                        else -> null
                                    }
                                    vibrationEffect?.let { vibrator.vibrate(it) }
                                    lastVibrationDistance = distance
                                }
                            }

                            // 지형 앵커 오류 콜백 설정
                            onTerrainAnchorError = { hasError ->
                                terrainAnchorError = hasError
                            }
                        }

                        // ARCameraView 참조 저장 (리소스 관리를 위해)
                        currentArCameraView = arCameraView

                        // ARCameraView를 안전하게 시작
                        try {
                            Log.d("ARScreen", "Starting ARCameraView")
                            arCameraView.onResume()
                        } catch (e: Exception) {
                            Log.e("ARScreen", "Failed to start ARCameraView", e)
                        }

                        arCameraView
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
                            .padding(top = 80.dp), // TopAppBar 아래쪽에 위치
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                distance <= 2.0f -> androidx.compose.ui.graphics.Color.Green.copy(
                                    alpha = 0.9f
                                )

                                distance <= 5.0f -> androidx.compose.ui.graphics.Color.Blue.copy(
                                    alpha = 0.9f
                                )

                                else -> androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.9f)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📏 객체까지 거리",
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
                        terrainAnchorError = terrainAnchorError,
                        isFallbackMode = isFallbackMode,
                        hasCameraPermission = hasCameraPermission,
                        hasFineLocationPermission = hasFineLocationPermission,
                        hasCoarseLocationPermission = hasCoarseLocationPermission,
                        isArSessionReady = isArSessionReady,
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

                // 미션 완료 버튼 (별도로 표시)
                if (missionCompleted) {
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .offset(y = (-80).dp) // 카드 위쪽에 표시
                    ) {
                        Text("미션 완료")
                    }
                }

                // 정확한 위치 업그레이드 안내 (대략적 위치만 있을 때)
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
                                    // 정확한 위치 권한만 요청
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
