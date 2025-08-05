package com.example.bogoargo.domain.use_case.mission

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.IMissionRepository
import javax.inject.Inject

class ManageMissionUseCase @Inject constructor(
    private val missionRepository: IMissionRepository
) {
    suspend fun startMission(missionId: Long): DataResult<Unit> {
        return missionRepository.startMission(missionId)
    }
    
    suspend fun completeMission(missionId: Long): DataResult<Unit> {
        return missionRepository.completeMission(missionId)
    }
}