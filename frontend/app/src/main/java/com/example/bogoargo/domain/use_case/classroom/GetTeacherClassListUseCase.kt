package com.example.bogoargo.domain.use_case.classroom

import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.IClassRepository
import javax.inject.Inject

class GetTeacherClassListUseCase @Inject constructor(
    private val classRepository: IClassRepository
) {
    suspend operator fun invoke(page: Int = 1, size: Int = 10, status: String? = "active"): DataResult<List<Class>> {
        return classRepository.getTeacherClassList(page, size, status)
    }
}