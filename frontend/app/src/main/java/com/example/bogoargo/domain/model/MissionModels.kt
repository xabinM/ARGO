package com.example.bogoargo.domain.model

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