package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.dto.response.StudentCoordinatesResponseDto
import com.example.bogoargo.data.dto.response.StudentLocationInfo
import com.example.bogoargo.data.dto.response.StudentsLocationResponse
import com.example.bogoargo.data.dto.response.UserCoordinatesDto
import com.example.bogoargo.domain.model.StudentLocation
import com.example.bogoargo.domain.model.StudentLocationData
import com.example.bogoargo.domain.model.UserCoordinates
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun StudentsLocationResponse.toDomainModel(): StudentLocationData {
    return StudentLocationData(
        classId = this.classId,
        className = this.className,
        students = this.students.map { it.toDomainModel() },
        timestamp = Instant.ofEpochMilli(this.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    )
}

fun StudentLocationInfo.toDomainModel(): StudentLocation {
    return StudentLocation(
        userId = this.userId,
        userName = this.userName,
        latitude = this.latitude,
        longitude = this.longitude,
        lastUpdated = Instant.ofEpochMilli(this.lastUpdated)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    )
}

fun StudentCoordinatesResponseDto.toDomainModel(): List<UserCoordinates> {
    return this.coordinatesDtos.map { it.toDomainModel() }
}

fun UserCoordinatesDto.toDomainModel(): UserCoordinates {
    return UserCoordinates(
        userId = this.userId,
        latitude = this.latitude.toDouble(),
        longitude = this.longitude.toDouble()
    )
}