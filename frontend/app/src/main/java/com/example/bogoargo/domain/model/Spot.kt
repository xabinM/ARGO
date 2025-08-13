package com.example.bogoargo.domain.model

import com.example.bogoargo.data.dto.response.Coordinates

data class Spot(
    val spotId: Long,
    val spotName: String,
    val description: String,
    val coordinates: Coordinates
)