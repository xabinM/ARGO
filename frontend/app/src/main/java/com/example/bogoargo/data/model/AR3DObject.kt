package com.example.bogoargo.data.model

/**
 * AR 객체의 배치 타입 (FallBack vs GeoSpatial 미션)
 */
enum class PlacementType {
    FALLBACK_ONLY,    // FallBack 모드에서만 사용 (일반적인 객체들)
    MISSION_ONLY,     // GeoSpatial 미션 지점에서만 사용 (특별한 객체들)
    BOTH             // 둘 다 사용 가능
}

/**
 * 지원되는 3D 모델 파일 형식
 */
enum class ModelFormat(
    val extension: String, 
    val displayName: String,
    val requiresMaterialFile: Boolean = false,
    val requiresTextureFiles: Boolean = false,
    val materialExtension: String? = null
) {
    GLB(".glb", "GLB (Binary glTF)"),
    GLTF(".gltf", "glTF (JSON)"),
    OBJ(".obj", "Wavefront OBJ", requiresMaterialFile = true, requiresTextureFiles = true, materialExtension = ".mtl");
    
    /**
     * 이 형식이 추가 파일들을 필요로 하는지 확인
     */
    fun hasAdditionalFiles(): Boolean {
        return requiresMaterialFile || requiresTextureFiles
    }
    
    
    companion object {
        /**
         * 파일 경로에서 모델 형식 감지
         */
        fun fromPath(path: String): ModelFormat? {
            val lowerPath = path.lowercase()
            return values().find { lowerPath.endsWith(it.extension) }
        }
        
        /**
         * 지원되는 모든 확장자 목록
         */
        fun getSupportedExtensions(): List<String> {
            return values().map { it.extension }
        }
        
        /**
         * 추가 파일이 필요한 형식들
         */
        fun getFormatsRequiringAdditionalFiles(): List<ModelFormat> {
            return values().filter { it.hasAdditionalFiles() }
        }
    }
}

/**
 * AR 환경에서 표시될 3D 객체의 정보를 담는 데이터 클래스
 */
data class AR3DObject(
    val id: String,                           // 고유 식별자
    val displayName: String,                  // 화면에 표시될 이름
    val modelPath: String,                    // 3D 모델 파일 경로 (assets 폴더 기준)
    val materialPath: String? = null,         // 매테리얼 파일 경로 (OBJ의 경우 MTL 파일)
    val texturePaths: List<String> = emptyList(), // 텍스처 파일 경로 목록
    val thumbnailPath: String? = null,        // 미리보기 이미지 경로
    val scale: Float = 0.5f,                  // 렌더링 크기 (기본값: 0.5f)
    val category: String? = null,             // 카테고리 (동물, 건물, 아이템 등)
    val description: String? = null,          // 객체 설명
    val heightOffset: Float = 0f,             // Y축 오프셋 (바닥으로부터의 높이 조정)
    val placementType: PlacementType = PlacementType.BOTH  // 배치 타입 (FallBack/Mission/Both)
) {
    companion object {
        const val DEFAULT_INTERACTION_DISTANCE = 2.0f // 기본 상호작용 거리
    }
    
    /**
     * 모델 파일 형식 가져오기
     */
    fun getModelFormat(): ModelFormat? {
        return ModelFormat.fromPath(modelPath)
    }
    
    /**
     * 모델 파일 이름 (확장자 제외)
     */
    fun getModelFileName(): String {
        return modelPath.substringAfterLast("/").substringBeforeLast(".")
    }
    
    /**
     * 모델 파일이 지원되는 형식인지 확인
     */
    fun isSupportedFormat(): Boolean {
        return getModelFormat() != null
    }
    
    /**
     * 객체가 상호작용 가능한 거리인지 확인
     */
    fun isWithinInteractionDistance(distance: Float): Boolean {
        return distance <= DEFAULT_INTERACTION_DISTANCE
    }
    
    /**
     * 이 모델에 필요한 모든 파일 목록 반환
     */
    fun getRequiredFiles(): List<String> {
        val files = mutableListOf(modelPath)
        
        // 매테리얼 파일 추가
        materialPath?.let { files.add(it) }
        
        // 텍스처 파일들 추가
        files.addAll(texturePaths)
        
        return files
    }
    
    /**
     * 자동으로 MTL 파일 경로 생성 (OBJ 파일인 경우)
     */
    fun getAutoMaterialPath(): String? {
        val format = getModelFormat()
        if (format == ModelFormat.OBJ && materialPath == null) {
            // modelPath에서 확장자를 .mtl로 변경
            return modelPath.substringBeforeLast(".") + ".mtl"
        }
        return materialPath
    }
    
    /**
     * 모델 형식이 추가 파일들을 필요로 하는지 확인
     */
    fun requiresAdditionalFiles(): Boolean {
        return getModelFormat()?.hasAdditionalFiles() == true
    }
    
    /**
     * 모델 정보 요약
     */
    fun getModelInfo(): String {
        val format = getModelFormat()?.displayName ?: "Unknown"
        val additionalFiles = if (requiresAdditionalFiles()) {
            val count = getRequiredFiles().size - 1 // 메인 모델 파일 제외
            " + ${count} files"
        } else {
            ""
        }
        return "$displayName ($format$additionalFiles, ${scale}x)"
    }
}