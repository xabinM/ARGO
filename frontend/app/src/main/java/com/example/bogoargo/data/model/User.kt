package com.example.bogoargo.data.model

data class User(
    val userId: Long = 0,
    val username: String = "",
    val name: String = "",
    val role: UserRole = UserRole.STUDENT
)