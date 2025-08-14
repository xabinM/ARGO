package com.example.bogoargo.domain.model

import com.example.bogoargo.data.dto.response.UserDataDto
import com.example.bogoargo.data.response.ClassDataDto
import java.time.LocalDateTime

// 초대
data class Application(
    val applicationId: Long,
    val studentId: Long,
    val studentName: String,
    val classId: Long,
    val status: String,
    val appliedAt: String,
    val processedAt: String?
)
