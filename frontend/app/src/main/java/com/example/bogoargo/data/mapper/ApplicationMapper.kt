package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.dto.response.ApplicationResponseDto
import com.example.bogoargo.data.dto.response.ApplicationDataDto
import com.example.bogoargo.domain.model.Application

fun ApplicationDataDto.toDomainModel(classId: Long): Application {
    return Application(
        applicationId = this.applicationId,
        studentId = this.studentId,
        studentName = this.studentName,
        classId = classId,
        status = this.status,
        appliedAt = this.appliedAt,
        processedAt = this.processedAt
    )
}

fun ApplicationResponseDto.toApplicationList(classId: Long): List<Application> {
    return data?.applications?.map { it.toDomainModel(classId) } ?: emptyList()
}
