package com.example.bogoargo.domain.use_case.mission

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.Mission
import com.example.bogoargo.domain.repository.IMissionRepository
import javax.inject.Inject

class GetMissionUseCase @Inject constructor(
    private val missionRepository: IMissionRepository
) {
    suspend fun getMissions(): DataResult<List<Mission>> {
        return missionRepository.getMissions()
    }
    
    suspend fun getMissionById(missionId: Long): DataResult<Mission> {
        return missionRepository.getMissionById(missionId)
    }
    
    suspend fun getMissionProgress(missionId: Long): DataResult<Int> {
        return missionRepository.getMissionProgress(missionId)
    }
}