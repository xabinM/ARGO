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

data class MissionSpot(
    val spotId: Long,
    val spotName: String,
    val latitude: Double,
    val longitude: Double
)

data class MissionSpotsResponse(
    val success: Boolean,
    val message: String,
    val data: List<MissionSpot>
)