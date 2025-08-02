package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.dto.response.ApplicationResponseDto
import com.example.bogoargo.data.model.Application

fun ApplicationResponseDto.toDomainModel(): Application {
    return Application(
        status = when (this.status) {
            "APPLIED" -> Application.ApplicationStatus.APPLIED
            "CANCELED" -> Application.ApplicationStatus.CANCELED
        },
        message = this.message
    )
}