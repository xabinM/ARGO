package com.example.bogoargo.data.dto.request

data class UserCoordinatesRequest(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null
)