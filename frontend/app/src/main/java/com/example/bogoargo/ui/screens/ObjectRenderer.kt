package com.example.bogoargo.ui.screens

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import com.google.ar.core.Anchor
import com.google.ar.core.Camera
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * 간단한 3D 객체(큐브) 렌더러
 * ARCore의 앵커 위치에 3D 객체를 렌더링하는 역할
 */
class ObjectRenderer {
    
    companion object {
        private const val TAG = "ObjectRenderer"
        
        // 큐브의 버텍스 좌표 (각 면에 6개의 버텍스)
        private val CUBE_VERTICES = floatArrayOf(
            // Front face
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            
            // Back face
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            
            // Top face
            -0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            
            // Bottom face
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f,
            
            // Right face
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            
            // Left face
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f
        )
        
        // 큐브의 색상 (빨간색)
        private val CUBE_COLORS = floatArrayOf(
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f
        ).let { baseColor ->
            // 모든 버텍스에 동일한 색상 적용
            FloatArray(36 * 4) { i -> baseColor[i % baseColor.size] }
        }
        
        // 버텍스 셰이더
        private const val VERTEX_SHADER = """
            uniform mat4 u_ModelViewProjection;
            attribute vec4 a_Position;
            attribute vec4 a_Color;
            varying vec4 v_Color;
            
            void main() {
                v_Color = a_Color;
                gl_Position = u_ModelViewProjection * a_Position;
            }
        """
        
        // 프래그먼트 셰이더
        private const val FRAGMENT_SHADER = """
            precision mediump float;
            varying vec4 v_Color;
            
            void main() {
                gl_FragColor = v_Color;
            }
        """
    }
    
    private var program: Int = 0
    private var positionHandle: Int = 0
    private var colorHandle: Int = 0
    private var modelViewProjectionHandle: Int = 0
    
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var colorBuffer: FloatBuffer
    
    private val modelMatrix = FloatArray(16)
    private val modelViewMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)
    
    /**
     * 렌더러 초기화
     */
    fun createOnGlThread(context: Context) {
        // 버텍스 버퍼 초기화
        val vertexByteBuffer = ByteBuffer.allocateDirect(CUBE_VERTICES.size * 4)
        vertexByteBuffer.order(ByteOrder.nativeOrder())
        vertexBuffer = vertexByteBuffer.asFloatBuffer()
        vertexBuffer.put(CUBE_VERTICES)
        vertexBuffer.position(0)
        
        // 색상 버퍼 초기화
        val colorByteBuffer = ByteBuffer.allocateDirect(CUBE_COLORS.size * 4)
        colorByteBuffer.order(ByteOrder.nativeOrder())
        colorBuffer = colorByteBuffer.asFloatBuffer()
        colorBuffer.put(CUBE_COLORS)
        colorBuffer.position(0)
        
        // 셰이더 프로그램 생성
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        
        // 핸들 가져오기
        positionHandle = GLES20.glGetAttribLocation(program, "a_Position")
        colorHandle = GLES20.glGetAttribLocation(program, "a_Color")
        modelViewProjectionHandle = GLES20.glGetUniformLocation(program, "u_ModelViewProjection")
    }
    
    /**
     * 앵커 위치에 객체 렌더링
     */
    fun draw(
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
        anchor: Anchor,
        scaleFactor: Float = 0.1f
    ) {
        if (anchor.trackingState != com.google.ar.core.TrackingState.TRACKING) {
            return
        }
        
        // 모델 매트릭스 가져오기
        anchor.pose.toMatrix(modelMatrix, 0)
        
        // 스케일 적용
        Matrix.scaleM(modelMatrix, 0, scaleFactor, scaleFactor, scaleFactor)
        
        // Model-View 매트릭스 계산
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        
        // Model-View-Projection 매트릭스 계산
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)
        
        // 셰이더 프로그램 사용
        GLES20.glUseProgram(program)
        
        // 버텍스 속성 설정
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(
            positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer
        )
        
        GLES20.glEnableVertexAttribArray(colorHandle)
        GLES20.glVertexAttribPointer(
            colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer
        )
        
        // 유니폼 설정
        GLES20.glUniformMatrix4fv(
            modelViewProjectionHandle, 1, false, modelViewProjectionMatrix, 0
        )
        
        // 큐브 그리기
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
        
        // 버텍스 속성 비활성화
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
    
    /**
     * 셰이더 로드
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }
}