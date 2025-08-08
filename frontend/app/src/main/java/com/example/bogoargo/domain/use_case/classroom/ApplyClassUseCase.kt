package com.example.bogoargo.domain.use_case.classroom

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.IClassRepository
import javax.inject.Inject

class ApplyClassUseCase @Inject constructor(
    private val classRepository: IClassRepository
) {
    suspend operator fun invoke(inviteCode: String): DataResult<Unit> {
        return classRepository.joinClass(inviteCode)
    }
}