package com.example.bogoargo.domain.use_case.classroom

import com.example.bogoargo.domain.model.Application
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.IClassRepository
import javax.inject.Inject

class GetApplicationListUseCase @Inject constructor(
    private val classRepository: IClassRepository
    ) {
    suspend operator fun invoke(classId: Long): DataResult<List<Application>> {
        return classRepository.getApplicationList(classId)
    }
}