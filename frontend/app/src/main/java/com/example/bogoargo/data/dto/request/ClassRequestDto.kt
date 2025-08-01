package com.example.bogoargo.data.dto

import com.example.bogoargo.data.dto.request.ApplicationRequestDto

// 반 생성 요청 DTO
data class ClassCreateRequest(
    val className: String,
    val description: String,
    val location: String,
    val activityDate: String,
    val maxStudents: Int
)

// 신청 학생 승인 요청 DTO
data class ApproveStudentRequest(
    val applicationList: List<ApplicationRequestDto>?,
    val action: String
)

// 참여 신청 요청 Dto
data class ApplyClassRequest(
    val inviteCode: String
)

