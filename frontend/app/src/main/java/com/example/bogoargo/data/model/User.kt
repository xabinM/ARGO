package com.example.bogoargo.domain.model

import com.example.bogoargo.data.model.Team

// User Model 클래스
data class User(
    val id: Long,
    val nickname: String,
    val role: UserRole,
    val team: Team?
) {
    enum class UserRole {
        STUDENT,
        TEACHER
    }
}