package com.example.bogoargo.data.dto.request

import com.example.bogoargo.data.dto.response.UserDataDto
import com.example.bogoargo.data.response.ClassDataDto
import java.time.LocalDateTime

data class ApplicationRequestDto(
    val action: String,
    val applicationIds: List<Long>
)
