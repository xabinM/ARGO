package com.example.bogoargo.data.dto.response

import java.math.BigDecimal

data class LocationResponseDto (
    val success: Boolean,
    val message: String,
    val data: List<LocationDataDto>
)
data class LocationDataDto (
    val locationId: Long,
    val name: String,
    val coordinates: Coordinates
)

data class Coordinates (
    val latitude: BigDecimal,
    val longitude: BigDecimal
)