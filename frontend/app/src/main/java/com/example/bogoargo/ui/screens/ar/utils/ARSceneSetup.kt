package com.example.bogoargo.ui.screens.ar.utils

import android.util.Log
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import com.example.bogoargo.ui.screens.ar.model.ARDebugInfo
import com.example.bogoargo.domain.model.AR3DObject

fun setupARScene(
    arSceneView: ARSceneView,
    session: Session,
    spotId: Long,
    latitude: Double,
    longitude: Double,
    onMissionComplete: () -> Unit, // 미션 발견 시 호출되는 콜백 (실제로는 발견 처리)
    onDebugInfoUpdate: (ARDebugInfo) -> Unit,
    onModelNodeUpdate: (ModelNode?) -> Unit,
    onObjectClick: ((ModelNode, Float) -> Boolean)? = null, // 객체 클릭 핸들러 (거리 포함)
    onObjectInfoUpdate: ((AR3DObject, Float) -> Unit)? = null // 객체 정보 및 거리 업데이트 콜백 추가
) {
    Log.d("ARScreen", "Setting up AR scene for mission spot $spotId at GPS($latitude, $longitude)")
    
    // 모델 파일 목록 수집
    val availableModels = try {
        arSceneView.context.assets.list("models")?.toList() ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    
    var frameCount = 0L
    
    // 초기 디버그 정보 업데이트
    onDebugInfoUpdate(ARDebugInfo(
        targetLatitude = latitude,
        targetLongitude = longitude,
        anchorMethod = "INITIALIZING",
        modelLoadingStatus = "SCENE_SETUP_STARTED",
        availableModels = availableModels
    ))
    
    // Earth tracking이 안정화되기까지 대기 (90프레임 = 약 3초)
    var waitFrameCount = 0
    var hasTriedTerrainAnchor = false
    var hasAnyAnchorSucceeded = false // 앵커 성공 여부 플래그
    
    // 현재 앵커 상태 추적 (mutable state로 관리)
    var currentAnchorType = "NONE" // 실제 사용 중인 앵커 타입
    var localModelNode: ModelNode? = null
    var currentARObject: AR3DObject? = null // 현재 사용 중인 AR 객체 정보 추적
    var localAnimationPlayed = false // 로컬 애니메이션 상태
    var objectWorldPosition: Position? = null // 객체의 월드 좌표
    
    // 상태 업데이트 함수 (중앙화된 상태 관리)
    fun updateAnchorState(anchorType: String, modelNode: ModelNode?) {
        currentAnchorType = anchorType
        localModelNode = modelNode
        localAnimationPlayed = false // 새 객체이므로 애니메이션 상태 초기화
        
        // 객체의 월드 좌표 업데이트
        objectWorldPosition = modelNode?.worldPosition
        
        onModelNodeUpdate(modelNode) // 컴포넌트 레벨로 상태 전달
        Log.d("ARScreen", "Anchor state updated: $anchorType, Model: ${modelNode != null}, Animation: $localAnimationPlayed, Position: $objectWorldPosition")
    }
    
    // 현재 애니메이션 상태 확인 함수 (중앙화된 상태 관리)
    fun getAnimationStatus(): String {
        return when {
            localModelNode == null -> "NONE"
            localAnimationPlayed -> "PLAYED"
            else -> "READY"
        }
    }
    
    arSceneView.onFrame = { frame ->
        frameCount++
        waitFrameCount++
        
        // 매 프레임마다 Geospatial API 상태 확인 및 디버그 정보 업데이트
        try {
            val earth = session.earth
            val (gpsEnabled, networkEnabled, locationServicesEnabled) = checkLocationServicesStatus(arSceneView.context)
            
            // 거리 계산
            val currentPosition = objectWorldPosition ?: localModelNode?.worldPosition
            val distance = if (currentPosition != null) {
                calculateDistanceToObject(arSceneView, currentPosition)
            } else {
                Float.MAX_VALUE
            }
            
            val isInteractable = isObjectInteractable(distance)
            
            val debugInfo = if (earth != null) {
                val cameraGeospatialPose = earth.cameraGeospatialPose
                val planes = session.getAllTrackables(Plane::class.java)
                val horizontalPlanes = planes.filter { 
                    it.type == Plane.Type.HORIZONTAL_UPWARD_FACING && 
                    it.trackingState == TrackingState.TRACKING 
                }
                
                ARDebugInfo(
                    currentLatitude = cameraGeospatialPose.latitude,
                    currentLongitude = cameraGeospatialPose.longitude,
                    targetLatitude = latitude,
                    targetLongitude = longitude,
                    gpsAccuracy = cameraGeospatialPose.horizontalAccuracy,
                    earthTrackingState = earth.trackingState.name,
                    anchorMethod = when {
                        !hasTriedTerrainAnchor && waitFrameCount < 90 -> "WAITING_FOR_GPS (${90 - waitFrameCount})"
                        currentAnchorType == "NONE" -> "CREATING_FALLBACK"
                        currentAnchorType == "FALLBACK_FIXED" && horizontalPlanes.isNotEmpty() -> "UPGRADING_TO_PLANE"
                        earth.trackingState == TrackingState.TRACKING && cameraGeospatialPose.horizontalAccuracy <= 10.0f -> "TERRAIN_ANCHOR_READY"
                        else -> "STABLE"
                    },
                    actualAnchorType = currentAnchorType, // 실제 사용 중인 앵커 타입
                    isSessionInitialized = true,
                    geospatialApiStatus = if (earth.trackingState == TrackingState.TRACKING) "ENABLED_TRACKING" else "ENABLED_${earth.trackingState.name}",
                    modelLoadingStatus = "LOADED",
                    terrainAnchorState = "READY",
                    availableModels = availableModels,
                    frameCount = frameCount,
                    gpsEnabled = gpsEnabled,
                    locationServicesEnabled = locationServicesEnabled,
                    networkConnected = networkEnabled,
                    googlePlayServicesAvailable = true,
                    planesDetected = horizontalPlanes.size,
                    planeAnchorUsed = currentAnchorType.contains("PLANE"),
                    animationStatus = getAnimationStatus(),
                    distanceToObject = distance,
                    isObjectInteractable = isInteractable,
                    objectPosition = currentPosition?.let { "X:${String.format("%.2f", it.x)}, Y:${String.format("%.2f", it.y)}, Z:${String.format("%.2f", it.z)}" } ?: "UNKNOWN"
                )
            } else {
                ARDebugInfo(
                    targetLatitude = latitude,
                    targetLongitude = longitude,
                    earthTrackingState = "EARTH_NULL",
                    anchorMethod = when {
                        !hasTriedTerrainAnchor && waitFrameCount < 90 -> "WAITING_FOR_GPS (${90 - waitFrameCount})"
                        currentAnchorType == "NONE" -> "CREATING_FALLBACK"
                        else -> "STABLE"
                    },
                    actualAnchorType = currentAnchorType, // 실제 사용 중인 앵커 타입
                    isSessionInitialized = true,
                    geospatialApiStatus = "DISABLED",
                    modelLoadingStatus = "LOADED",
                    availableModels = availableModels,
                    frameCount = frameCount,
                    gpsEnabled = gpsEnabled,
                    locationServicesEnabled = locationServicesEnabled,
                    networkConnected = networkEnabled,
                    googlePlayServicesAvailable = true,
                    planesDetected = 0,
                    planeAnchorUsed = currentAnchorType.contains("PLANE"),
                    animationStatus = getAnimationStatus(),
                    distanceToObject = distance,
                    isObjectInteractable = isInteractable,
                    objectPosition = currentPosition?.let { "X:${String.format("%.2f", it.x)}, Y:${String.format("%.2f", it.y)}, Z:${String.format("%.2f", it.z)}" } ?: "UNKNOWN"
                )
            }
            
            // 30프레임마다 디버그 정보 업데이트 (성능 고려)
            if (frameCount % 30 == 0L) {
                onDebugInfoUpdate(debugInfo)
            }
            
            // 현재 AR 객체가 있고 거리가 계산되었다면 UI에 업데이트
            val arObject = currentARObject
            if (arObject != null && distance != Float.MAX_VALUE) {
                onObjectInfoUpdate?.invoke(arObject, distance)
            }
            
            // 90프레임 후에 Terrain Anchor 시도
            if (waitFrameCount >= 90 && !hasTriedTerrainAnchor) {
                hasTriedTerrainAnchor = true
                
                // 1단계: Terrain Anchor 시도 (Geospatial API 사용)
                val terrainAnchorSuccess = tryCreateTerrainAnchor(
                    arSceneView, session, latitude, longitude, onDebugInfoUpdate,
                    onAnchorTypeChange = { anchorType, modelNode ->
                        if (anchorType == "TERRAIN_ANCHOR" && modelNode != null) {
                            updateAnchorState(anchorType, modelNode)
                            hasAnyAnchorSucceeded = true // 앵커 성공 플래그 설정
                        }
                    },
                    onObjectInfoUpdate = { arObject, distance ->
                        currentARObject = arObject
                        onObjectInfoUpdate?.invoke(arObject, distance)
                    }
                )
                if (terrainAnchorSuccess) {
                    Log.i("ARScreen", "Successfully created Terrain Anchor at GPS($latitude, $longitude)")
                } else {
                    // 2단계: Plane Anchor 시도 (Terrain Anchor 실패 시)
                    Log.i("ARScreen", "Terrain Anchor failed, trying Plane Anchor")
                    val planeAnchorSuccess = tryCreatePlaneAnchor(
                        arSceneView, session, onDebugInfoUpdate,
                        onAnchorTypeChange = { anchorType, modelNode ->
                            if (anchorType == "PLANE_ANCHOR" && modelNode != null) {
                                updateAnchorState(anchorType, modelNode)
                                hasAnyAnchorSucceeded = true // 앵커 성공 플래그 설정
                            }
                        },
                        onObjectInfoUpdate = { arObject, distance ->
                        currentARObject = arObject
                        onObjectInfoUpdate?.invoke(arObject, distance)
                    }
                    )
                    if (planeAnchorSuccess) {
                        Log.i("ARScreen", "Successfully created Plane Anchor")
                    }
                }
            }
            
            // Fallback Anchor 시도 (모든 앵커 방식 실패 시)
            if (waitFrameCount >= 120 && hasTriedTerrainAnchor && currentAnchorType == "NONE" && !hasAnyAnchorSucceeded) {
                // 모든 앵커 방식이 실패했으므로 Fallback 생성
                Log.i("ARScreen", "Creating fallback anchor - All anchor methods failed")
                onDebugInfoUpdate(ARDebugInfo(
                    targetLatitude = latitude,
                    targetLongitude = longitude,
                    anchorMethod = "FALLBACK_FIXED",
                    actualAnchorType = "FALLBACK_FIXED",
                    modelLoadingStatus = "USING_FALLBACK",
                    geospatialApiStatus = "FALLBACK_MODE",
                    availableModels = availableModels
                ))
                createFallbackNode(arSceneView, "All anchor methods failed",
                    onAnchorTypeChange = { anchorType, modelNode ->
                        if ((anchorType == "FALLBACK_FIXED" || anchorType == "PRIMITIVE_FALLBACK") && modelNode != null) {
                            updateAnchorState(anchorType, modelNode)
                        }
                    },
                    onObjectInfoUpdate = { arObject, distance ->
                        currentARObject = arObject
                        onObjectInfoUpdate?.invoke(arObject, distance)
                    }
                )
            }
            
            // 지속적인 평면 모니터링 및 동적 위치 조정
            if (localModelNode != null) {
                when (currentAnchorType) {
                    // Fallback에서 Plane Anchor로 업그레이드 시도
                    "FALLBACK_FIXED", "PRIMITIVE_FALLBACK" -> {
                        val arObject = currentARObject
                        if (waitFrameCount > 120 && arObject != null) {
                            val upgraded = tryUpgradeToPlaneAnchor(
                                arSceneView, session, localModelNode!!, arObject, onDebugInfoUpdate
                            ) { anchorType, modelNode ->
                                if (anchorType == "PLANE_ADJUSTED" && modelNode != null) {
                                    updateAnchorState(anchorType, modelNode)
                                }
                            }
                            
                            if (upgraded) {
                                Log.i("ARScreen", "Successfully upgraded Fallback to Plane Anchor")
                            }
                        }
                    }
                    
                    // 평면 기반 앵커의 지속적인 품질 모니터링
                    "PLANE_ANCHOR", "PLANE_ADJUSTED" -> {
                        // 30프레임(약 1초)마다 평면 품질 확인
                        val arObject = currentARObject
                        if (frameCount % 30 == 0L && arObject != null) {
                            val dynamicallyAdjusted = tryDynamicPlaneAdjustment(
                                arSceneView, session, localModelNode!!, arObject, onDebugInfoUpdate
                            ) { anchorType, modelNode ->
                                if (anchorType == "PLANE_DYNAMICALLY_ADJUSTED" && modelNode != null) {
                                    updateAnchorState("PLANE_ADJUSTED", modelNode)
                                }
                            }
                            
                            if (dynamicallyAdjusted) {
                                Log.d("ARScreen", "Dynamically adjusted object position to better plane")
                            }
                        }
                    }
                }
            }
            
            if (earth != null) {
                val cameraGeospatialPose = earth.cameraGeospatialPose
                
                // 상세한 진단 로깅 (Earth tracking 상태가 변경될 때만)
                if (frameCount % 60 == 0L) { // 2초마다 상세 로그
                    Log.d("ARScreen", "=== Earth Tracking Diagnosis ===")
                    Log.d("ARScreen", "Earth tracking state: ${earth.trackingState}")
                    Log.d("ARScreen", "GPS accuracy: ${cameraGeospatialPose.horizontalAccuracy}m")
                    Log.d("ARScreen", "Current position: ${cameraGeospatialPose.latitude}, ${cameraGeospatialPose.longitude}")
                    Log.d("ARScreen", "Altitude: ${cameraGeospatialPose.altitude}m")
                    Log.d("ARScreen", "GPS enabled: $gpsEnabled, Location services: $locationServicesEnabled, Network: $networkEnabled")
                    Log.d("ARScreen", "Wait frames: $waitFrameCount/60, Has tried terrain anchor: $hasTriedTerrainAnchor")
                    
                    when (earth.trackingState) {
                        TrackingState.STOPPED -> {
                            Log.w("ARScreen", "Earth tracking STOPPED - possible causes:")
                            Log.w("ARScreen", "  - Location services disabled: ${!locationServicesEnabled}")
                            Log.w("ARScreen", "  - GPS disabled: ${!gpsEnabled}")
                            Log.w("ARScreen", "  - Network location disabled: ${!networkEnabled}")
                            Log.w("ARScreen", "  - Insufficient permissions or Google Play Services issue")
                        }
                        TrackingState.PAUSED -> {
                            Log.i("ARScreen", "Earth tracking PAUSED - initializing or temporary loss")
                        }
                        TrackingState.TRACKING -> {
                            Log.i("ARScreen", "Earth tracking ACTIVE - GPS accuracy: ${cameraGeospatialPose.horizontalAccuracy}m")
                        }
                    }
                    Log.d("ARScreen", "=== End Diagnosis ===")
                }
                
                Log.v("ARScreen", "GPS accuracy: ${cameraGeospatialPose.horizontalAccuracy}m, " +
                        "Earth tracking: ${earth.trackingState}")
            } else {
                if (frameCount % 60 == 0L) {
                    Log.e("ARScreen", "Earth object is NULL - Geospatial API not properly initialized")
                    Log.e("ARScreen", "  - Check ARCore installation and Google Play Services")
                    Log.e("ARScreen", "  - GPS enabled: $gpsEnabled, Location services: $locationServicesEnabled")
                    Log.e("ARScreen", "  - Wait frames: $waitFrameCount/60, Has tried terrain anchor: $hasTriedTerrainAnchor")
                }
            }
        } catch (e: Exception) {
            Log.v("ARScreen", "Earth tracking check failed: ${e.message}")
            
            // 오류 상태도 디버그 정보로 업데이트
            if (frameCount % 30 == 0L) {
                onDebugInfoUpdate(ARDebugInfo(
                    targetLatitude = latitude,
                    targetLongitude = longitude,
                    earthTrackingState = "ERROR: ${e.message}",
                    anchorMethod = if (!hasTriedTerrainAnchor && waitFrameCount < 90) "WAITING_FOR_GPS (${90 - waitFrameCount})" else "ERROR_FALLBACK",
                    actualAnchorType = currentAnchorType,
                    isSessionInitialized = true,
                    geospatialApiStatus = "ERROR",
                    modelLoadingStatus = "ERROR",
                    availableModels = availableModels,
                    frameCount = frameCount
                ))
            }
        }
    }
    
    // 객체 클릭 처리 함수
    fun handleObjectClick(x: Float, y: Float): Boolean {
        val modelNode = localModelNode
        if (modelNode == null) {
            Log.d("ARScreen", "No model node available for interaction")
            return false
        }
        
        // 현재 객체까지의 거리 계산
        val currentPosition = objectWorldPosition ?: modelNode.worldPosition
        val distance = if (currentPosition != null) {
            calculateDistanceToObject(arSceneView, currentPosition)
        } else {
            Float.MAX_VALUE
        }
        
        Log.d("ARScreen", "Object interaction attempt - Distance: ${formatDistance(distance)}")
        
        // 거리 확인 (2m 이내)
        if (!isObjectInteractable(distance)) {
            Log.d("ARScreen", "Object too far for interaction: ${formatDistance(distance)}")
            return false
        }
        
        // 객체 클릭 핸들러 호출
        val handled = onObjectClick?.invoke(modelNode, distance) ?: false
        
        if (handled) {
            Log.i("ARScreen", "Object interaction successful at distance: ${formatDistance(distance)}")
            // 미션 발견 처리 (완료가 아닌 발견으로 변경)
            onMissionComplete()
        }
        
        return handled
    }
    
    // 외부에서 접근 가능하도록 태그로 저장
    arSceneView.setTag("ar_object_click_handler".hashCode(), ::handleObjectClick)
}