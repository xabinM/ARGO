package com.example.bogoargo.data.model

import com.example.bogoargo.data.model.Team

// User Model 클래스
data class User(
    val userId: Long,
    val name: String,
    val role: UserRole,
    val team: Team?,
)
