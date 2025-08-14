package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.dto.response.Coordinates
import com.example.bogoargo.data.dto.response.SpotDataDto
import com.example.bogoargo.domain.model.Spot
import java.math.BigDecimal

// SpotDataDto를 위한 변환 함수들
fun SpotDataDto.getSpotId(): Int = this.spotId
fun SpotDataDto.getSpotName(): String = this.name
fun SpotDataDto.getDescription(): String = this.description
fun SpotDataDto.getCoordinates(): Coordinates = this.coordinates

// Domain model conversion functions
fun SpotDataDto.toDomain(): Spot {
    return Spot(
        spotId = this.spotId.toLong(),
        spotName = this.name,
        description = this.description,
        //coordinates = this.coordinates
        coordinates = Coordinates(
            latitude = BigDecimal("0.0"), // 테스트용 위도
            longitude = BigDecimal("0.0") // 테스트용 경도
        )
    )
}

fun List<SpotDataDto>.toDomain(): List<Spot> {
    return this.map { it.toDomain() }
}