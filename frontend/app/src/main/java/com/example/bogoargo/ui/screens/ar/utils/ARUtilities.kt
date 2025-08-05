package com.example.bogoargo.ui.screens.ar.utils

import android.content.Context
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import kotlin.math.sqrt
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


// GPS 및 위치 서비스 상태 확인 함수
fun checkLocationServicesStatus(context: Context): Triple<Boolean, Boolean, Boolean> {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    
    // GPS 활성화 상태
    val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    
    // 네트워크 위치 서비스 활성화 상태  
    val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    
    // 전체 위치 서비스 활성화 상태 (API 28+부터는 LocationManager 사용 권장)
    val locationServicesEnabled = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        locationManager.isLocationEnabled
    } else {
        @Suppress("DEPRECATION")
        try {
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_OFF
        } catch (e: Exception) {
            false
        }
    }
    
    return Triple(gpsEnabled, networkEnabled, locationServicesEnabled)
}

// 랜덤 Y축 회전 생성
fun generateRandomYRotation(): Rotation {
    return Rotation(0f, Random.nextFloat() * 360f, 0f)
}

// 사용자 주변 랜덤 위치 생성 (1.5m~5m 범위, 360도)
fun generateRandomPositionAroundUser(): Position {
    // 최소 1.5m, 최대 5m 범위에서 랜덤 거리
    val minDistance = 1.5f
    val maxDistance = 5.0f
    val distance = Random.nextFloat() * (maxDistance - minDistance) + minDistance
    
    // 360도 랜덤 각도
    val angle = Random.nextFloat() * 2 * kotlin.math.PI
    
    val x = distance * cos(angle).toFloat()
    val z = distance * sin(angle).toFloat()
    
    return Position(x, -1.5f, z)
}

// 카메라와 AR 객체 간의 거리 계산 (ARSceneView를 통해)
fun calculateDistanceToObject(arSceneView: io.github.sceneview.ar.ARSceneView, objectPosition: Position): Float {
    return try {
        val session = arSceneView.session ?: return Float.MAX_VALUE
        val frame = session.update()
        val cameraPose = frame.camera.pose
        
        // 카메라 위치 가져오기
        val cameraX = cameraPose.translation[0]
        val cameraY = cameraPose.translation[1] 
        val cameraZ = cameraPose.translation[2]
        
        // 3D 유클리드 거리 계산
        val deltaX = objectPosition.x - cameraX
        val deltaY = objectPosition.y - cameraY
        val deltaZ = objectPosition.z - cameraZ
        
        sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()).toFloat()
    } catch (e: Exception) {
        Log.w("ARUtilities", "Failed to calculate distance to object: ${e.message}")
        Float.MAX_VALUE
    }
}

// 객체가 상호작용 가능한 거리 내에 있는지 확인 (2m 이내)
fun isObjectInteractable(distance: Float, maxDistance: Float = 2.0f): Boolean {
    return distance <= maxDistance
}

// 거리를 사용자 친화적 문자열로 변환
fun formatDistance(distance: Float): String {
    return when {
        distance == Float.MAX_VALUE -> "알 수 없음"
        distance < 1.0f -> "${(distance * 100).toInt()}cm"
        else -> String.format("%.1fm", distance)
    }
}