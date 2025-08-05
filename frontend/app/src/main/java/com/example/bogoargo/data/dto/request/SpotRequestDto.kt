package com.example.bogoargo.data.dto.request

data class SpotDtoRequest(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String
)