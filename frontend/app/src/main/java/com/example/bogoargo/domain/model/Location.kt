package com.example.bogoargo.domain.model

import com.example.bogoargo.data.dto.response.Coordinates

data class Location (
    val locationId: Long,
    val name: String,
    val coordinates: Coordinates
)