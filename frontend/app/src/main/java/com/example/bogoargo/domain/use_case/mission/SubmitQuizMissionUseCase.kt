package com.example.bogoargo.domain.use_case.mission

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.MissionSubmitResult
import com.example.bogoargo.domain.repository.IMissionRepository
import javax.inject.Inject

class SubmitQuizMissionUseCase @Inject constructor(
    private val missionRepository: IMissionRepository
) {
    suspend operator fun invoke(missionId: Long, isSuccess: Boolean): DataResult<MissionSubmitResult> {
        return missionRepository.submitQuizMission(missionId, isSuccess)
    }
}