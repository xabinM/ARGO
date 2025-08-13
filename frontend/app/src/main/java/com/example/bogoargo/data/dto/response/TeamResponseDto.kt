package com.example.bogoargo.data.dto.response


// 팀 정보 응답 DTO
data class TeamDataDto (
    val teamId: Long,
    val teamName: String,
    val maxMembers: Int,
    val currentMembers: Int,
    val createdAt: String,
    val classId: Long,
    val className: String,
    val assignedAt: String?
)

// 팀 리스트 응답 DTO
data class TeamSummary (
    val totalTeams: Int,
    val assignedStudents: Int,
    val unassignedStudents: Int,
    val teams: List<TeamDataDto>
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
