package com.example.bogoargo.data.dto.response

data class SpotDataDto (
    val spotId: Int,
    val spotName: String,
    val latitude: Double,
    val longitude: Double
)

// 미션 위치 목록 응답 Dto
data class SpotListResponse(
    val success: Boolean,
    val message: String,
    val data: List<SpotDataDto>
)
