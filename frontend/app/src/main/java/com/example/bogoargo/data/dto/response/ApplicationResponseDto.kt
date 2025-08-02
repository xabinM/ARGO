package com.example.bogoargo.data.dto.response

import com.example.bogoargo.data.response.ClassDataDto

data class ApplicationDataDto(
    val applicationId: Long,
    val studentId: Long,
    val studentName: String,
    val status: String,
    val appliedAt: String,
    val processedAt: String
)

data class ApplicationResponseDto (
    val success: Boolean,
    val message: String,
    val data: ClassDataDto?,
    val applications: List<ApplicationDataDto>?
)