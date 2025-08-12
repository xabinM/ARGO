package com.example.bogoargo.data.dto.response

data class StudentListResponseDto (
    val studentId: Int,
    val studentName: String,
    val joinedAt: String,
    val teamInfo: TeamDataDto
)