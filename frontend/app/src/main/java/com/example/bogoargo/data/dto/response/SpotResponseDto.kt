package com.example.bogoargo.data.dto.response

data class SpotDataDto (
    val spotId: Int,
    val name: String,
    val description: String,
    val coordinates: Coordinates
)

// 미션 위치 목록 응답 Dto
data class SpotListResponse(
    val success: Boolean,
    val message: String,
    val data: List<SpotDataDto>
)
