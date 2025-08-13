package com.example.bogoargo.domain.use_case.classroom

import com.example.bogoargo.data.response.ClassMemberResponse
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.IClassRepository
import javax.inject.Inject

class GetClassMemberListUseCase @Inject constructor(
    private val classRepository: IClassRepository
) {
    suspend operator fun invoke(
        classId: Long,
        status: String,
        page: Int = 1,
        size: Int = 10
    ): DataResult<ClassMemberResponse> {
        return classRepository.getClassMemberList(classId, status, page, size)
    }
}