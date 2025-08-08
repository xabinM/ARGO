package com.example.bogoargo.domain.use_case.classroom

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.IClassRepository
import javax.inject.Inject

class DeleteClassUseCase @Inject constructor(
    private val classRepository: IClassRepository
) {
    suspend operator fun invoke(classId: Long): DataResult<Unit> {
        return classRepository.deleteClass(classId)
    }
}