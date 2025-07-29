package com.example.bogoargo.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
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
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasCoarseLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasFineLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
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
    
    // 권한 요청을 위한 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: false
        hasCoarseLocationPermission = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
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
    
    // 카메라와 위치 권한이 허용되면 ARCore 세션 초기화
    LaunchedEffect(hasCameraPermission, hasLocationPermission) {
        if (hasCameraPermission && hasLocationPermission) {
            try {
                // ARCore 설치 상태 확인 (더 안전한 방식)
                val installStatus = ArCoreApk.getInstance().requestInstall(context as Activity, false)
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
                val isGeospatialSupported = session.isGeospatialModeSupported(Config.GeospatialMode.ENABLED)
                
                if (!isGeospatialSupported) {
                    errorMessage = "이 기기는 Geospatial API를 지원하지 않습니다."
                    return@LaunchedEffect
                }
                
                val config = Config(session).apply {
                    // 평면 감지 활성화
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                    // 최신 카메라 이미지를 사용하여 업데이트
                    updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                    // 광원 추정 활성화
                    lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                    // Geospatial API 활성화
                    geospatialMode = Config.GeospatialMode.ENABLED
                }
                session.configure(config)
                
                // VPS 가용성 체크
                try {
                    session.checkVpsAvailabilityAsync(latitude, longitude) { availability ->
                        when (availability.toString()) {
                            "UNAVAILABLE" -> {
                                errorMessage = "이 지역에서는 정밀 위치 서비스를 사용할 수 없습니다."
                            }
                        }
                    }
                } catch (e: Exception) {
                    // VPS availability check failed
                }
                
                arSession = session
                isArSessionReady = true
                errorMessage = null
                
            } catch (e: Exception) {
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
            }
        }
    }
    
    // 컴포넌트가 해제될 때 ARCore 세션 정리
    DisposableEffect(Unit) {
        onDispose {
            arSession?.close()  // 메모리 누수 방지를 위해 세션 종료
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로 가기")
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
                                // 재시도 버튼
                                errorMessage = null
                                isArSessionReady = false
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
                        // 커스텀 ARCameraView 생성
                        val arCameraView = ARCameraView(
                            context = context,
                            session = arSession!!,
                            targetLatitude = latitude,
                            targetLongitude = longitude,
                            onMissionComplete = { completed ->
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
                            }
                        }
                        // ARCameraView를 즉시 시작
                        arCameraView.onResume()
                        arCameraView
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // 화면 하단 AR 상태 정보 오버레이
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    // 미션 정보 카드
                    Card(
                        colors = CardDefaults.cardColors(
                            // 반투명 배경으로 AR 화면이 보이도록 설정
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
                            
                            Text(
                                text = "목표 위치: ${String.format("%.6f, %.6f", latitude, longitude)}",
                                fontSize = 12.sp
                            )
                            
                            if (geospatialError != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "⚠️ $geospatialError",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // 미션 상태에 따른 메시지 표시
                            Text(
                                text = if (missionCompleted) "🎉 미션 완료!" else "🎯 AR 객체를 찾아보세요",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 미션 완료 시에만 완료 버튼 표시
                    if (missionCompleted) {
                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("미션 완료")
                        }
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
                } else {
                    // 기존 AR 사용법 안내 텍스트
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📱 AR 디버그 정보",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "카메라 권한: ${if (hasCameraPermission) "✅" else "❌"}",
                            fontSize = 12.sp
                        )
                        
                        Text(
                            text = "위치 권한: ${when {
                                hasFineLocationPermission -> "✅ 정확한 위치"
                                hasCoarseLocationPermission -> "🟡 대략적 위치"
                                else -> "❌ 권한 없음"
                            }}",
                            fontSize = 12.sp
                        )
                        
                        Text(
                            text = "AR 세션: ${if (isArSessionReady) "✅ 준비됨" else "⏳ 로딩중"}",
                            fontSize = 12.sp
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Geospatial API: ${if (isEarthTracking) "✅ 활성" else "❌ 비활성"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEarthTracking) androidx.compose.ui.graphics.Color.Green else androidx.compose.ui.graphics.Color.Red
                        )
                        
                        if (isEarthTracking) {
                            Text(
                                text = "현재 GPS: ${String.format("%.6f, %.6f", currentLatitude, currentLongitude)}",
                                fontSize = 11.sp
                            )
                            Text(
                                text = "고도: ${String.format("%.1fm", currentAltitude)}, 정확도: ${String.format("%.1fm", currentAccuracy)}",
                                fontSize = 11.sp
                            )
                        } else if (geospatialError != null) {
                            Text(
                                text = "⚠️ $geospatialError",
                                fontSize = 11.sp,
                                color = androidx.compose.ui.graphics.Color.Red
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = if (missionCompleted) "🎉 미션 완료!" else "🎯 객체를 터치하세요",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                }
            }
        }
    }
}