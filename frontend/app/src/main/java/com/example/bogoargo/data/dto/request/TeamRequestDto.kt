package com.example.bogoargo.data.dto

import com.example.bogoargo.data.dto.response.UserDataDto

// 팀 생성 요청 Dto
data class TeamCreateRequest(
    val teamName: String,
    val maxMembers: Int
)

// 팀에 학생 배정 요청 Dto
data class TeamAssignStudentRequest(
    val studentIds: List<Long> //TODO: 유저dto에서 id만 추출해서 전달
)

// 팀 랜덤 배정 요청 Dto
data class TeatAssignRamdomRequest(
    val assignmentType: String? = "balanced"
)

