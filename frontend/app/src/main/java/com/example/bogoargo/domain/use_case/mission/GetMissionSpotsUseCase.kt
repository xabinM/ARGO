package com.example.bogoargo.domain.use_case.mission

import com.example.bogoargo.data.repository.MissionRepositoryImpl
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.MissionSpot
import javax.inject.Inject

class GetMissionSpotsUseCase @Inject constructor(
    private val missionRepository: MissionRepositoryImpl
) {
    suspend operator fun invoke(classId: Long): DataResult<List<MissionSpot>> {
        return missionRepository.getMissionSpots(classId)
    }
}