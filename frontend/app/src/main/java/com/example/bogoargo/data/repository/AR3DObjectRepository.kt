package com.example.bogoargo.data.repository

import com.example.bogoargo.domain.model.AR3DObject
import com.example.bogoargo.domain.model.ModelFormat
import com.example.bogoargo.domain.model.PlacementType

/**
 * AR 3D 객체들을 관리하는 저장소 (하드코딩)
 */
object AR3DObjectRepository {
    
    private val objects = listOf(
        // GLB 형식 객체들 (실제 파일 존재)
        AR3DObject(
            id = "royal_seal_box",
            displayName = "왕실 인장함",
            modelPath = "models/royalsealbox.glb",
            thumbnailPath = "images/objects/royal_seal_box_thumb.png",
            scale = 0.8f,
            category = "문화재",
            description = "조선시대 왕실에서 사용했던 인장을 보관하는 상자입니다.",
            placementType = PlacementType.FALLBACK_ONLY
        ),
        
        // GLB 형식 객체들
        AR3DObject(
            id = "bodhisattva_statue",
            displayName = "금동 반가사유상",
            modelPath = "models/bodhisattva_statue.glb",
            thumbnailPath = "images/objects/bodhisattva_statue_thumb.png",
            scale = 0.6f,
            category = "문화재",
            description = "한국의 대표적인 불교 조각상인 금동 반가사유상입니다. 국보급 문화재를 AR로 체험해보세요.",
            placementType = PlacementType.MISSION_ONLY
        ),
        
        // 나전경함 (고려시대 보물)
        AR3DObject(
            id = "mother_of_pearl_sutra_case",
            displayName = "나전경함",
            modelPath = "models/mother_of_pearl_sutra_case_goryeo.glb",
            thumbnailPath = "images/objects/mother_of_pearl_sutra_case_thumb.JPG",
            scale = 0.7f,
            category = "문화재",
            description = "고려 후기 나전으로 장식된 불교 경전함입니다. 모란당초문이 새겨진 보물급 문화재입니다.",
            placementType = PlacementType.FALLBACK_ONLY
        )
        // 새로운 3D 모델 추가 가이드:
        // 1. GLB/GLTF 파일을 assets/models/ 폴더에 추가
        // 2. 위의 리스트에 AR3DObject 추가
        // 3. modelPath에 올바른 확장자 사용 (.glb, .gltf)
    )
    
    /**
     * 모든 객체 목록 반환
     */
    fun getAllObjects(): List<AR3DObject> = objects
    
    /**
     * ID로 특정 객체 조회
     */
    fun getObjectById(id: String): AR3DObject? {
        return objects.find { it.id == id }
    }
    
    /**
     * 카테고리별 객체 조회
     */
    fun getObjectsByCategory(category: String): List<AR3DObject> {
        return objects.filter { it.category == category }
    }
    
    
    /**
     * 랜덤 객체 반환
     */
    fun getRandomObject(): AR3DObject {
        return objects.random()
    }
    
    /**
     * 조건에 맞는 랜덤 객체 반환
     */
    fun getRandomObjectWithCondition(
        category: String? = null,
        format: ModelFormat? = null,
        onlySupported: Boolean = true
    ): AR3DObject {
        var filteredObjects = if (onlySupported) getValidObjects() else objects
        
        category?.let {
            filteredObjects = filteredObjects.filter { obj -> obj.category == it }
        }
        
        format?.let {
            filteredObjects = filteredObjects.filter { obj -> obj.getModelFormat() == it }
        }
        
        return filteredObjects.randomOrNull() ?: getRandomObject()
    }
    
    /**
     * 사용 가능한 카테고리 목록
     */
    fun getAvailableCategories(): List<String> {
        return objects.mapNotNull { it.category }.distinct()
    }
    
    /**
     * 객체 개수 반환
     */
    fun getObjectCount(): Int = objects.size
    
    /**
     * 모델 형식별 객체 조회
     */
    fun getObjectsByFormat(format: ModelFormat): List<AR3DObject> {
        return objects.filter { it.getModelFormat() == format }
    }
    
    /**
     * 지원되는 모델 형식 목록
     */
    fun getSupportedFormats(): List<ModelFormat> {
        return objects.mapNotNull { it.getModelFormat() }.distinct()
    }
    
    /**
     * 지원되지 않는 형식의 객체 필터링
     */
    fun getUnsupportedObjects(): List<AR3DObject> {
        return objects.filter { !it.isSupportedFormat() }
    }
    
    /**
     * 지원되는 형식의 객체만 반환
     */
    fun getValidObjects(): List<AR3DObject> {
        return objects.filter { it.isSupportedFormat() }
    }
    
    /**
     * FallBack 모드에서 사용할 수 있는 객체들 반환
     */
    fun getObjectsForFallback(): List<AR3DObject> {
        return objects.filter { 
            it.placementType == PlacementType.FALLBACK_ONLY || 
            it.placementType == PlacementType.BOTH 
        }.filter { it.isSupportedFormat() }
    }
    
    /**
     * GeoSpatial 미션에서 사용할 수 있는 객체들 반환
     */
    fun getObjectsForMission(): List<AR3DObject> {
        return objects.filter { 
            it.placementType == PlacementType.MISSION_ONLY || 
            it.placementType == PlacementType.BOTH 
        }.filter { it.isSupportedFormat() }
    }
    
    /**
     * FallBack용 랜덤 객체 반환
     */
    fun getRandomFallbackObject(): AR3DObject {
        val fallbackObjects = getObjectsForFallback()
        return fallbackObjects.randomOrNull() ?: getRandomObject()
    }
    
    /**
     * 미션용 랜덤 객체 반환
     */
    fun getRandomMissionObject(): AR3DObject {
        val missionObjects = getObjectsForMission()
        return missionObjects.randomOrNull() ?: getRandomObject()
    }
    
    /**
     * 배치 타입별 객체 조회
     */
    fun getObjectsByPlacementType(placementType: PlacementType): List<AR3DObject> {
        return when (placementType) {
            PlacementType.FALLBACK_ONLY -> objects.filter { it.placementType == PlacementType.FALLBACK_ONLY }
            PlacementType.MISSION_ONLY -> objects.filter { it.placementType == PlacementType.MISSION_ONLY }
            PlacementType.BOTH -> objects.filter { it.placementType == PlacementType.BOTH }
        }.filter { it.isSupportedFormat() }
    }
}