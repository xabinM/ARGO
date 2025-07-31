package com.example.bogoargo.data.dto.response

// 팀 정보 응답 DTO
data class TeamDataDto (
    val teamId: Int,
    val teamName: String,
    val maxMembers: Int? = 99,
    val currentMembers: Int,
    val createdAt: String,
    val classId: Long,
    val className: String
)

// 팀 생성 결과 응답 DTO
data class TeamCreateResponse(
    val success: Boolean,
    val message: String,
    val data: TeamDataDto?
)

// 팀 배치 결과 응답 Dto
data class TeamAssignResponse(
    val success: Boolean,
    val message: String,
    // (배치된 학생 목록)
)

// 팀 삭제 결과 응답 Dto
data class TeamDeleteResponse(
    val success: Boolean,
    val message: String,
    val students: List<UserDataDto>?
)
