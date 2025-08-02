package com.example.bogoargo.ui.screens.ar.utils

import android.util.Log
import com.example.bogoargo.data.model.AR3DObject
import com.example.bogoargo.data.model.PlacementType
import com.example.bogoargo.data.repository.AR3DObjectRepository

/**
 * 미션에 따라 적절한 AR 객체를 선택하는 유틸리티 클래스
 */
object MissionObjectSelector {
    
    private const val TAG = "MissionObjectSelector"
    
    /**
     * 미션 타입과 조건에 따라 AR 객체 선택
     */
    fun selectObjectForMission(
        missionType: MissionType,
        category: String? = null,
        specificObjectId: String? = null
    ): AR3DObject {
        Log.d(TAG, "Selecting object for mission type: $missionType, category: $category")
        
        return when (missionType) {
            MissionType.RANDOM -> {
                // 완전 랜덤 선택
                AR3DObjectRepository.getRandomObject().also {
                    Log.d(TAG, "Selected random object: ${it.displayName}")
                }
            }
            
            MissionType.CATEGORY_BASED -> {
                // 카테고리 기반 선택
                if (category != null) {
                    val categoryObjects = AR3DObjectRepository.getObjectsByCategory(category)
                    if (categoryObjects.isNotEmpty()) {
                        categoryObjects.random().also {
                            Log.d(TAG, "Selected object from category '$category': ${it.displayName}")
                        }
                    } else {
                        Log.w(TAG, "No objects found for category: $category, falling back to random")
                        AR3DObjectRepository.getRandomObject()
                    }
                } else {
                    Log.w(TAG, "Category not specified, falling back to random")
                    AR3DObjectRepository.getRandomObject()
                }
            }
            
            MissionType.SPECIFIC -> {
                // 특정 객체 선택
                if (specificObjectId != null) {
                    AR3DObjectRepository.getObjectById(specificObjectId)?.also {
                        Log.d(TAG, "Selected specific object: ${it.displayName}")
                    } ?: run {
                        Log.w(TAG, "Specific object not found: $specificObjectId, falling back to random")
                        AR3DObjectRepository.getRandomObject()
                    }
                } else {
                    Log.w(TAG, "Specific object ID not provided, falling back to random")
                    AR3DObjectRepository.getRandomObject()
                }
            }
            
            MissionType.MIXED -> {
                // 카테고리를 고려한 선택
                AR3DObjectRepository.getRandomObjectWithCondition(
                    category = category
                ).also {
                    Log.d(TAG, "Selected mixed criteria object: ${it.displayName}")
                }
            }
        }
    }
    
    
    /**
     * 미션 완료 횟수에 따른 다음 객체 선택
     */
    fun selectNextObjectBasedOnProgress(
        completedMissions: Int,
        currentCategory: String? = null
    ): AR3DObject {
        // 10개 미션마다 카테고리 변경
        val shouldChangeCategory = completedMissions > 0 && completedMissions % 10 == 0
        
        return if (shouldChangeCategory && currentCategory != null) {
            // 다른 카테고리에서 선택
            val allCategories = AR3DObjectRepository.getAvailableCategories()
            val otherCategories = allCategories.filter { it != currentCategory }
            val newCategory = otherCategories.randomOrNull() ?: currentCategory
            
            AR3DObjectRepository.getRandomObjectWithCondition(
                category = newCategory
            )
        } else {
            // 같은 카테고리에서 선택
            AR3DObjectRepository.getRandomObjectWithCondition(
                category = currentCategory
            )
        }.also {
            Log.d(TAG, "Selected next object based on progress ($completedMissions missions): ${it.displayName}")
        }
    }
    
    /**
     * 미션용 랜덤 객체 선택 (GeoSpatial 모드에서 사용)
     */
    fun selectMissionObject(
        category: String? = null,
        specificObjectId: String? = null
    ): AR3DObject {
        Log.d(TAG, "Selecting mission object for category: $category, specificId: $specificObjectId")
        
        return when {
            specificObjectId != null -> {
                // 특정 객체 선택
                AR3DObjectRepository.getObjectById(specificObjectId)?.also {
                    Log.d(TAG, "Selected specific mission object: ${it.displayName}")
                } ?: run {
                    Log.w(TAG, "Specific mission object not found: $specificObjectId, falling back to random mission object")
                    AR3DObjectRepository.getRandomMissionObject()
                }
            }
            
            category != null -> {
                // 카테고리별 미션 객체 선택
                val missionObjects = AR3DObjectRepository.getObjectsForMission()
                val categoryObjects = missionObjects.filter { it.category == category }
                
                if (categoryObjects.isNotEmpty()) {
                    categoryObjects.random().also {
                        Log.d(TAG, "Selected mission object from category '$category': ${it.displayName}")
                    }
                } else {
                    Log.w(TAG, "No mission objects found for category: $category, falling back to random mission object")
                    AR3DObjectRepository.getRandomMissionObject()
                }
            }
            
            else -> {
                // 랜덤 미션 객체 선택
                AR3DObjectRepository.getRandomMissionObject().also {
                    Log.d(TAG, "Selected random mission object: ${it.displayName}")
                }
            }
        }
    }
    
    /**
     * FallBack용 랜덤 객체 선택
     */
    fun selectFallbackObject(category: String? = null): AR3DObject {
        Log.d(TAG, "Selecting fallback object for category: $category")
        
        return if (category != null) {
            val fallbackObjects = AR3DObjectRepository.getObjectsForFallback()
            val categoryObjects = fallbackObjects.filter { it.category == category }
            
            if (categoryObjects.isNotEmpty()) {
                categoryObjects.random().also {
                    Log.d(TAG, "Selected fallback object from category '$category': ${it.displayName}")
                }
            } else {
                Log.w(TAG, "No fallback objects found for category: $category, falling back to random fallback object")
                AR3DObjectRepository.getRandomFallbackObject()
            }
        } else {
            AR3DObjectRepository.getRandomFallbackObject().also {
                Log.d(TAG, "Selected random fallback object: ${it.displayName}")
            }
        }
    }
}

/**
 * 미션 타입 정의
 */
enum class MissionType {
    RANDOM,           // 완전 랜덤
    CATEGORY_BASED,   // 특정 카테고리에서 선택
    SPECIFIC,         // 특정 객체 지정
    MIXED            // 카테고리를 고려한 선택
}