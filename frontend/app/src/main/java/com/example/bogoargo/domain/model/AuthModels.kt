package com.example.bogoargo.domain.model

data class LoginRequest(
    val username: String,
    val password: String,
    val fcmToken: String?
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
)

data class TokenInfo(
    val accessToken: String,
    val refreshToken: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class RefreshTokenResponse(
    val success: Boolean,
    val tokens: TokenInfo
)