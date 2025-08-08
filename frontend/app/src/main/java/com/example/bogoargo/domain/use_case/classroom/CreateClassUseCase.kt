package com.example.bogoargo.domain.use_case.classroom

import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.IClassRepository
import javax.inject.Inject

class CreateClassUseCase @Inject constructor(
    private val classRepository: IClassRepository
) {
    suspend operator fun invoke(
        className: String,
        description: String,
        location: String,
        activityDate: String,
        maxStudents: Int
    ): DataResult<Class> {
        return classRepository.createClass(className, description, location, activityDate, maxStudents)
    }
}