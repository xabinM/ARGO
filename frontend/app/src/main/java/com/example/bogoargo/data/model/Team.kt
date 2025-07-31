package com.example.bogoargo.data.model

import java.time.LocalDate

data class Team(
    val id: Long,
    val classId: Long,
    val name: String,
    val maxMembers: Int,
    val currentMembers: Int,
    val createdAt: LocalDate,
)

data class TeamMember(
    val id: String = "",
    val teamId: String = "",
    val userId: String = "",
    val userName: String = "",
    val role: TeamRole = TeamRole.MEMBER,
    val joinedAt: String = ""
)

enum class TeamRole {
    LEADER,     // 팀장
    MEMBER      // 팀원
}