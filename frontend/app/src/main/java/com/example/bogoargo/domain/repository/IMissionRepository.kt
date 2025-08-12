package com.example.bogoargo.domain.repository

import com.example.bogoargo.domain.model.*

interface IMissionRepository {
    suspend fun getMissions(): DataResult<List<Mission>>
    suspend fun getMissionById(missionId: Long): DataResult<Mission>
    suspend fun startMission(missionId: Long): DataResult<Unit>
    suspend fun completeMission(missionId: Long): DataResult<Unit>
    suspend fun getMissionProgress(missionId: Long): DataResult<Int>
    
    // 새로운 미션 관련 메서드들
    suspend fun createMission(teamId: Long, spotId: Long): DataResult<MissionCreateResult>
    suspend fun submitQuizMission(missionId: Long, isSuccess: Boolean): DataResult<MissionSubmitResult>
    
    // TODO: 셀피 미션 관련 메서드들 (백엔드 API 완성 후 구현)
    suspend fun submitSelfieMission(missionId: Long, imageBase64: String, pose: String): DataResult<MissionSubmitResult>
    suspend fun validateSelfie(imageBase64: String, pose: String): DataResult<Boolean>
    
    // 미션 지점 가능 여부 확인
    suspend fun checkPossibleMissionSpot(teamId: Long, spotId: Long): DataResult<MissionPossibleCheckResult>
}