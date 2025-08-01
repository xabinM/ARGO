package com.example.bogoargo.ui.screens.ar.model

// 디버그 정보를 담는 데이터 클래스
data class ARDebugInfo(
    val currentLatitude: Double = 0.0,
    val currentLongitude: Double = 0.0,
    val targetLatitude: Double = 0.0,
    val targetLongitude: Double = 0.0,
    val gpsAccuracy: Double = 0.0,
    val earthTrackingState: String = "UNKNOWN",
    val anchorMethod: String = "NONE",
    val actualAnchorType: String = "NONE", // 실제 사용 중인 앵커 타입
    val isSessionInitialized: Boolean = false,
    val geospatialApiStatus: String = "DISABLED",
    val modelLoadingStatus: String = "NOT_STARTED",
    val terrainAnchorState: String = "NONE",
    val availableModels: List<String> = emptyList(),
    val frameCount: Long = 0,
    val gpsEnabled: Boolean = false,
    val locationServicesEnabled: Boolean = false,
    val networkConnected: Boolean = false,
    val googlePlayServicesAvailable: Boolean = false,
    val planesDetected: Int = 0,
    val planeAnchorUsed: Boolean = false,
    val animationStatus: String = "NONE" // NONE, READY, PLAYED
)