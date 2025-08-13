package com.example.bogoargo.domain.use_case.mission

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.MissionCreateResult
import com.example.bogoargo.domain.repository.IMissionRepository
import javax.inject.Inject

class CreateMissionUseCase @Inject constructor(
    private val missionRepository: IMissionRepository
) {
    suspend operator fun invoke(teamId: Long, spotId: Long): DataResult<MissionCreateResult> {
        return missionRepository.createMission(teamId, spotId)
    }
}