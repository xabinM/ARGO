package com.example.bogoargo.data.model

import com.example.bogoargo.data.dto.response.UserDataDto
import com.example.bogoargo.data.response.ClassDataDto
import java.time.LocalDateTime

// 초대
data class Application(
    val applicationId: Long,
    val user: UserDataDto,
    val classRoom: ClassDataDto,
    val status: String,
    val processedAt: LocalDateTime
)
