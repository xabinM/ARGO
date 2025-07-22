package com.example.bogoargo.data.model

data class LoginRequest(
    val id: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
)

data class TokenInfo(
    val accessToken: String,
    val refreshToken: String
)