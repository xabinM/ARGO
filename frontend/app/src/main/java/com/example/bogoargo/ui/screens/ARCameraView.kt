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
    private var missionCompleted = false
    
    // Geospatial 관련 상태
    private var earth: Earth? = null
    private var geospatialAnchor: Anchor? = null
    private var isEarthTracking = false
    private var currentGeospatialPose: GeospatialPose? = null
    private var earthState: Earth.EarthState? = null
    private var lastTrackingFailureReason: String? = null
    
    // 상태 콜백
    var onEarthStateChanged: ((Boolean, GeospatialPose?) -> Unit)? = null
    var onGeospatialError: ((String) -> Unit)? = null
    
    // 렌더러
    private val backgroundRenderer = BackgroundRenderer()
    private val objectRenderer = ObjectRenderer()
    
    init {
        setupOpenGL()
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
     * 터치 이벤트 처리 - Geospatial 앵커 터치 감지
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && isSessionActive && !missionCompleted) {
            // Geospatial 앵커가 터치되었는지 확인
            geospatialAnchor?.let { anchor ->
                if (anchor.trackingState == TrackingState.TRACKING) {
                    missionCompleted = true
                    onMissionComplete(true)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
    
    
    /**
     * AR 세션 시작
     */
    override fun onResume() {
        super.onResume()
        try {
            session.resume()
            isSessionActive = true
            
            // Geospatial 지원 여부 체크
            try {
                val isGeospatialSupported = session.isGeospatialModeSupported(Config.GeospatialMode.ENABLED)
                if (!isGeospatialSupported) {
                    onGeospatialError?.invoke("이 기기는 Geospatial API를 지원하지 않습니다")
                }
            } catch (e: Exception) {
                // Error checking geospatial support
            }
            
        } catch (e: Exception) {
            isSessionActive = false
        }
    }
    
    /**
     * AR 세션 일시정지
     */
    override fun onPause() {
        super.onPause()
        try {
            session.pause()
            isSessionActive = false
        } catch (e: Exception) {
            // Failed to pause AR session
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
            session.setDisplayGeometry(display.rotation, width, height)
        }
        
        override fun onDrawFrame(gl: GL10) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            
            if (!isSessionActive) return
            
            try {
                // 카메라 텍스처 설정
                session.setCameraTextureName(backgroundRenderer.textureId)
                
                // ARCore 프레임 업데이트
                val frame = session.update()
                val camera = frame.camera
                
                // 카메라 배경 렌더링
                backgroundRenderer.render(frame)
                
                // 카메라 추적 상태 확인
                if (camera.trackingState == TrackingState.PAUSED) return
                
                // Earth 객체 및 Geospatial 추적 상태 확인
                earth = session.earth
                checkGeospatialStatus()
                
                if (earth?.trackingState == TrackingState.TRACKING) {
                    isEarthTracking = true
                    
                    // 현재 Geospatial Pose 가져오기
                    currentGeospatialPose = earth?.cameraGeospatialPose
                    currentGeospatialPose?.let { pose ->
                        onEarthStateChanged?.invoke(true, pose)
                    }
                    
                    // 미션 위치에 앵커 생성 (한 번만)
                    if (geospatialAnchor == null && !missionCompleted) {
                        createGeospatialAnchor()
                    }
                } else {
                    isEarthTracking = false
                    val trackingState = earth?.trackingState
                    
                    when (trackingState) {
                        TrackingState.PAUSED -> {
                            lastTrackingFailureReason = "GPS 신호를 찾고 있습니다..."
                        }
                        TrackingState.STOPPED -> {
                            lastTrackingFailureReason = "위치 서비스를 사용할 수 없습니다"
                        }
                        else -> {
                            lastTrackingFailureReason = "Geospatial API 상태 불명"
                        }
                    }
                    
                    lastTrackingFailureReason?.let { reason ->
                        onGeospatialError?.invoke(reason)
                    }
                    
                    onEarthStateChanged?.invoke(false, null)
                }
                
                // 3D 객체 렌더링
                renderAnchors(camera)
                
            } catch (e: Exception) {
                // Rendering error
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
            
            // Depth 테스트 활성화
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glDepthFunc(GLES20.GL_LEQUAL)
            
            // Geospatial 앵커 렌더링
            geospatialAnchor?.let { anchor ->
                if (anchor.trackingState == TrackingState.TRACKING) {
                    objectRenderer.draw(viewMatrix, projectionMatrix, anchor, 0.5f)
                }
            }
            
            // Depth 테스트 비활성화
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
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
                                onGeospatialError?.invoke("이 지역에서는 정밀 위치 서비스를 사용할 수 없습니다")
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
    
}