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
import com.example.bogoargo.domain.model.AR3DObject
import com.example.bogoargo.data.repository.AR3DObjectRepository

// Geospatial API를 통한 Terrain Anchor 생성 시도
fun tryCreateTerrainAnchor(
    arSceneView: ARSceneView,
    session: Session,
    latitude: Double,
    longitude: Double,
    onDebugInfoUpdate: (ARDebugInfo) -> Unit,
    onAnchorTypeChange: (String, ModelNode?) -> Unit,
    onObjectInfoUpdate: ((AR3DObject, Float) -> Unit)? = null
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
        
        // 미션용 AR 객체 선택
        val arObject = AR3DObjectRepository.getRandomMissionObject()
        val modelPath = arObject.modelPath
        Log.i("ARScreen", "Selected mission object for Terrain Anchor: ${arObject.displayName}")
        
        // 객체 정보 업데이트 콜백 호출 (초기 거리는 미정)
        onObjectInfoUpdate?.invoke(arObject, Float.MAX_VALUE)
        
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
                            scaleToUnits = arObject.scale
                        ).apply {
                            // 객체의 높이 오프셋 적용
                            position = Position(0.0f, arObject.heightOffset, 0.0f)
                            // Y축(수직축) 랜덤 회전 (0-360도)
                            rotation = generateRandomYRotation()
                            Log.d("ARScreen", "Model prepared with scale: ${arObject.scale}, heightOffset: ${arObject.heightOffset}")
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
    onDebugInfoUpdate: (ARDebugInfo) -> Unit,
    onAnchorTypeChange: (String, ModelNode?) -> Unit,
    onObjectInfoUpdate: ((AR3DObject, Float) -> Unit)? = null
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
        
        // 미션용 AR 객체 선택
        val arObject = AR3DObjectRepository.getRandomMissionObject()
        val modelPath = arObject.modelPath
        Log.i("ARScreen", "Selected mission object for Plane Anchor: ${arObject.displayName}")
        
        // 객체 정보 업데이트 콜백 호출 (초기 거리는 미정)
        onObjectInfoUpdate?.invoke(arObject, Float.MAX_VALUE)
        
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
            scaleToUnits = arObject.scale
        ).apply {
            // 객체의 높이 오프셋 적용
            position = Position(0.0f, arObject.heightOffset, 0.0f)
            // Y축(수직축) 랜덤 회전
            rotation = generateRandomYRotation()
            Log.d("ARScreen", "Model prepared with scale: ${arObject.scale}, heightOffset: ${arObject.heightOffset}")
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
    arObject: AR3DObject,
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
        
        // 기존 XZ 좌표 유지, Y만 평면 높이로 조정 (객체의 heightOffset 고려)
        val newPosition = Position(
            currentPosition.x, 
            planeY + arObject.heightOffset,
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
    reason: String,
    onAnchorTypeChange: (String, ModelNode?) -> Unit,
    onObjectInfoUpdate: ((AR3DObject, Float) -> Unit)? = null
) {
    try {
        Log.i("ARScreen", "Creating fallback node - reason: $reason")
        
        // FallBack용 랜덤 객체 선택
        val fallbackObject = AR3DObjectRepository.getRandomFallbackObject()
        val fallbackModelPath = fallbackObject.modelPath
        
        // 선택된 객체의 상세 정보 로깅
        Log.i("ARScreen", "=== FALLBACK OBJECT SELECTED ===")
        Log.i("ARScreen", "ID: ${fallbackObject.id}")
        Log.i("ARScreen", "Display Name: ${fallbackObject.displayName}")
        Log.i("ARScreen", "Model Path: ${fallbackModelPath}")
        Log.i("ARScreen", "Format: ${fallbackObject.getModelFormat()?.displayName ?: "Unknown"}")
        Log.i("ARScreen", "Scale: ${fallbackObject.scale}")
        Log.i("ARScreen", "Placement Type: ${fallbackObject.placementType}")
        
        
        // 객체 정보 업데이트 콜백 호출 (초기 거리는 미정)
        onObjectInfoUpdate?.invoke(fallbackObject, Float.MAX_VALUE)
        
        // 파일 존재 여부 확인 로깅
        Log.i("ARScreen", "=== FILE EXISTENCE CHECK ===")
        try {
            val assetManager = arSceneView.context.assets
            
            // 주 모델 파일 확인
            val modelExists = try {
                assetManager.open(fallbackModelPath).use { true }
            } catch (e: Exception) {
                false
            }
            Log.i("ARScreen", "Main model file exists: $modelExists ($fallbackModelPath)")
            
        } catch (e: Exception) {
            Log.w("ARScreen", "Error checking file existence", e)
        }
        
        Log.i("ARScreen", "=== ATTEMPTING MODEL LOADING ===")
        Log.i("ARScreen", "Trying to load model: $fallbackModelPath")
        
        val modelInstance = arSceneView.modelLoader.createModelInstance(fallbackModelPath)
        if (modelInstance != null) {
            Log.i("ARScreen", "✅ Model instance created successfully!")
            
            val modelNode = ModelNode(
                modelInstance = modelInstance,
                scaleToUnits = fallbackObject.scale
            ).apply {
                // 사용자 주변 랜덤 위치에 바닥에 붙게 배치 (1.5m~5m 범위, 360도)
                val randomPos = generateRandomPositionAroundUser()
                position = Position(randomPos.x, randomPos.y + fallbackObject.heightOffset, randomPos.z)
                // Y축(수직축) 랜덤 회전 (0-360도)
                rotation = generateRandomYRotation()
                Log.d("ARScreen", "Fallback model prepared: ${fallbackObject.displayName}")
            }
            
            arSceneView.addChildNode(modelNode)
            Log.i("ARScreen", "✅ Fallback model loaded successfully: ${fallbackObject.displayName} at random position")
            
            // 앵커 타입 변경 알림
            onAnchorTypeChange("FALLBACK_FIXED", modelNode)
            
            Log.d("ARScreen", "Fallback model loaded - ready for interaction")
        } else {
            Log.w("ARScreen", "❌ Failed to create fallback model instance!")
            Log.w("ARScreen", "Model path that failed: $fallbackModelPath")
            Log.w("ARScreen", "Model format: ${fallbackObject.getModelFormat()?.displayName ?: "Unknown"}")
            Log.w("ARScreen", "Falling back to primitive node (Royal Seal Box)")
            createPrimitiveNode(arSceneView, onAnchorTypeChange, onObjectInfoUpdate)
        }
        
    } catch (e: Exception) {
        Log.e("ARScreen", "❌ Error creating fallback node", e)
        Log.e("ARScreen", "Exception details: ${e.message}")
        Log.e("ARScreen", "Falling back to primitive node (Royal Seal Box)")
        createPrimitiveNode(arSceneView, onAnchorTypeChange, onObjectInfoUpdate)
    }
}

// 평면 품질 평가
data class PlaneQuality(
    val plane: Plane,
    val score: Float,
    val distance: Float,
    val size: Float,
    val stability: Float
)

// 평면 품질 계산
fun evaluatePlaneQuality(plane: Plane, currentPosition: Position): PlaneQuality {
    val planePose = plane.centerPose
    val planeX = planePose.translation[0]
    val planeZ = planePose.translation[2]
    
    // 거리 계산
    val distance = kotlin.math.sqrt(
        ((currentPosition.x - planeX) * (currentPosition.x - planeX) + 
         (currentPosition.z - planeZ) * (currentPosition.z - planeZ)).toDouble()
    ).toFloat()
    
    // 평면 크기 계산 (extent 사용)
    val size = plane.extentX * plane.extentZ
    
    // 안정성 점수 (추적 상태 고려)
    val stability = when (plane.trackingState) {
        TrackingState.TRACKING -> 1.0f
        TrackingState.PAUSED -> 0.5f
        else -> 0.0f
    }
    
    // 종합 점수 계산 (거리가 가까울수록, 크기가 클수록, 안정성이 높을수록 좋음)
    val distanceScore = kotlin.math.max(0.0f, 1.0f - (distance / 5.0f)) // 5m 이내에서 최대 점수
    val sizeScore = kotlin.math.min(1.0f, size / 2.0f) // 2㎡ 이상이면 최대 점수
    val score = (distanceScore * 0.4f + sizeScore * 0.3f + stability * 0.3f)
    
    return PlaneQuality(plane, score, distance, size, stability)
}

// 동적 평면 조정 시도
fun tryDynamicPlaneAdjustment(
    arSceneView: ARSceneView,
    session: Session,
    currentModelNode: ModelNode,
    arObject: AR3DObject,
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
        
        val currentPosition = currentModelNode.position
        
        // 현재 위치에서 모든 평면의 품질 평가
        val planeQualities = horizontalPlanes.map { plane ->
            evaluatePlaneQuality(plane, currentPosition)
        }.sortedByDescending { it.score }
        
        val bestPlane = planeQualities.firstOrNull()
        if (bestPlane == null || bestPlane.score < 0.3f) {
            return false
        }
        
        // 현재 평면과 최고 평면 비교
        val currentPlaneScore = planeQualities.find { quality ->
            val planePose = quality.plane.centerPose
            val planeY = planePose.translation[1]
            kotlin.math.abs(currentPosition.y - arObject.heightOffset - planeY) < 0.1f
        }?.score ?: 0.0f
        
        // 현재 평면보다 훨씬 좋은 평면이 있는 경우에만 이동
        if (bestPlane.score > currentPlaneScore + 0.2f) {
            val planePose = bestPlane.plane.centerPose
            val planeY = planePose.translation[1]
            
            // 부드러운 위치 조정 (Y축만 조정)
            val newPosition = Position(
                currentPosition.x,
                planeY + arObject.heightOffset,
                currentPosition.z
            )
            
            // 부드러운 애니메이션으로 위치 업데이트
            smoothUpdatePosition(currentModelNode, newPosition)
            
            Log.i("ARScreen", "Dynamically adjusted to better plane - quality improved from $currentPlaneScore to ${bestPlane.score}")
            
            // 앵커 타입 변경 알림
            onAnchorTypeChange("PLANE_DYNAMICALLY_ADJUSTED", currentModelNode)
            
            // 디버그 정보 업데이트
            onDebugInfoUpdate(ARDebugInfo(
                anchorMethod = "PLANE_DYNAMICALLY_ADJUSTED",
                actualAnchorType = "PLANE_ADJUSTED",
                modelLoadingStatus = "DYNAMICALLY_ADJUSTED",
                planesDetected = planes.size,
                planeAnchorUsed = true
            ))
            
            return true
        }
        
        return false
        
    } catch (e: Exception) {
        Log.e("ARScreen", "Error in dynamic plane adjustment", e)
        return false
    }
}

// 부드러운 위치 업데이트 (애니메이션)
fun smoothUpdatePosition(modelNode: ModelNode, targetPosition: Position) {
    try {
        // 현재는 즉시 업데이트, 추후 애니메이션 추가 가능
        modelNode.position = targetPosition
        
        // TODO: 향후 SceneView의 애니메이션 API를 사용하여 부드러운 전환 구현
        // modelNode.animate().position(targetPosition).setDuration(500).start()
        
    } catch (e: Exception) {
        Log.e("ARScreen", "Error updating model position", e)
        // 실패 시 즉시 업데이트
        modelNode.position = targetPosition
    }
}

// 기본 프리미티브 노드 생성 (최종 대체용)
fun createPrimitiveNode(
    arSceneView: ARSceneView,
    onAnchorTypeChange: ((String, ModelNode?) -> Unit)? = null,
    onObjectInfoUpdate: ((AR3DObject, Float) -> Unit)? = null
) {
    try {
        Log.i("ARScreen", "Creating primitive fallback node")
        
        // 왕실 인장함 AR3DObject 가져오기
        val royalSealObject = AR3DObjectRepository.getObjectById("royal_seal_box")
        if (royalSealObject == null) {
            Log.e("ARScreen", "Royal Seal Box object not found in repository")
            return
        }
        
        Log.i("ARScreen", "Using Royal Seal Box object: ${royalSealObject.displayName}")
        
        // 동기적으로 기본 GLB 모델 생성 시도 (지원되는 형식 사용)
        val fallbackPath = royalSealObject.modelPath
        try {
            val modelInstance = arSceneView.modelLoader.createModelInstance(fallbackPath)
            if (modelInstance != null) {
                val royalSealNode = ModelNode(
                    modelInstance = modelInstance,
                    scaleToUnits = royalSealObject.scale
                ).apply {
                    // 사용자 주변 랜덤 위치에 바닥에 붙게 배치 (1.5m~5m 범위, 360도)
                    val randomPos = generateRandomPositionAroundUser()
                    position = Position(randomPos.x, randomPos.y + royalSealObject.heightOffset, randomPos.z)
                    // Y축(수직축) 랜덤 회전 (0-360도)
                    rotation = generateRandomYRotation()
                    Log.d("ARScreen", "Primitive fallback model prepared (Royal Seal Box) at random position")
                }
                arSceneView.addChildNode(royalSealNode)
                Log.i("ARScreen", "Primitive fallback Royal Seal Box model loaded")
                
                // 상태 업데이트 콜백 호출 (평면 추적을 위해)
                onAnchorTypeChange?.invoke("PRIMITIVE_FALLBACK", royalSealNode)
                
                // 객체 정보 업데이트 콜백 호출 (UI 정보 업데이트를 위해, 초기 거리는 미정)
                onObjectInfoUpdate?.invoke(royalSealObject, Float.MAX_VALUE)
            } else {
                Log.w("ARScreen", "Failed to create primitive fallback Royal Seal Box model instance")
            }
        } catch (e: Exception) {
            Log.w("ARScreen", "Error creating primitive fallback Royal Seal Box node", e)
        }
    } catch (e: Exception) {
        Log.w("ARScreen", "Primitive fallback model creation skipped: ${e.message}")
        // 모델 없이도 AR 세션은 정상 동작
    }
}