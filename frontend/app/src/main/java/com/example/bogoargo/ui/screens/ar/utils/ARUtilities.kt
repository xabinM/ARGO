package com.example.bogoargo.ui.screens.ar.utils

import android.content.Context
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import kotlin.random.Random

// 애니메이션 실행 함수 (한번만 실행)
fun playAnimationOnce(
    modelNode: ModelNode, 
    onAnimationPlayed: () -> Unit,
    onGlobalAnimationPlayed: () -> Unit = {}
) {
    try {
        // 애니메이션 한번만 재생 (loop = false)
        modelNode.playAnimation(animationIndex = 0, loop = false)
        onAnimationPlayed() // 로컬 애니메이션 재생 상태 업데이트
        onGlobalAnimationPlayed() // 글로벌 애니메이션 재생 상태 업데이트
        Log.d("ARScreen", "Animation started - will play once")
    } catch (e: Exception) {
        Log.w("ARScreen", "No animations available for this model: ${e.message}")
    }
}

// GPS 및 위치 서비스 상태 확인 함수
fun checkLocationServicesStatus(context: Context): Triple<Boolean, Boolean, Boolean> {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    
    // GPS 활성화 상태
    val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    
    // 네트워크 위치 서비스 활성화 상태  
    val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    
    // 전체 위치 서비스 활성화 상태
    val locationServicesEnabled = try {
        Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_OFF
    } catch (e: Exception) {
        false
    }
    
    return Triple(gpsEnabled, networkEnabled, locationServicesEnabled)
}

// 랜덤 Y축 회전 생성
fun generateRandomYRotation(): Rotation {
    return Rotation(0f, Random.nextFloat() * 360f, 0f)
}