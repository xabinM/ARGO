package com.example.bogoargo.data.dto.request

import java.math.BigDecimal

data class UserCoordinatesRequest(
    val latitude: BigDecimal,
    val longitude: BigDecimal
)