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
    var arSession by remember { mutableStateOf<Session?>(null) }        // ARCore 세션 객체
    var isArSessionReady by remember { mutableStateOf(false) }          // AR 세션 준비 완료 여부
    var missionCompleted by remember { mutableStateOf(false) }          // 미션 완료 상태
    var errorMessage by remember { mutableStateOf<String?>(null) }      // 에러 메시지
    
    // 카메라 권한 요청을 위한 런처
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    // 카메라 권한이 허용되면 ARCore 세션 초기화
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            try {
                // ARCore 설치 상태 확인 (더 안전한 방식)
                val installStatus = ArCoreApk.getInstance().requestInstall(context as Activity, false)
                when (installStatus) {
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        Log.d("ARScreen", "ARCore installation requested")
                        errorMessage = "ARCore 설치가 필요합니다. 설치 후 다시 시도해주세요."
                        return@LaunchedEffect
                    }
                    ArCoreApk.InstallStatus.INSTALLED -> {
                        Log.d("ARScreen", "ARCore is installed, creating session...")
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
                val config = Config(session).apply {
                    // 평면 감지 활성화
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                    // 최신 카메라 이미지를 사용하여 업데이트
                    updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                    // 광원 추정 활성화
                    lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                }
                session.configure(config)
                
                Log.d("ARScreen", "AR Session configured successfully")
                arSession = session
                isArSessionReady = true
                errorMessage = null // 성공 시 에러 메시지 클리어
                
            } catch (e: Exception) {
                Log.e("ARScreen", "Failed to create AR session", e)
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
            if (!hasCameraPermission) {
                // 카메라 권한이 없는 경우 - 권한 요청 UI 표시
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "카메라 권한이 필요합니다",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "AR 미션을 시작하려면 카메라 권한을 허용해주세요.",
                        fontSize = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            // 카메라 권한 요청 실행
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    ) {
                        Text("카메라 권한 허용")
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
                        val arCameraView = ARCameraView(context, arSession!!) { completed ->
                            missionCompleted = completed  // 미션 완료 상태 업데이트
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
                
                // 화면 상단 AR 사용법 안내 텍스트
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
                            text = "AR 세션: ${if (isArSessionReady) "✅ 준비됨" else "⏳ 로딩중"}",
                            fontSize = 12.sp
                        )
                        
                        Text(
                            text = if (missionCompleted) "🎉 미션 완료!" else "🎯 평면을 터치하세요",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}