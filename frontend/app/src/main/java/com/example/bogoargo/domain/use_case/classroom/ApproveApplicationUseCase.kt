package com.example.bogoargo.domain.use_case.classroom

import com.example.bogoargo.data.dto.response.MessageResponseDto
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.IClassRepository
import javax.inject.Inject

class ApproveApplicationUseCase @Inject constructor(
    private val classRepository: IClassRepository
) {
    suspend operator fun invoke(
        classId: Long,
        action: String,
        applicationIds: List<Long>
    ): DataResult<MessageResponseDto> {
        return classRepository.approveApplication(classId, action, applicationIds)
    }
    
    suspend fun approveApplications(
        classId: Long,
        applicationIds: List<Long>
    ): DataResult<MessageResponseDto> {
        return invoke(classId, "APPROVE", applicationIds)
    }
    
    suspend fun rejectApplications(
        classId: Long,
        applicationIds: List<Long>
    ): DataResult<MessageResponseDto> {
        return invoke(classId, "REJECT", applicationIds)
    }
}