package com.example.bogoargo.domain.model

// User Model 클래스
data class User(
    //val userId: Long,
    val name: String,
    val role: UserRole,
    val team: Team?,
)
