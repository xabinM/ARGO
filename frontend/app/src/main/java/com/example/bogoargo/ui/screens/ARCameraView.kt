package com.example.bogoargo.ui.screens

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent
import com.google.ar.core.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * ARCore 기반 카메라 뷰
 * 
 * 주요 기능:
 * - 실시간 카메라 피드 렌더링
 * - 평면 감지 및 3D 객체 배치
 * - AR 세션 생명주기 관리
 */
class ARCameraView(
    context: Context,
    private val session: Session,
    private val targetLatitude: Double,
    private val targetLongitude: Double,
    private val onMissionComplete: (Boolean) -> Unit
) : GLSurfaceView(context) {
    
    companion object {
        private const val TAG = "ARCameraView"
    }
    
    // 렌더링 상태
    private var isSessionActive = false
    private var isSessionPaused = false
    private var sessionResumeAttempts = 0
    private val MAX_RESUME_ATTEMPTS = 3
    private var missionCompleted = false
    
    // 카메라 텍스처 최적화
    private var lastCameraTextureUpdate = 0L
    private val CAMERA_TEXTURE_UPDATE_INTERVAL = 16L // 60fps 제한
    private var frameSkipCount = 0
    private val MAX_FRAME_SKIP = 3
    
    // 자동 복구 로직
    private var lastSuccessfulFrame = 0L
    private val SESSION_TIMEOUT = 5000L // 5초
    private var consecutiveErrors = 0
    private val MAX_CONSECUTIVE_ERRORS = 10
    private var isRecovering = false
    private var lastRecoveryAttempt = 0L
    private val RECOVERY_COOLDOWN = 3000L // 3초
    
    // Geospatial 관련 상태
    private var earth: Earth? = null
    private var geospatialAnchor: Anchor? = null
    private var isEarthTracking = false
    private var currentGeospatialPose: GeospatialPose? = null
    private var earthState: Earth.EarthState? = null
    private var lastTrackingFailureReason: String? = null
    
    // 앙커 생성 단계 관리
    enum class AnchorCreationStage {
        TERRAIN,    // 지형 기반 앙커 시도
        FALLBACK    // 카메라 전방 앙커 (최종 fallback)
    }
    
    // 폴백 모드 관련 상태
    private var isFallbackMode = false
    private var fallbackAnchor: Anchor? = null
    private var geospatialCheckAttempts = 0
    private val MAX_GEOSPATIAL_ATTEMPTS = 30 // 약 30초간 시도
    private var isVpsUnavailable = false // VPS 불가능 지역 표시
    private var currentAnchorStage = AnchorCreationStage.TERRAIN
    private var anchorCreationAttempted = false
    
    // 상태 콜백
    var onEarthStateChanged: ((Boolean, GeospatialPose?) -> Unit)? = null
    var onGeospatialError: ((String) -> Unit)? = null
    var onDistanceUpdate: ((Float) -> Unit)? = null // 거리 업데이트 콜백
    var onTerrainAnchorError: ((Boolean) -> Unit)? = null // 지형 앵커 오류 콜백
    
    // 터치 이벤트 처리용 변수
    private var pendingTouchX: Float? = null
    private var pendingTouchY: Float? = null
    
    /**
     * VPS 가용성 상태 설정
     */
    fun setVpsAvailability(isAvailable: Boolean) {
        isVpsUnavailable = !isAvailable
        if (!isAvailable) {
            Log.d(TAG, "VPS unavailable - will use faster fallback")
        }
    }
    
    /**
     * 단계별 앙커 생성 시도
     */
    private fun attemptAnchorCreation(camera: Camera) {
        if (geospatialAnchor != null || missionCompleted || isDisposed) {
            return // 이미 앙커가 있거나 미션 완료됨
        }
        
        when (currentAnchorStage) {
            AnchorCreationStage.TERRAIN -> {
                if (isEarthTracking && !anchorCreationAttempted) {
                    Log.d(TAG, "1단계: 지형 기반 앙커 시도")
                    createGeospatialAnchor()
                    anchorCreationAttempted = true
                }
            }
            AnchorCreationStage.FALLBACK -> {
                if (fallbackAnchor == null && !anchorCreationAttempted) {
                    Log.d(TAG, "3단계: 카메라 전방 앙커 시도")
                    createFallbackAnchor(camera)
                    anchorCreationAttempted = true
                    isFallbackMode = true
                }
            }
        }
    }
    
    /**
     * 카메라와 앙커 사이의 거리 계산
     */
    private fun calculateDistanceToAnchor(camera: Camera): Float? {
        val cameraPose = camera.pose
        val cameraPosition = cameraPose.translation
        
        // 활성화된 앙커 찾기
        val activeAnchor = when {
            geospatialAnchor?.trackingState == TrackingState.TRACKING -> geospatialAnchor
            fallbackAnchor?.trackingState == TrackingState.TRACKING -> fallbackAnchor
            else -> null
        }
        
        return activeAnchor?.let { anchor ->
            val anchorPose = anchor.pose
            val anchorPosition = anchorPose.translation
            
            // 3D 유클리드 거리 계산
            val dx = cameraPosition[0] - anchorPosition[0]
            val dy = cameraPosition[1] - anchorPosition[1]
            val dz = cameraPosition[2] - anchorPosition[2]
            
            kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
        }
    }
    
    /**
     * 다음 앙커 생성 단계로 이동
     */
    private fun moveToNextAnchorStage() {
        when (currentAnchorStage) {
            AnchorCreationStage.TERRAIN -> {
                currentAnchorStage = AnchorCreationStage.FALLBACK
                anchorCreationAttempted = false
                isFallbackMode = true
                Log.d(TAG, "앙커 단계 전환: TERRAIN → FALLBACK")
            }
            AnchorCreationStage.FALLBACK -> {
                // 이미 최종 단계
                Log.d(TAG, "이미 최종 fallback 단계입니다")
            }
        }
    }
    
    // 리소스 정리 상태
    private var isResourcesInitialized = false
    private var isDisposed = false
    
    // 렌더러
    private val backgroundRenderer = BackgroundRenderer()
    private val objectRenderer = ObjectRenderer()
    
    init {
        setupOpenGL()
        // 초기 상태 설정
        lastSuccessfulFrame = System.currentTimeMillis()
    }
    
    /**
     * OpenGL ES 설정
     */
    private fun setupOpenGL() {
        preserveEGLContextOnPause = true
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        setRenderer(ARRenderer())
        renderMode = RENDERMODE_CONTINUOUSLY
    }
    
    /**
     * 터치 이벤트 처리 - 터치 좌표만 저장하고 GL 스레드에서 처리
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && isSessionActive && !missionCompleted) {
            // 터치 좌표만 저장 (GL 스레드에서 처리)
            pendingTouchX = event.x
            pendingTouchY = event.y
            Log.d(TAG, "터치 이벤트 저장: (${event.x}, ${event.y})")
            return true
        }
        return super.onTouchEvent(event)
    }
    
    
    /**
     * AR 세션 시작 - 안전성 강화
     */
    override fun onResume() {
        super.onResume()
        
        // 이미 활성 상태이거나 최대 재시도 횟수를 초과한 경우 건너뛰기
        if (isSessionActive && !isSessionPaused) {
            Log.d(TAG, "Session already active, skipping resume")
            return
        }
        
        if (sessionResumeAttempts >= MAX_RESUME_ATTEMPTS) {
            Log.e(TAG, "Maximum resume attempts reached")
            onGeospatialError?.invoke("카메라 초기화에 실패했습니다. 앱을 재시작해주세요.")
            return
        }
        
        try {
            sessionResumeAttempts++
            Log.d(TAG, "Attempting to resume session (attempt $sessionResumeAttempts)")
            
            session.resume()
            isSessionActive = true
            isSessionPaused = false
            sessionResumeAttempts = 0 // 성공 시 재시도 카운터 리셋
            
            Log.d(TAG, "Session resumed successfully")
            
            // Geospatial 지원 여부 체크
            try {
                val isGeospatialSupported = session.isGeospatialModeSupported(Config.GeospatialMode.ENABLED)
                if (!isGeospatialSupported) {
                    onGeospatialError?.invoke("이 기기는 Geospatial API를 지원하지 않습니다")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error checking geospatial support", e)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resume AR session (attempt $sessionResumeAttempts)", e)
            isSessionActive = false
            isSessionPaused = true
            
            // 재시도가 남아있으면 잠시 후 재시도
            if (sessionResumeAttempts < MAX_RESUME_ATTEMPTS) {
                post {
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        onResume()
                    }, 1000) // 1초 후 재시도
                }
            } else {
                onGeospatialError?.invoke("카메라 연결에 실패했습니다: ${e.message}")
            }
        }
    }
    
    /**
     * AR 세션 일시정지 - 안전성 강화
     */
    override fun onPause() {
        super.onPause()
        
        if (!isSessionActive) {
            Log.d(TAG, "Session already inactive, skipping pause")
            return
        }
        
        try {
            Log.d(TAG, "Pausing AR session")
            session.pause()
            isSessionActive = false
            isSessionPaused = true
            Log.d(TAG, "Session paused successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause AR session", e)
            // 강제로 상태 업데이트
            isSessionActive = false
            isSessionPaused = true
        }
    }
    
    /**
     * AR 렌더러 - 매 프레임마다 AR 콘텐츠 렌더링
     */
    inner class ARRenderer : GLSurfaceView.Renderer {
        
        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
            
            try {
                backgroundRenderer.initialize(context)
                objectRenderer.createOnGlThread(context)
            } catch (e: Exception) {
                // Failed to initialize renderer
            }
        }
        
        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            try {
                session.setDisplayGeometry(display.rotation, width, height)
                // 화면 크기 변경 시 텍스처 업데이트 타이머 리셋
                lastCameraTextureUpdate = 0L
                frameSkipCount = 0
                Log.d(TAG, "Surface changed: ${width}x${height}")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting display geometry", e)
            }
        }
        
        override fun onDrawFrame(gl: GL10) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            
            if (!isSessionActive) return
            
            try {
                val currentTime = System.currentTimeMillis()
                
                // 카메라 텍스처 업데이트 빈도 제한
                if (currentTime - lastCameraTextureUpdate >= CAMERA_TEXTURE_UPDATE_INTERVAL) {
                    session.setCameraTextureName(backgroundRenderer.textureId)
                    lastCameraTextureUpdate = currentTime
                    frameSkipCount = 0
                } else {
                    frameSkipCount++
                    // 너무 많은 프레임을 건너뛰지 않도록 제한
                    if (frameSkipCount > MAX_FRAME_SKIP) {
                        session.setCameraTextureName(backgroundRenderer.textureId)
                        lastCameraTextureUpdate = currentTime
                        frameSkipCount = 0
                    }
                }
                
                // ARCore 프레임 업데이트
                val frame = session.update()
                val camera = frame.camera
                
                // 성공적인 프레임 업데이트 기록
                lastSuccessfulFrame = currentTime
                consecutiveErrors = 0
                
                // 카메라 배경 렌더링
                backgroundRenderer.render(frame)
                
                // 카메라 추적 상태 확인
                if (camera.trackingState == TrackingState.PAUSED) {
                    Log.d(TAG, "Camera tracking paused, waiting for recovery")
                    checkSessionHealth(currentTime)
                    return
                } else if (camera.trackingState == TrackingState.STOPPED) {
                    Log.w(TAG, "Camera tracking stopped, attempting recovery")
                    triggerSessionRecovery("Camera tracking stopped")
                    return
                }
                
                // Earth 객체 및 Geospatial 추적 상태 확인
                earth = session.earth
                checkGeospatialStatus()
                
                if (earth?.trackingState == TrackingState.TRACKING) {
                    isEarthTracking = true
                    geospatialCheckAttempts = 0
                    
                    // 현재 Geospatial Pose 가져오기
                    currentGeospatialPose = earth?.cameraGeospatialPose
                    currentGeospatialPose?.let { pose ->
                        onEarthStateChanged?.invoke(true, pose)
                    }
                    
                    // 단계별 앙커 생성 시도
                    attemptAnchorCreation(camera)
                    
                    // 터치 이벤트 처리 (GL 스레드에서)
                    processPendingTouch(frame, camera)
                    
                } else {
                    isEarthTracking = false
                    val trackingState = earth?.trackingState
                    
                    // Geospatial 시도 횟수 증가
                    geospatialCheckAttempts++
                    
                    when (trackingState) {
                        TrackingState.PAUSED -> {
                            lastTrackingFailureReason = "GPS 신호를 찾고 있습니다... (${geospatialCheckAttempts}/${MAX_GEOSPATIAL_ATTEMPTS})"
                        }
                        TrackingState.STOPPED -> {
                            lastTrackingFailureReason = "위치 서비스를 사용할 수 없습니다"
                        }
                        else -> {
                            lastTrackingFailureReason = "Geospatial API 상태 불명"
                        }
                    }
                    
                    // VPS 불가능 지역이거나 최대 시도 횟수 초과 시 fallback 단계로 전환
                    val shouldFallback = (isVpsUnavailable && geospatialCheckAttempts >= 30) || // VPS 불가능 시 빠른 fallback
                                       (geospatialCheckAttempts >= MAX_GEOSPATIAL_ATTEMPTS)
                    
                    if (shouldFallback && currentAnchorStage != AnchorCreationStage.FALLBACK && !missionCompleted && !isDisposed) {
                        lastTrackingFailureReason = if (isVpsUnavailable) {
                            "VPS 불가능 지역 → 카메라 전방 모드로 전환"
                        } else {
                            "GPS 불안정 → 카메라 전방 모드로 전환"
                        }
                        currentAnchorStage = AnchorCreationStage.FALLBACK
                        anchorCreationAttempted = false
                        isFallbackMode = true
                    }
                    
                    // Geospatial API 추적 실패 시에도 앙커 생성 시도 (ROOFTOP이나 FALLBACK 단계)
                    attemptAnchorCreation(camera)
                    
                    lastTrackingFailureReason?.let { reason ->
                        onGeospatialError?.invoke(reason)
                    }
                    
                    onEarthStateChanged?.invoke(false, null)
                }
                
                // 거리 계산 및 업데이트
                calculateDistanceToAnchor(camera)?.let { distance ->
                    onDistanceUpdate?.invoke(distance)
                }
                
                // 3D 객체 렌더링
                renderAnchors(camera)
                
            } catch (e: Exception) {
                consecutiveErrors++
                Log.e(TAG, "Rendering error in onDrawFrame (consecutive: $consecutiveErrors)", e)
                
                // 연속 오류가 많으면 세션 복구 시도
                if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
                    triggerSessionRecovery("Too many consecutive rendering errors: ${e.message}")
                } else {
                    handleRenderingError(e)
                }
            }
        }
        
        /**
         * 배치된 앵커들 렌더링
         */
        private fun renderAnchors(camera: Camera) {
            // 프로젝션 매트릭스 가져오기
            val projectionMatrix = FloatArray(16)
            camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f)
            
            // 뷰 매트릭스 가져오기
            val viewMatrix = FloatArray(16)
            camera.getViewMatrix(viewMatrix, 0)
            
            // 현재 거리 계산
            val currentDistance = calculateDistanceToAnchor(camera)
            
            // Depth 테스트 활성화
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glDepthFunc(GLES20.GL_LEQUAL)
            
            // Geospatial 앵커 렌더링
            geospatialAnchor?.let { anchor ->
                val trackingState = anchor.trackingState
                Log.d(TAG, "Geospatial 앵커 - 거리: ${currentDistance}m, 추적상태: $trackingState")
                
                if (trackingState == TrackingState.TRACKING) {
                    objectRenderer.draw(viewMatrix, projectionMatrix, anchor, 0.5f)
                    Log.d(TAG, "Geospatial 앵커 렌더링 완료")
                } else {
                    Log.w(TAG, "Geospatial 앵커 추적 실패: $trackingState")
                }
            }
            
            // 폴백 앵커 렌더링
            fallbackAnchor?.let { anchor ->
                val trackingState = anchor.trackingState
                Log.d(TAG, "Fallback 앵커 - 거리: ${currentDistance}m, 추적상태: $trackingState")
                
                if (trackingState == TrackingState.TRACKING) {
                    objectRenderer.draw(viewMatrix, projectionMatrix, anchor, 0.5f)
                    Log.d(TAG, "Fallback 앵커 렌더링 완료")
                } else {
                    Log.w(TAG, "Fallback 앵커 추적 실패: $trackingState")
                }
            }
            
            // Depth 테스트 비활성화
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        }
    }
    
    /**
     * 렌더링 오류 처리
     */
    private fun handleRenderingError(error: Exception) {
        when {
            error.message?.contains("camera", ignoreCase = true) == true -> {
                Log.w(TAG, "Camera-related rendering error, attempting session recovery")
                triggerSessionRecovery("Camera error: ${error.message}")
            }
            error.message?.contains("texture", ignoreCase = true) == true -> {
                Log.w(TAG, "Texture-related error, resetting texture update timer")
                lastCameraTextureUpdate = 0L
                frameSkipCount = 0
            }
            error.message?.contains("disconnected", ignoreCase = true) == true -> {
                Log.w(TAG, "Camera disconnected, triggering recovery")
                triggerSessionRecovery("Camera disconnected: ${error.message}")
            }
            else -> {
                Log.w(TAG, "Generic rendering error: ${error.message}")
            }
        }
    }
    
    /**
     * 세션 상태 모니터링
     */
    private fun checkSessionHealth(currentTime: Long) {
        // 마지막 성공적인 프레임에서 너무 오래 지났는지 확인
        if (currentTime - lastSuccessfulFrame > SESSION_TIMEOUT) {
            Log.w(TAG, "Session timeout detected, attempting recovery")
            triggerSessionRecovery("Session timeout - no successful frames for ${SESSION_TIMEOUT}ms")
        }
    }
    
    /**
     * 세션 복구 시도
     */
    private fun triggerSessionRecovery(reason: String) {
        val currentTime = System.currentTimeMillis()
        
        // 이미 복구 중이거나 쿨다운 기간이면 건너뛰기
        if (isRecovering || (currentTime - lastRecoveryAttempt) < RECOVERY_COOLDOWN) {
            Log.d(TAG, "Recovery already in progress or in cooldown, skipping")
            return
        }
        
        isRecovering = true
        lastRecoveryAttempt = currentTime
        
        Log.i(TAG, "Triggering session recovery: $reason")
        onGeospatialError?.invoke("카메라 연결을 복구하고 있습니다...")
        
        post {
            try {
                if (isSessionActive) {
                    Log.d(TAG, "Pausing session for recovery")
                    onPause()
                }
                
                // 짧은 대기 후 재시작
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    try {
                        Log.d(TAG, "Resuming session after recovery")
                        onResume()
                        
                        // 복구 완료
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isRecovering = false
                            consecutiveErrors = 0
                            Log.i(TAG, "Session recovery completed")
                        }, 1000)
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to resume session during recovery", e)
                        isRecovering = false
                        onGeospatialError?.invoke("카메라 복구에 실패했습니다: ${e.message}")
                    }
                }, 1000)
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initiate session recovery", e)
                isRecovering = false
                onGeospatialError?.invoke("카메라 복구 시작에 실패했습니다: ${e.message}")
            }
        }
    }
    
    /**
     * Geospatial API 상태 체크 및 로깅
     */
    private fun checkGeospatialStatus() {
        earth?.let { earth ->
            // Earth State 체크
            earthState = earth.earthState
            
            when (earthState) {
                Earth.EarthState.ENABLED -> {
                    // Geospatial API is enabled
                }
                Earth.EarthState.ERROR_GEOSPATIAL_MODE_DISABLED -> {
                    onGeospatialError?.invoke("Geospatial 모드가 비활성화되어 있습니다")
                }
                else -> {
                    // 다른 가능한 오류 상태들을 문자열로 체크
                    when (earthState.toString()) {
                        "ERROR_NOT_AUTHORIZED" -> {
                            onGeospatialError?.invoke("⚠️ Google Cloud API 설정 필요\n• ARCore API 활성화\n• 결제 계정 연결\n• API 키 권한 확인")
                        }
                        "ERROR_LOCATION_NOT_AUTHORIZED" -> {
                            onGeospatialError?.invoke("위치 권한이 필요합니다")
                        }
                        "ERROR_INTERNET_PERMISSION_NOT_GRANTED" -> {
                            onGeospatialError?.invoke("인터넷 권한이 필요합니다")
                        }
                        "ERROR_VPS_AVAILABILITY_REQUEST_FAILED" -> {
                            onGeospatialError?.invoke("VPS 서비스에 연결할 수 없습니다")
                        }
                        "ERROR_RESOURCE_EXHAUSTED" -> {
                            onGeospatialError?.invoke("서비스 할당량을 초과했습니다")
                        }
                        else -> {
                            if (earthState != Earth.EarthState.ENABLED) {
                                onGeospatialError?.invoke("Geospatial API 오류: $earthState")
                            }
                        }
                    }
                }
            }
        } ?: run {
            onGeospatialError?.invoke("Geospatial API를 사용할 수 없습니다")
        }
    }
    
    
    /**
     * 미션 위치에 Geospatial 앵커 생성
     */
    private fun createGeospatialAnchor() {
        earth?.let { earth ->
            try {
                // 지형 기반 앵커 생성 (altitude는 지형 높이로 자동 결정)
                val altitudeAboveTerrain = 0.5 // 지면에서 1미터 위
                val qx = 0f
                val qy = 0f
                val qz = 0f
                val qw = 1f // 회전 없음
                
                earth.resolveAnchorOnTerrainAsync(
                    targetLatitude,
                    targetLongitude,
                    altitudeAboveTerrain,
                    qx, qy, qz, qw,
                    { anchor, state ->
                        when (state) {
                            Anchor.TerrainAnchorState.SUCCESS -> {
                                geospatialAnchor = anchor
                            }
                            Anchor.TerrainAnchorState.ERROR_NOT_AUTHORIZED -> {
                                onGeospatialError?.invoke("API 인증 오류 - API 키를 확인해주세요")
                            }
                            Anchor.TerrainAnchorState.ERROR_UNSUPPORTED_LOCATION -> {
                                onGeospatialError?.invoke("지형 정보 없음 → 카메라 전방 모드로 전환")
                                onTerrainAnchorError?.invoke(true) // 지형 앵커 오류 상태 전달
                                // 다음 단계(Rooftop)로 전환
                                moveToNextAnchorStage()
                            }
                            Anchor.TerrainAnchorState.ERROR_INTERNAL -> {
                                onGeospatialError?.invoke("내부 오류가 발생했습니다")
                            }
                            else -> {
                                onGeospatialError?.invoke("앵커 생성 실패: $state")
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                onGeospatialError?.invoke("앵커 생성 오류: ${e.message}")
            }
        } ?: run {
            onGeospatialError?.invoke("Earth 객체를 사용할 수 없습니다")
        }
    }
    
    /**
     * 폴백 모드에서 카메라 주변에 앵커 생성 (로컬 기반)
     */
    private fun createFallbackAnchor(camera: Camera) {
        try {
            val gpsAccuracy = currentGeospatialPose?.horizontalAccuracy ?: Double.MAX_VALUE
            val isGpsStable = gpsAccuracy <= 10.0
            
            if (isGpsStable) {
                // GPS 안정: 미션 위치 방향으로 배치 (Earth 사용 X)
                createDirectionalAnchor(camera, gpsAccuracy)
            } else {
                // GPS 불안정: 랜덤 위치
                createRandomPositionAnchor(camera, gpsAccuracy)
            }
        } catch (e: Exception) {
            Log.e(TAG, "폴백 앵커 생성 실패", e)
            onGeospatialError?.invoke("폴백 앵커 생성 실패: ${e.message}")
        }
    }
    
    /**
     * 미션 방향으로 앵커 생성 (로컬 좌표계 기반)
     */
    private fun createDirectionalAnchor(camera: Camera, gpsAccuracy: Double) {
        val cameraPose = camera.pose
        val translation = cameraPose.translation
        val rotation = cameraPose.rotationQuaternion
        
        // 현재 GPS 위치 확인
        val currentLat = currentGeospatialPose?.latitude
        val currentLon = currentGeospatialPose?.longitude
        
        if (currentLat != null && currentLon != null) {
            // 미션까지의 방위각 계산 (북쪽 기준)
            val bearing = calculateBearing(currentLat, currentLon, targetLatitude, targetLongitude)
            val distance = 7f // 7미터 앞에 배치
            
            // 방위각을 라디안으로 변환하고 카메라 좌표계에 맞게 조정
            val radians = Math.toRadians(bearing).toFloat()
            
            // 카메라 좌표계에서 미션 방향으로 위치 계산
            val x = translation[0] + distance * kotlin.math.sin(radians)
            val z = translation[2] - distance * kotlin.math.cos(radians) // Z축은 반대
            val y = translation[1] // 높이는 카메라와 동일
            
            val targetPosition = floatArrayOf(x, y, z)
            val targetPose = Pose(targetPosition, rotation)
            
            // 앵커 생성
            fallbackAnchor = session.createAnchor(targetPose)
            
            Log.d(TAG, "GPS 안정 (정확도: ${gpsAccuracy}m) - 미션 방향으로 ${distance}m 앞에 앵커 생성 (방위각: ${bearing.toInt()}도)")
            onGeospatialError?.invoke("미션 방향 ${distance.toInt()}m 앞에 객체 배치")
        } else {
            // GPS 위치를 알 수 없으면 카메라 전방에 배치
            val forward = floatArrayOf(0f, 0f, -5f, 1f)
            val rotationMatrix = FloatArray(16)
            val translationMatrix = FloatArray(16)
            val resultMatrix = FloatArray(16)
            
            android.opengl.Matrix.setIdentityM(rotationMatrix, 0)
            setRotationFromQuaternion(rotationMatrix, rotation)
            android.opengl.Matrix.setIdentityM(translationMatrix, 0)
            android.opengl.Matrix.translateM(translationMatrix, 0, translation[0], translation[1], translation[2])
            android.opengl.Matrix.multiplyMM(resultMatrix, 0, translationMatrix, 0, rotationMatrix, 0)
            
            val result = FloatArray(4)
            android.opengl.Matrix.multiplyMV(result, 0, resultMatrix, 0, forward, 0)
            
            val targetPosition = floatArrayOf(result[0], result[1], result[2])
            val targetPose = Pose(targetPosition, rotation)
            
            fallbackAnchor = session.createAnchor(targetPose)
            
            Log.d(TAG, "GPS 위치 없음 - 카메라 전방 5m에 앵커 생성")
            onGeospatialError?.invoke("카메라 전방 5m에 객체 배치")
        }
    }
    
    /**
     * 두 GPS 좌표 간의 방위각 계산 (북쪽 기준, 시계방향 도)
     */
    private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLon = Math.toRadians(lon2 - lon1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        
        val y = kotlin.math.sin(dLon) * kotlin.math.cos(lat2Rad)
        val x = kotlin.math.cos(lat1Rad) * kotlin.math.sin(lat2Rad) - 
                kotlin.math.sin(lat1Rad) * kotlin.math.cos(lat2Rad) * kotlin.math.cos(dLon)
        
        val bearing = Math.toDegrees(kotlin.math.atan2(y, x))
        return (bearing + 360) % 360 // 0-360도로 정규화
    }
    
    /**
     * 터치 이벤트 처리 (GL 스레드에서 실행)
     */
    private fun processPendingTouch(frame: Frame, camera: Camera) {
        val touchX = pendingTouchX
        val touchY = pendingTouchY
        
        if (touchX != null && touchY != null) {
            try {
                // hitTest를 통한 정확한 터치 감지
                val hits = frame.hitTest(touchX, touchY)
                
                for (hit in hits) {
                    val trackable = hit.trackable
                    
                    // 앵커와 연결된 객체인지 확인
                    if (trackable is Anchor) {
                        // Geospatial 앵커 확인
                        if (trackable == geospatialAnchor && trackable.trackingState == TrackingState.TRACKING) {
                            missionCompleted = true
                            onMissionComplete(true)
                            Log.d(TAG, "Geospatial 앵커 터치 완료")
                            break
                        }
                        
                        // 폴백 앵커 확인
                        if (trackable == fallbackAnchor && trackable.trackingState == TrackingState.TRACKING) {
                            missionCompleted = true
                            onMissionComplete(true)
                            Log.d(TAG, "Fallback 앵커 터치 완료")
                            break
                        }
                    }
                }
                
                // hitTest 결과가 없으면 거리 기반으로 확인
                if (hits.isEmpty()) {
                    val distance = calculateDistanceToAnchor(camera)
                    if (distance != null && distance <= 2.0f) {
                        // Geospatial 앵커가 터치되었는지 확인
                        geospatialAnchor?.let { anchor ->
                            if (anchor.trackingState == TrackingState.TRACKING) {
                                missionCompleted = true
                                onMissionComplete(true)
                                Log.d(TAG, "거리 기반 Geospatial 앵커 터치 완료")
                                return
                            }
                        }
                        
                        // 폴백 앵커가 터치되었는지 확인
                        fallbackAnchor?.let { anchor ->
                            if (anchor.trackingState == TrackingState.TRACKING) {
                                missionCompleted = true
                                onMissionComplete(true)
                                Log.d(TAG, "거리 기반 Fallback 앵커 터치 완료")
                                return
                            }
                        }
                    } else if (distance != null && distance > 2.0f) {
                        // 너무 멀리 있으면 안내 메시지
                        onGeospatialError?.invoke("객체에 더 가까이 가서 터치하세요 (현재 ${String.format("%.1f", distance)}m)")
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "터치 처리 중 오류", e)
            } finally {
                // 터치 이벤트 처리 완료 후 초기화
                pendingTouchX = null
                pendingTouchY = null
            }
        }
    }
    
    /**
     * 카메라 주변 랜덤 위치에 앵커 생성
     */
    private fun createRandomPositionAnchor(camera: Camera, gpsAccuracy: Double) {
        val cameraPose = camera.pose
        val translation = cameraPose.translation
        val rotation = cameraPose.rotationQuaternion
        
        // 랜덤 각도와 거리로 위치 계산
        val distance = 3f + kotlin.random.Random.nextFloat() * 7f // 3~10미터
        val angle = kotlin.random.Random.nextFloat() * 360f // 0~360도
        
        val radians = Math.toRadians(angle.toDouble()).toFloat()
        val x = translation[0] + distance * kotlin.math.cos(radians)
        val z = translation[2] + distance * kotlin.math.sin(radians)
        val y = translation[1] // 높이는 카메라와 동일
        
        val targetPosition = floatArrayOf(x, y, z)
        val targetPose = Pose(targetPosition, rotation)
        
        // 앵커 생성
        fallbackAnchor = session.createAnchor(targetPose)
        
        Log.d(TAG, "랜덤 배치 (GPS 정확도: ${gpsAccuracy}m) - 각도 ${angle.toInt()}도, 거리 ${distance.toInt()}m")
        onGeospatialError?.invoke("주변 ${distance.toInt()}m 거리에 객체 배치")
    }
    
    /**
     * 쿼터니언을 회전 매트릭스로 변환
     */
    private fun setRotationFromQuaternion(matrix: FloatArray, quaternion: FloatArray) {
        val x = quaternion[0]
        val y = quaternion[1] 
        val z = quaternion[2]
        val w = quaternion[3]
        
        val x2 = x + x
        val y2 = y + y
        val z2 = z + z
        val xx = x * x2
        val xy = x * y2
        val xz = x * z2
        val yy = y * y2
        val yz = y * z2
        val zz = z * z2
        val wx = w * x2
        val wy = w * y2
        val wz = w * z2
        
        matrix[0] = 1f - (yy + zz)
        matrix[1] = xy + wz
        matrix[2] = xz - wy
        matrix[3] = 0f
        
        matrix[4] = xy - wz
        matrix[5] = 1f - (xx + zz)
        matrix[6] = yz + wx
        matrix[7] = 0f
        
        matrix[8] = xz + wy
        matrix[9] = yz - wx
        matrix[10] = 1f - (xx + yy)
        matrix[11] = 0f
        
        matrix[12] = 0f
        matrix[13] = 0f
        matrix[14] = 0f
        matrix[15] = 1f
    }
    
    /**
     * 리소스 정리 - 외부에서 호출
     */
    fun cleanup() {
        Log.d(TAG, "Cleaning up ARCameraView resources")
        isDisposed = true
        
        try {
            // 세션 일시정지
            if (isSessionActive) {
                onPause()
            }
            
            // 앵커 정리
            cleanupAnchors()
            
            // 상태 초기화
            currentAnchorStage = AnchorCreationStage.TERRAIN
            anchorCreationAttempted = false
            isFallbackMode = false
            geospatialCheckAttempts = 0
            
            // 콜백 제거
            onEarthStateChanged = null
            onGeospatialError = null
            onDistanceUpdate = null
            onTerrainAnchorError = null
            
            Log.d(TAG, "ARCameraView cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
    
    /**
     * 앵커 정리
     */
    private fun cleanupAnchors() {
        try {
            geospatialAnchor?.detach()
            geospatialAnchor = null
            
            fallbackAnchor?.detach()
            fallbackAnchor = null
            
            Log.d(TAG, "Anchors cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up anchors", e)
        }
    }
    
}