package com.example.bogoargo.domain.use_case.location

import com.example.bogoargo.domain.model.UserCoordinates
import com.example.bogoargo.domain.repository.ILocationRepository
import javax.inject.Inject

class GetCoordinatesByClassUseCase @Inject constructor(
    private val locationRepository: ILocationRepository
) {
    suspend operator fun invoke(classId: Long): Result<List<UserCoordinates>> {
        return locationRepository.getCoordinatesByClass(classId)
    }
}