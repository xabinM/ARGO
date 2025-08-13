package com.example.bogoargo.data.dto.response

import java.math.BigDecimal


data class StudentsLocationResponse(
    val classId: Long,
    val className: String,
    val students: List<StudentLocationInfo>,
    val timestamp: Long
)

data class StudentLocationInfo(
    val userId: Long,
    val userName: String,
    val latitude: Double,
    val longitude: Double,
    val lastUpdated: Long
)

data class StudentCoordinatesResponseDto(
    val success: Boolean,
    val message: String,
    val coordinatesDtos: List<UserCoordinatesDto>
)

data class UserCoordinatesDto(
    val userId: Long,
    val latitude: BigDecimal,
    val longitude: BigDecimal
)