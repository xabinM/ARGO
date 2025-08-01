package com.example.bogoargo.util

import android.content.Context
import android.util.Log
import com.example.bogoargo.data.model.AR3DObject
import com.example.bogoargo.data.model.ModelFormat

/**
 * 3D 모델 파일 형식 관련 유틸리티 함수들
 */
object ModelFormatUtils {
    
    private const val TAG = "ModelFormatUtils"
    
    /**
     * Assets 폴더에서 모델 파일의 존재 여부 확인
     */
    fun isModelFileExists(context: Context, modelPath: String): Boolean {
        return try {
            val fileName = modelPath.substringAfterLast("/")
            val folderPath = modelPath.substringBeforeLast("/")
            val assetList = context.assets.list(folderPath)
            val exists = assetList?.contains(fileName) == true
            
            if (!exists) {
                Log.w(TAG, "Model file not found: $modelPath")
                Log.d(TAG, "Available files in $folderPath: ${assetList?.joinToString()}")
            }
            
            exists
        } catch (e: Exception) {
            Log.e(TAG, "Error checking model file existence: $modelPath", e)
            false
        }
    }
    
    /**
     * AR3DObject의 모델 파일 유효성 검증
     */
    fun validateAR3DObject(context: Context, arObject: AR3DObject): ValidationResult {
        val issues = mutableListOf<String>()
        
        // 1. 파일 형식 지원 여부 확인
        val format = arObject.getModelFormat()
        if (format == null) {
            issues.add("Unsupported file format: ${arObject.modelPath}")
            issues.add("Supported formats: ${ModelFormat.getSupportedExtensions().joinToString()}")
        }
        
        // 2. 모든 필요 파일들 존재 여부 확인
        val missingFiles = mutableListOf<String>()
        val requiredFiles = arObject.getRequiredFiles()
        requiredFiles.forEach { filePath ->
            if (!isModelFileExists(context, filePath)) {
                missingFiles.add(filePath)
            }
        }
        
        if (missingFiles.isNotEmpty()) {
            issues.add("Missing files: ${missingFiles.joinToString()}")
        }
        
        // 3. OBJ 형식의 경우 추가 검증
        if (format == ModelFormat.OBJ) {
            // 3-1. MTL 파일 검증
            val mtlPath = arObject.materialPath ?: arObject.getAutoMaterialPath()
            if (mtlPath != null && !isModelFileExists(context, mtlPath)) {
                issues.add("MTL file not found: $mtlPath (required for OBJ format)")
            }
            
            // 3-2. 텍스처 파일들 검증
            if (arObject.texturePaths.isNotEmpty()) {
                val missingTextures = arObject.texturePaths.filter { 
                    !isModelFileExists(context, it) 
                }
                if (missingTextures.isNotEmpty()) {
                    issues.add("Missing texture files: ${missingTextures.joinToString()}")
                }
            }
        }
        
        // 4. 스케일 값 유효성 확인
        if (arObject.scale <= 0f) {
            issues.add("Invalid scale value: ${arObject.scale} (must be > 0)")
        }
        
        // 5. ID 유효성 확인
        if (arObject.id.isBlank()) {
            issues.add("Object ID cannot be blank")
        }
        
        // 6. 표시 이름 유효성 확인
        if (arObject.displayName.isBlank()) {
            issues.add("Display name cannot be blank")
        }
        
        return ValidationResult(
            isValid = issues.isEmpty(),
            issues = issues,
            format = format,
            missingFiles = missingFiles,
            requiredFilesCount = requiredFiles.size
        )
    }
    
    /**
     * 여러 AR3DObject들의 일괄 유효성 검증
     */
    fun validateAR3DObjects(context: Context, objects: List<AR3DObject>): List<ObjectValidationResult> {
        return objects.map { obj ->
            ObjectValidationResult(
                arObject = obj,
                validationResult = validateAR3DObject(context, obj)
            )
        }
    }
    
    /**
     * 지원되지 않는 형식의 객체들 필터링
     */
    fun filterUnsupportedObjects(objects: List<AR3DObject>): FilterResult {
        val supported = objects.filter { it.isSupportedFormat() }
        val unsupported = objects.filter { !it.isSupportedFormat() }
        
        return FilterResult(
            supportedObjects = supported,
            unsupportedObjects = unsupported
        )
    }
    
    /**
     * 존재하지 않는 파일을 가진 객체들 필터링
     */
    fun filterMissingFileObjects(context: Context, objects: List<AR3DObject>): FilterResult {
        val existing = objects.filter { isModelFileExists(context, it.modelPath) }
        val missing = objects.filter { !isModelFileExists(context, it.modelPath) }
        
        return FilterResult(
            supportedObjects = existing,
            unsupportedObjects = missing
        )
    }
    
    /**
     * 모델 형식별 통계 정보
     */
    fun getFormatStatistics(objects: List<AR3DObject>): FormatStatistics {
        val formatCounts = mutableMapOf<ModelFormat?, Int>()
        var totalSupported = 0
        var totalUnsupported = 0
        
        objects.forEach { obj ->
            val format = obj.getModelFormat()
            formatCounts[format] = formatCounts.getOrDefault(format, 0) + 1
            
            if (format != null) {
                totalSupported++
            } else {
                totalUnsupported++
            }
        }
        
        return FormatStatistics(
            totalObjects = objects.size,
            supportedCount = totalSupported,
            unsupportedCount = totalUnsupported,
            formatBreakdown = formatCounts.filterKeys { it != null }
                .mapKeys { it.key!! }
        )
    }
    
    /**
     * 대체 모델 파일 경로 생성 (파일이 없는 경우)
     */
    fun generateFallbackPath(originalPath: String, preferredFormat: ModelFormat = ModelFormat.GLB): String {
        val baseName = originalPath.substringAfterLast("/").substringBeforeLast(".")
        val folder = originalPath.substringBeforeLast("/")
        return "$folder/fallback_$baseName${preferredFormat.extension}"
    }
    
    /**
     * 로그 출력용 객체 정보 포맷팅
     */
    fun validateMTLFile(context: Context, mtlPath: String): MTLValidationResult {
        val issues = mutableListOf<String>()
        val referencedTextures = mutableListOf<String>()
        
        try {
            val inputStream = context.assets.open(mtlPath)
            inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val trimmedLine = line.trim()
                    when {
                        trimmedLine.startsWith("map_Kd ") -> {
                            val texturePath = trimmedLine.substring(7).trim()
                            referencedTextures.add(texturePath)
                        }
                        trimmedLine.startsWith("map_Ks ") -> {
                            val texturePath = trimmedLine.substring(7).trim()
                            referencedTextures.add(texturePath)
                        }
                        trimmedLine.startsWith("map_Bump ") || trimmedLine.startsWith("bump ") -> {
                            val texturePath = trimmedLine.substring(if (trimmedLine.startsWith("map_Bump")) 9 else 5).trim()
                            referencedTextures.add(texturePath)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            issues.add("Failed to read MTL file: ${e.message}")
        }
        
        return MTLValidationResult(
            isValid = issues.isEmpty(),
            issues = issues,
            referencedTextures = referencedTextures
        )
    }
    
    fun formatObjectInfo(arObject: AR3DObject): String {
        val format = arObject.getModelFormat()
        val requiredFiles = arObject.getRequiredFiles()
        
        return buildString {
            append("${arObject.displayName} (${arObject.id})")
            append(" - Path: ${arObject.modelPath}")
            append(" - Format: ${format?.displayName ?: "Unknown/Unsupported"}")
            append(" - Scale: ${arObject.scale}x")
            
            if (arObject.heightOffset != 0f) {
                append(" - Height Offset: ${arObject.heightOffset}")
            }
            
            if (requiredFiles.size > 1) {
                append(" - Additional Files: ${requiredFiles.size - 1}")
                if (arObject.materialPath != null) {
                    append(" (MTL)")
                }
                if (arObject.texturePaths.isNotEmpty()) {
                    append(" (${arObject.texturePaths.size} textures)")
                }
            }
        }
    }
}

/**
 * 유효성 검증 결과
 */
data class ValidationResult(
    val isValid: Boolean,
    val issues: List<String>,
    val format: ModelFormat?,
    val missingFiles: List<String> = emptyList(),
    val requiredFilesCount: Int = 1
)

/**
 * MTL 파일 검증 결과
 */
data class MTLValidationResult(
    val isValid: Boolean,
    val issues: List<String>,
    val referencedTextures: List<String>
)

/**
 * 객체별 유효성 검증 결과
 */
data class ObjectValidationResult(
    val arObject: AR3DObject,
    val validationResult: ValidationResult
)

/**
 * 필터링 결과
 */
data class FilterResult(
    val supportedObjects: List<AR3DObject>,
    val unsupportedObjects: List<AR3DObject>
)

/**
 * 형식별 통계
 */
data class FormatStatistics(
    val totalObjects: Int,
    val supportedCount: Int,
    val unsupportedCount: Int,
    val formatBreakdown: Map<ModelFormat, Int>
)