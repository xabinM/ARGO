package com.example.bogoargo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode

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
                                    // Geospatial API 활성화
                                    config.geospatialMode = Config.GeospatialMode.ENABLED
                                    config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                                    config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                                    isSessionInitialized = true
                                    Log.d("ARScreen", "AR Session initialized for spot $spotId at $latitude, $longitude")
                                }
                                
                                // AR 세션 콜백 설정
                                onSessionCreated = { session ->
                                    try {
                                        setupARScene(this, session, spotId, latitude, longitude) {
                                            missionCompleted = true
                                        }
                                    } catch (e: Exception) {
                                        Log.e("ARScreen", "Error setting up AR scene", e)
                                        errorMessage = "AR 장면 설정 중 오류가 발생했습니다."
                                    }
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
                        modifier = Modifier.fillMaxSize()
                    )
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
    modifier: Modifier = Modifier
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
    onMissionComplete: () -> Unit
) {
    Log.d("ARScreen", "Setting up AR scene for mission spot $spotId")
    
    // AR 장면 설정
    arSceneView.onFrame = { frame ->
        // 매 프레임마다 AR 상태 확인 (필요시 추가 로직 구현)
        Log.d("ARScreen", "AR frame updated")
    }
    
    // 임시로 화면 중앙에 간단한 3D 객체 생성
    try {
        // 모델 파일 존재 확인
        val modelPath = "models/cube.glb"
        val assetList = arSceneView.context.assets.list("models")
        
        if (assetList?.contains("cube.glb") != true) {
            Log.e("ARScreen", "Model file not found: $modelPath")
            Log.d("ARScreen", "Available models: ${assetList?.joinToString()}")
            // 기본 프리미티브 객체로 대체
            createPrimitiveNode(arSceneView)
            return
        }
        
        // 동기적으로 모델 인스턴스 생성 (SceneView 2.3.0 방식)
        try {
            val modelInstance = arSceneView.modelLoader.createModelInstance(modelPath)
            if (modelInstance != null) {
                // 모델 노드 생성 (올바른 방식)
                val modelNode = ModelNode(
                    modelInstance = modelInstance,
                    scaleToUnits = 1.0f
                ).apply {
                    // 위치 설정 (사용자 앞 2미터)
                    position = Position(0.0f, 0.0f, -2.0f)
                }
                
                // 장면에 추가
                arSceneView.addChildNode(modelNode)
                Log.d("ARScreen", "Model loaded and added successfully: $modelPath")
                
                // 애니메이션이 있다면 재생 (첫 번째 애니메이션을 루프로 재생)
                try {
                    modelNode.playAnimation(animationIndex = 0, loop = true)
                } catch (e: Exception) {
                    Log.d("ARScreen", "No animations available for this model")
                }
            } else {
                Log.w("ARScreen", "Failed to create model instance: $modelPath")
                createPrimitiveNode(arSceneView)
            }
        } catch (e: Exception) {
            Log.e("ARScreen", "Error creating model node: $modelPath", e)
            createPrimitiveNode(arSceneView)
        }
        
    } catch (e: Exception) {
        Log.e("ARScreen", "Error setting up AR objects", e)
        createPrimitiveNode(arSceneView)
    }
}

// 기본 프리미티브 노드 생성 (모델 로딩 실패 시 대체용)
private fun createPrimitiveNode(arSceneView: ARSceneView) {
    try {
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
                }
                arSceneView.addChildNode(cubeNode)
                Log.d("ARScreen", "Fallback cube model loaded")
            } else {
                Log.d("ARScreen", "Failed to create fallback model instance")
            }
        } catch (e: Exception) {
            Log.d("ARScreen", "Error creating fallback cube node", e)
        }
    } catch (e: Exception) {
        Log.d("ARScreen", "Fallback model creation skipped: ${e.message}")
        // 모델 없이도 AR 세션은 정상 동작
    }
}