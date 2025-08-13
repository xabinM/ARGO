package com.example.bogoargo.domain.model

data class MissionSpot(
    val spotId: Long,
    val name: String,
    val description: String,
    val coordinates: SpotCoordinates
)

data class SpotCoordinates(
    val latitude: Double,
    val longitude: Double
)

data class MissionSpotsResponse(
    val success: Boolean,
    val message: String,
    val data: List<MissionSpot>
)