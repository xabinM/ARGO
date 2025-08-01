package com.example.bogoargo.ui.screens.ar.utils

import android.util.Log
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import com.example.bogoargo.ui.screens.ar.model.ARDebugInfo

// Geospatial API를 통한 Terrain Anchor 생성 시도
fun tryCreateTerrainAnchor(
    arSceneView: ARSceneView,
    session: Session,
    latitude: Double,
    longitude: Double,
    modelPath: String,
    onDebugInfoUpdate: (ARDebugInfo) -> Unit,
    onAnchorTypeChange: (String, ModelNode?) -> Unit
): Boolean {
    try {
        val earth = session.earth
        if (earth == null) {
            Log.w("ARScreen", "Earth is null - Geospatial API not available")
            return false
        }
        
        // Earth tracking 상태 확인
        if (earth.trackingState != TrackingState.TRACKING) {
            Log.w("ARScreen", "Earth tracking state: ${earth.trackingState} - not ready for anchors")
            return false
        }
        
        // GPS 정확도 확인 (10m 이내)
        val cameraGeospatialPose = earth.cameraGeospatialPose
        val horizontalAccuracy = cameraGeospatialPose.horizontalAccuracy
        
        if (horizontalAccuracy > 10.0f) {
            Log.w("ARScreen", "GPS accuracy too low: ${horizontalAccuracy}m (required: <10m)")
            return false
        }
        
        Log.i("ARScreen", "GPS accuracy: ${horizontalAccuracy}m - creating Terrain Anchor")
        
        // 모델 인스턴스 먼저 생성
        val modelInstance = arSceneView.modelLoader.createModelInstance(modelPath)
        if (modelInstance == null) {
            Log.w("ARScreen", "Failed to create model instance for terrain anchor")
            return false
        }
        
        // Terrain Anchor 비동기 생성 (최신 방식)
        val terrainFuture = earth.resolveAnchorOnTerrainAsync(
            latitude,
            longitude,
            0.0,  // altitude relative to terrain
            0.0f, 0.0f, 0.0f, 1.0f  // quaternion (no rotation)
        ) { anchor, terrainAnchorState ->
            // 콜백에서 처리
            when (terrainAnchorState) {
                Anchor.TerrainAnchorState.SUCCESS -> {
                    try {
                        // AnchorNode 생성 (Terrain Anchor 사용)
                        val anchorNode = AnchorNode(
                            engine = arSceneView.engine,
                            anchor = anchor
                        )
                        
                        // ModelNode 생성 및 AnchorNode에 추가
                        val modelNode = ModelNode(
                            modelInstance = modelInstance,
                            scaleToUnits = 0.5f
                        ).apply {
                            // Terrain Anchor는 바닥에 붙게 배치 (모델 중심점이 중앙이므로 아래로 이동)
                            position = Position(0.0f, 0.0f, 0.0f)
                            // Y축(수직축) 랜덤 회전 (0-360도)
                            rotation = generateRandomYRotation()
                            // 모든 애니메이션 정지
                            stopAnimation(0)
                        }
                        
                        anchorNode.addChildNode(modelNode)
                        arSceneView.addChildNode(anchorNode)
                        
                        // 앵커 타입 변경 알림
                        onAnchorTypeChange("TERRAIN_ANCHOR", modelNode)
                        
                        Log.d("ARScreen", "Terrain Anchor model loaded - ready for interaction")
                        
                        Log.i("ARScreen", "Terrain Anchor created successfully at GPS($latitude, $longitude)")
                        
                        // 성공 상태 디버그 정보 업데이트
                        onDebugInfoUpdate(ARDebugInfo(
                            targetLatitude = latitude,
                            targetLongitude = longitude,
                            anchorMethod = "TERRAIN_ANCHOR_SUCCESS",
                            actualAnchorType = "TERRAIN_ANCHOR",
                            modelLoadingStatus = "LOADED_WITH_TERRAIN_ANCHOR",
                            terrainAnchorState = "SUCCESS",
                            geospatialApiStatus = "TERRAIN_ANCHOR_CREATED"
                        ))
                        
                    } catch (e: Exception) {
                        Log.e("ARScreen", "Error setting up model with terrain anchor", e)
                        
                        // 오류 상태 디버그 정보 업데이트
                        onDebugInfoUpdate(ARDebugInfo(
                            targetLatitude = latitude,
                            targetLongitude = longitude,
                            anchorMethod = "TERRAIN_ANCHOR_ERROR",
                            modelLoadingStatus = "ERROR_SETTING_UP_MODEL",
                            terrainAnchorState = "ERROR: ${e.message}",
                            geospatialApiStatus = "MODEL_SETUP_FAILED"
                        ))
                    }
                }
                
                else -> {
                    Log.w("ARScreen", "Terrain anchor failed with state: $terrainAnchorState")
                    
                    // 실패 상태 디버그 정보 업데이트
                    onDebugInfoUpdate(ARDebugInfo(
                        targetLatitude = latitude,
                        targetLongitude = longitude,
                        anchorMethod = "TERRAIN_ANCHOR_FAILED",
                        modelLoadingStatus = "TERRAIN_ANCHOR_FAILED",
                        terrainAnchorState = terrainAnchorState.name,
                        geospatialApiStatus = "TERRAIN_ANCHOR_FAILED"
                    ))
                }
            }
        }
        
        if (terrainFuture == null) {
            Log.w("ARScreen", "Failed to initiate terrain anchor creation")
            return false
        }
        
        Log.i("ARScreen", "Terrain Anchor creation initiated at GPS($latitude, $longitude)")
        return true
        
    } catch (e: Exception) {
        Log.e("ARScreen", "Error creating terrain anchor", e)
        return false
    }
}

// 평면 기반 Anchor 생성 시도
fun tryCreatePlaneAnchor(
    arSceneView: ARSceneView,
    session: Session,
    modelPath: String,
    onDebugInfoUpdate: (ARDebugInfo) -> Unit,
    onAnchorTypeChange: (String, ModelNode?) -> Unit
): Boolean {
    try {
        // 모든 감지된 평면 가져오기
        val planes = session.getAllTrackables(Plane::class.java)
        val horizontalPlanes = planes.filter { 
            it.type == Plane.Type.HORIZONTAL_UPWARD_FACING && 
            it.trackingState == TrackingState.TRACKING 
        }
        
        Log.d("ARScreen", "Detected ${planes.size} total planes, ${horizontalPlanes.size} horizontal planes")
        
        if (horizontalPlanes.isEmpty()) {
            Log.w("ARScreen", "No horizontal planes detected for anchor placement")
            onDebugInfoUpdate(ARDebugInfo(
                anchorMethod = "NO_PLANES_DETECTED",
                modelLoadingStatus = "WAITING_FOR_PLANES",
                planesDetected = planes.size
            ))
            return false
        }
        
        // 카메라에서 가장 가까운 평면 찾기 (첫 번째 평면 선택으로 단순화)
        val nearestPlane = horizontalPlanes.firstOrNull()
        
        if (nearestPlane == null) {
            Log.w("ARScreen", "Failed to find nearest plane")
            return false
        }
        
        // 평면 중심에 Anchor 생성
        val planePose = nearestPlane.centerPose
        val planeAnchor = nearestPlane.createAnchor(planePose)
        
        // 모델 인스턴스 생성
        val modelInstance = arSceneView.modelLoader.createModelInstance(modelPath)
        if (modelInstance == null) {
            Log.w("ARScreen", "Failed to create model instance for plane anchor")
            planeAnchor.detach()
            return false
        }
        
        // AnchorNode 생성
        val anchorNode = AnchorNode(
            engine = arSceneView.engine,
            anchor = planeAnchor
        )
        
        // ModelNode 생성 및 배치
        val modelNode = ModelNode(
            modelInstance = modelInstance,
            scaleToUnits = 0.5f
        ).apply {
            // 평면 위에 바닥에 붙게 배치 (모델 중심점이 중앙이므로 아래로 이동)
            position = Position(0.0f, 0.0f, 0.0f)
            // Y축(수직축) 랜덤 회전
            rotation = generateRandomYRotation()
            // 모든 애니메이션 정지
            stopAnimation(0)
        }
        
        anchorNode.addChildNode(modelNode)
        arSceneView.addChildNode(anchorNode)
        
        // 앵커 타입 변경 알림
        onAnchorTypeChange("PLANE_ANCHOR", modelNode)
        
        Log.d("ARScreen", "Plane Anchor model loaded - ready for interaction")
        
        Log.i("ARScreen", "Plane-based anchor created successfully on detected ground plane")
        
        // 성공 상태 디버그 정보 업데이트
        onDebugInfoUpdate(ARDebugInfo(
            anchorMethod = "PLANE_ANCHOR_SUCCESS",
            actualAnchorType = "PLANE_ANCHOR",
            modelLoadingStatus = "LOADED_WITH_PLANE_ANCHOR",
            planesDetected = planes.size,
            planeAnchorUsed = true
        ))
        
        return true
        
    } catch (e: Exception) {
        Log.e("ARScreen", "Error creating plane-based anchor", e)
        onDebugInfoUpdate(ARDebugInfo(
            anchorMethod = "PLANE_ANCHOR_ERROR",
            modelLoadingStatus = "PLANE_ANCHOR_FAILED"
        ))
        return false
    }
}

// Fallback에서 Plane Anchor로 부드러운 전환 (XZ 좌표 유지, Y만 조정)
fun tryUpgradeToPlaneAnchor(
    arSceneView: ARSceneView,
    session: Session,
    currentModelNode: ModelNode,
    onDebugInfoUpdate: (ARDebugInfo) -> Unit,
    onAnchorTypeChange: (String, ModelNode?) -> Unit
): Boolean {
    try {
        // 모든 감지된 평면 가져오기
        val planes = session.getAllTrackables(Plane::class.java)
        val horizontalPlanes = planes.filter { 
            it.type == Plane.Type.HORIZONTAL_UPWARD_FACING && 
            it.trackingState == TrackingState.TRACKING 
        }
        
        if (horizontalPlanes.isEmpty()) {
            return false
        }
        
        // 현재 객체 위치 가져오기
        val currentPosition = currentModelNode.position
        
        // 현재 객체와 가장 가까운 평면 찾기
        var nearestPlane: Plane? = null
        var minDistance = Float.MAX_VALUE
        
        for (plane in horizontalPlanes) {
            val planePose = plane.centerPose
            val planeX = planePose.translation[0]
            val planeZ = planePose.translation[2]
            
            // XZ 평면에서의 거리 계산 (Y는 제외)
            val distance = kotlin.math.sqrt(
                ((currentPosition.x - planeX) * (currentPosition.x - planeX) + 
                 (currentPosition.z - planeZ) * (currentPosition.z - planeZ)).toDouble()
            ).toFloat()
            
            if (distance < minDistance) {
                minDistance = distance
                nearestPlane = plane
            }
        }
        
        if (nearestPlane == null) {
            return false
        }
        
        // 가장 가까운 평면의 높이 계산
        val planePose = nearestPlane.centerPose
        val planeY = planePose.translation[1]
        
        // 기존 XZ 좌표 유지, Y만 평면 높이로 조정 (모델 중심점 고려해서 아래로)
        val newPosition = Position(
            currentPosition.x, 
            planeY,
            currentPosition.z
        )
        
        // 부드럽게 위치 업데이트
        currentModelNode.position = newPosition
        
        Log.i("ARScreen", "Successfully upgraded Fallback to Plane Anchor - Y adjusted from ${currentPosition.y} to ${newPosition.y}")
        
        // 앵커 타입 변경 알림
        onAnchorTypeChange("PLANE_ADJUSTED", currentModelNode)
        
        // 성공 상태 디버그 정보 업데이트
        onDebugInfoUpdate(ARDebugInfo(
            anchorMethod = "PLANE_ADJUSTED",
            actualAnchorType = "PLANE_ADJUSTED",
            modelLoadingStatus = "UPGRADED_TO_PLANE",
            planesDetected = planes.size,
            planeAnchorUsed = true
        ))
        
        return true
        
    } catch (e: Exception) {
        Log.e("ARScreen", "Error upgrading to plane anchor", e)
        return false
    }
}

// Fallback: 고정 위치에 객체 배치 (평면 감지 실패 시)
fun createFallbackNode(
    arSceneView: ARSceneView, 
    modelPath: String, 
    reason: String,
    onAnchorTypeChange: (String, ModelNode?) -> Unit
) {
    try {
        Log.i("ARScreen", "Creating fallback node - reason: $reason")
        
        val modelInstance = arSceneView.modelLoader.createModelInstance(modelPath)
        if (modelInstance != null) {
            val modelNode = ModelNode(
                modelInstance = modelInstance,
                scaleToUnits = 0.5f
            ).apply {
                // 사용자 앞 2미터, 바닥에 붙게 배치 (모델 중심점이 중앙이므로 아래로 이동)
                position = Position(0.0f, -1.5f, -2.0f)
                // Y축(수직축) 랜덤 회전 (0-360도)
                rotation = generateRandomYRotation()
                // 모든 애니메이션 정지
                stopAnimation(0)
            }
            
            arSceneView.addChildNode(modelNode)
            Log.i("ARScreen", "Fallback model loaded at fixed position")
            
            // 앵커 타입 변경 알림
            onAnchorTypeChange("FALLBACK_FIXED", modelNode)
            
            Log.d("ARScreen", "Fallback model loaded - ready for interaction")
        } else {
            Log.w("ARScreen", "Failed to create fallback model instance")
            createPrimitiveNode(arSceneView)
        }
        
    } catch (e: Exception) {
        Log.e("ARScreen", "Error creating fallback node", e)
        createPrimitiveNode(arSceneView)
    }
}

// 기본 프리미티브 노드 생성 (최종 대체용)
fun createPrimitiveNode(arSceneView: ARSceneView) {
    try {
        Log.i("ARScreen", "Creating primitive fallback node")
        
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
                    // 모든 애니메이션 정지
                    stopAnimation(0)
                }
                arSceneView.addChildNode(cubeNode)
                Log.i("ARScreen", "Primitive fallback cube model loaded")
            } else {
                Log.w("ARScreen", "Failed to create primitive fallback model instance")
            }
        } catch (e: Exception) {
            Log.w("ARScreen", "Error creating primitive fallback cube node", e)
        }
    } catch (e: Exception) {
        Log.w("ARScreen", "Primitive fallback model creation skipped: ${e.message}")
        // 모델 없이도 AR 세션은 정상 동작
    }
}