package com.example.bogoargo.domain.use_case.classroom

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.TeaCherClassDetail
import com.example.bogoargo.domain.repository.IClassRepository
import javax.inject.Inject

class GetTeacherClassDetailUseCase @Inject constructor(
    private val classRepository: IClassRepository
    ) {
        suspend operator fun invoke(classId: Long): DataResult<TeaCherClassDetail?> {
            return classRepository.getTeacherClassById(classId)
        }
    }