package com.example.bogoargo.domain.use_case.classroom

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.StudentClassDetail
import com.example.bogoargo.domain.repository.IClassRepository
import javax.inject.Inject

class GetStudentClassDetailUseCase @Inject constructor(
    private val classRepository: IClassRepository
) {
    suspend operator fun invoke(classId: Long, include: String? = null): DataResult<StudentClassDetail> {
        return classRepository.getStudentClassDetail(classId, include)
    }
}