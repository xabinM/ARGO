package com.example.bogoargo.domain.use_case.classroom

import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.IClassRepository
import javax.inject.Inject

class GetClassesUseCase @Inject constructor(
    private val classRepository: IClassRepository
) {
    suspend operator fun invoke(): DataResult<List<Class>> {
        return classRepository.getClasses()
    }
}