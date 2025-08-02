package com.example.bogoargo.data.model

import com.example.bogoargo.data.model.Team

// User Model 클래스
data class User(
    val id: Long,
    val nickname: String,
    val role: UserRole,
    val team: Team?,
    val userRole: UserRole
)
enum class UserRole {
    STUDENT,
    TEACHER
}
