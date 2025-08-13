package com.example.bogoargo.domain.use_case.mission

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.MissionSubmitResult
import com.example.bogoargo.domain.repository.IMissionRepository
import javax.inject.Inject

// TODO: 셀피 미션 UseCase (백엔드 API 완성 후 구현)
class SubmitSelfieMissionUseCase @Inject constructor(
    private val missionRepository: IMissionRepository
) {
    suspend operator fun invoke(missionId: Long, imageBase64: String, pose: String): DataResult<MissionSubmitResult> {
        return missionRepository.submitSelfieMission(missionId, imageBase64, pose)
    }
}

class ValidateSelfieUseCase @Inject constructor(
    private val missionRepository: IMissionRepository
) {
    suspend operator fun invoke(imageBase64: String, pose: String): DataResult<Boolean> {
        return missionRepository.validateSelfie(imageBase64, pose)
    }
}