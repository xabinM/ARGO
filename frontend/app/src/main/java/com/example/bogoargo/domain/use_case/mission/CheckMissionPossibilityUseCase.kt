package com.example.bogoargo.domain.use_case.mission

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.MissionPossibleCheckResult
import com.example.bogoargo.domain.repository.IMissionRepository
import javax.inject.Inject

class CheckMissionPossibilityUseCase @Inject constructor(
    private val missionRepository: IMissionRepository
) {
    suspend operator fun invoke(teamId: Long, spotId: Long): DataResult<MissionPossibleCheckResult> {
        return missionRepository.checkPossibleMissionSpot(teamId, spotId)
    }
}