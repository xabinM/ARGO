package com.example.bogoargo.data.dto.response

import com.example.bogoargo.data.response.ClassDataDto
import java.time.LocalDateTime

data class ApplicationDataDto(
    val applicationId: Long,
    val user: UserDataDto,
    val classRoom: ClassDataDto,
    val status: String,
    val processedAt: LocalDateTime
)

data class ApplicationResponseDto (
    val success: Boolean,
    val message: String,
    val data: ClassDataDto?,
    val applications: List<ApplicationDataDto>?
)