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
    private val onMissionComplete: (Boolean) -> Unit
) : GLSurfaceView(context) {
    
    companion object {
        private const val TAG = "ARCameraView"
    }
    
    // 렌더링 상태
    private var isSessionActive = false
    private var hasFoundPlane = false
    private var missionCompleted = false
    private val anchors = mutableListOf<Anchor>()
    
    // 렌더러
    private val backgroundRenderer = BackgroundRenderer()
    
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
     * 터치 이벤트 처리 - 평면에 3D 객체 배치
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && 
            hasFoundPlane && 
            !missionCompleted &&
            isSessionActive) {
            
            queueEvent {
                handleTouchEvent(event.x, event.y)
            }
            return true
        }
        return super.onTouchEvent(event)
    }
    
    /**
     * 터치 지점에 AR 객체 배치
     */
    private fun handleTouchEvent(x: Float, y: Float) {
        try {
            val frame = session.update()
            val camera = frame.camera
            
            if (camera.trackingState != TrackingState.TRACKING) return
            
            // 히트 테스트로 평면과의 교차점 찾기
            val hits = frame.hitTest(x, y)
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    val anchor = hit.createAnchor()
                    anchors.add(anchor)
                    
                    if (!missionCompleted) {
                        missionCompleted = true
                        onMissionComplete(true)
                        Log.d(TAG, "Mission completed!")
                    }
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling touch event", e)
        }
    }
    
    /**
     * AR 세션 시작
     */
    override fun onResume() {
        super.onResume()
        try {
            session.resume()
            isSessionActive = true
            Log.d(TAG, "AR session resumed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resume AR session", e)
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
            Log.d(TAG, "AR session paused")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause AR session", e)
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
                Log.d(TAG, "AR renderer initialized")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize renderer", e)
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
                
                // 평면 감지 상태 업데이트
                val planes = session.getAllTrackables(Plane::class.java)
                hasFoundPlane = planes.any { it.trackingState == TrackingState.TRACKING }
                
                // 3D 객체 렌더링 (실제 구현에서는 ObjectRenderer 사용)
                renderAnchors(camera)
                
            } catch (e: Exception) {
                Log.e(TAG, "Rendering error", e)
            }
        }
        
        /**
         * 배치된 앵커들 렌더링
         */
        private fun renderAnchors(camera: Camera) {
            for (anchor in anchors) {
                if (anchor.trackingState == TrackingState.TRACKING) {
                    // 여기서 실제 3D 모델을 렌더링
                    // 현재는 로그만 출력
                    Log.v(TAG, "Rendering anchor at ${anchor.pose}")
                }
            }
        }
    }
}