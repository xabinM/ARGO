package com.example.bogoargo.ui.screens

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * 카메라 배경 렌더러
 * ARCore 카메라 피드를 OpenGL 텍스처로 렌더링
 */
class BackgroundRenderer {
    
    companion object {
        private const val TAG = "BackgroundRenderer"
        
        // 버텍스 셰이더
        private const val VERTEX_SHADER = """
            attribute vec2 a_Position;
            attribute vec2 a_TexCoord;
            varying vec2 v_TexCoord;
            void main() {
                gl_Position = vec4(a_Position, 0.0, 1.0);
                v_TexCoord = a_TexCoord;
            }
        """
        
        // 프래그먼트 셰이더 (OES 텍스처용) - 단순화
        private const val FRAGMENT_SHADER = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            uniform samplerExternalOES u_Texture;
            varying vec2 v_TexCoord;
            void main() {
                gl_FragColor = texture2D(u_Texture, v_TexCoord);
            }
        """
    }
    
    var textureId: Int = -1
        private set
    
    private var shaderProgram: Int = 0
    private var vertexBuffer: FloatBuffer? = null
    private var texCoordBuffer: FloatBuffer? = null
    
    // 셰이더 핸들 캐싱
    private var positionHandle: Int = -1
    private var texCoordHandle: Int = -1
    private var textureHandle: Int = -1
    
    // 원본 VIEW_NORMALIZED 좌표 (변환 전)
    private val originalViewCoords = floatArrayOf(
        0.0f, 0.0f,  // 좌하단
        1.0f, 0.0f,  // 우하단
        0.0f, 1.0f,  // 좌상단
        1.0f, 1.0f   // 우상단
    )
    
    /**
     * 렌더러 초기화
     */
    fun initialize(context: Context) {
        // 텍스처 생성
        createTexture()
        
        // 셰이더 프로그램 생성
        createShaderProgram()
        
        // 버텍스 버퍼 생성
        createVertexBuffers()
        
        // 셰이더 핸들 가져오기 (캐싱)
        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "a_Position")
        texCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "a_TexCoord")
        textureHandle = GLES20.glGetUniformLocation(shaderProgram, "u_Texture")
        
        Log.d(TAG, "Background renderer initialized")
    }
    
    /**
     * OES 텍스처 생성
     */
    private fun createTexture() {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
    }
    
    /**
     * 셰이더 프로그램 생성
     */
    private fun createShaderProgram() {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        
        shaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShader)
        GLES20.glAttachShader(shaderProgram, fragmentShader)
        GLES20.glLinkProgram(shaderProgram)
        
        // 링크 상태 확인
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val error = GLES20.glGetProgramInfoLog(shaderProgram)
            GLES20.glDeleteProgram(shaderProgram)
            throw RuntimeException("Shader program linking failed: $error")
        }
    }
    
    /**
     * 버텍스 버퍼 생성
     */
    private fun createVertexBuffers() {
        // 전체 화면을 덮는 사각형 (Triangle Strip)
        val vertices = floatArrayOf(
            -1.0f, -1.0f,  // 좌하단
             1.0f, -1.0f,  // 우하단
            -1.0f,  1.0f,  // 좌상단
             1.0f,  1.0f   // 우상단
        )
        
        // 초기 텍스처 좌표 (업데이트될 예정)
        val texCoords = floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
        )
        
        vertexBuffer = createFloatBuffer(vertices)
        texCoordBuffer = createFloatBuffer(texCoords)
    }
    
    /**
     * FloatBuffer 생성 헬퍼
     */
    private fun createFloatBuffer(data: FloatArray): FloatBuffer {
        return ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(data)
            .apply { position(0) }
    }
    
    /**
     * 셰이더 로드 및 컴파일
     */
    private fun loadShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val error = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Shader compilation failed: $error")
        }
        
        return shader
    }
    
    /**
     * 카메라 배경 렌더링
     */
    fun render(frame: Frame) {
        if (shaderProgram == 0 || vertexBuffer == null || texCoordBuffer == null) {
            Log.w(TAG, "Renderer not properly initialized")
            return
        }
        
        // 디스플레이 지오메트리가 변경되었는지 확인
        if (frame.hasDisplayGeometryChanged()) {
            updateTextureCoordinates(frame)
        }
        
        // 깊이 테스트 비활성화 (배경은 항상 뒤에)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(false)
        
        // 셰이더 프로그램 사용
        GLES20.glUseProgram(shaderProgram)
        
        // 텍스처 바인딩
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        
        // 버텍스 어트리뷰트 설정
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)
        
        // 유니폼 설정
        GLES20.glUniform1i(textureHandle, 0)
        
        // 사각형 렌더링
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        
        // 어트리뷰트 비활성화
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
        
        // 깊이 테스트 재활성화
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(true)
    }
    
    /**
     * 디스플레이 지오메트리 변경시 텍스처 좌표 업데이트
     */
    private fun updateTextureCoordinates(frame: Frame) {
        try {
            val transformedTexCoords = FloatArray(8)
            frame.transformCoordinates2d(
                Coordinates2d.VIEW_NORMALIZED,
                originalViewCoords,
                Coordinates2d.TEXTURE_NORMALIZED,
                transformedTexCoords
            )
            
            // Triangle Strip 순서로 재배열
// 좌우 반전 (상하는 그대로)
            val reorderedCoords = floatArrayOf(
                // 상하만 반전: 위쪽 두 점과 아래쪽 두 점을 바꾼다
                transformedTexCoords[4], transformedTexCoords[5],  // 좌상단 -> 좌하단
                transformedTexCoords[6], transformedTexCoords[7],  // 우상단 -> 우하단
                transformedTexCoords[0], transformedTexCoords[1],  // 좌하단 -> 좌상단
                transformedTexCoords[2], transformedTexCoords[3]   // 우하단 -> 우상단
            )


            
            texCoordBuffer?.apply {
                clear()
                put(reorderedCoords)
                position(0)
            }
            
            Log.d(TAG, "Texture coordinates updated")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update texture coordinates", e)
        }
    }
    
    /**
     * OpenGL 리소스 정리
     */
    fun cleanup() {
        if (textureId != -1) {
            val textures = intArrayOf(textureId)
            GLES20.glDeleteTextures(1, textures, 0)
            textureId = -1
        }
        
        if (shaderProgram != 0) {
            GLES20.glDeleteProgram(shaderProgram)
            shaderProgram = 0
        }
        
        vertexBuffer = null
        texCoordBuffer = null
        
        Log.d(TAG, "Background renderer resources cleaned up")
    }
}