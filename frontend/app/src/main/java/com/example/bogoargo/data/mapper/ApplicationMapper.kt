package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.dto.response.ApplicationResponseDto
import com.example.bogoargo.data.dto.response.ApplicationDataDto
import com.example.bogoargo.domain.model.Application

fun ApplicationDataDto.toDomainModel(): Application {
    return Application(
        applicationId = applicationId,
        user = user,
        classRoom = classRoom,
        status = status,
        processedAt = processedAt
    )
}

fun ApplicationResponseDto.toApplicationList(): List<Application> {
    return applications?.map { it.toDomainModel() } ?: emptyList()
}