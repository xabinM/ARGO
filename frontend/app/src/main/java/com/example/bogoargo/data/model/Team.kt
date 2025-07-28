package com.example.bogoargo.data.model

data class Team(
    val id: String = "",
    val classId: String = "",
    val name: String = "",
    val description: String = "",
    val leaderId: String = "",
    val memberIds: List<String> = emptyList(),
    val maxMembers: Int = 4,
    val currentMembers: Int = 0,
    val color: String = "#6200EE", // 팀 컬러
    val createdAt: String = "",
    val updatedAt: String = ""
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