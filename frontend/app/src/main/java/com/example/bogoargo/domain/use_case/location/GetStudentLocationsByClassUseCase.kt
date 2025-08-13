package com.example.bogoargo.domain.use_case.location

import com.example.bogoargo.domain.model.StudentLocationData
import com.example.bogoargo.domain.repository.ILocationRepository
import javax.inject.Inject

class GetStudentLocationsByClassUseCase @Inject constructor(
    private val locationRepository: ILocationRepository
) {
    suspend operator fun invoke(classId: Long): Result<StudentLocationData> {
        return locationRepository.getStudentLocationsByClass(classId)
    }
}