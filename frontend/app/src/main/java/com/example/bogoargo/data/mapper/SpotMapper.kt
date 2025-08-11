package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.dto.response.SpotDataDto
import com.example.bogoargo.domain.model.Spot

// SpotDataDto를 위한 변환 함수들
fun SpotDataDto.getSpotId(): Int = this.spotId
fun SpotDataDto.getSpotName(): String = this.spotName
fun SpotDataDto.getLatitude(): Double = this.latitude
fun SpotDataDto.getLongitude(): Double = this.longitude

// Domain model conversion functions
fun SpotDataDto.toDomain(): Spot {
    return Spot(
        spotId = this.spotId.toLong(),
        spotName = this.spotName,
        latitude = this.latitude,
        longitude = this.longitude
    )
}

fun List<SpotDataDto>.toDomain(): List<Spot> {
    return this.map { it.toDomain() }
}