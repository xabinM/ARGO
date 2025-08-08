package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.dto.response.LocationResponseDto
import com.example.bogoargo.domain.model.Location

fun LocationResponseDto.toDomainModel(): Location {
    return Location(
        locationId = this.locationId,
        name = this.name,
        coordinates = this.coordinates
    )
}