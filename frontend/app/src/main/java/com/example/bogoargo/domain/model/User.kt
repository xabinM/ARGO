package com.example.bogoargo.domain.model

// User Model 클래스
data class User(
    val userId: Long = 1L, //TODO: null처리 해지
    val name: String,
    val role: UserRole,
    val team: Team?,
)
