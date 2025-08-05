package com.example.bogoargo.data.dto.response

import java.time.LocalDateTime // 날짜/시간 처리를 위해 import

// 최상위 응답 DTO
data class MissionResponseDto(
    val success: Boolean,
    val message: String,
    val data: MissionDataDto
)

// 'data' 필드에 해당하는 DTO
data class MissionDataDto(
    val missionSession: MissionSessionDto,
    val spotInfo: SpotInfoDto,
    val mission: MissionDetailDto,
    val arObjects: List<ARObjectDto>,
    val hints: List<HintDto>
)

// 'missionSession' 필드에 해당하는 DTO
data class MissionSessionDto(
    val sessionId: String,
    val spotId: Long,
    val teamId: Long,
    val startedAt: LocalDateTime, // ISO 8601 형식 문자열을 LocalDateTime으로 파싱
    val expiresAt: LocalDateTime // ISO 8601 형식 문자열을 LocalDateTime으로 파싱
)

// 'spotInfo' 필드에 해당하는 DTO
data class SpotInfoDto(
    val spotId: Long,
    val location: LocationDto
)

// 'spotInfo.location' 필드에 해당하는 DTO
data class LocationDto(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val landmark: String
)

// 'mission' 필드에 해당하는 DTO
data class MissionDetailDto(
    val missionType: String,
    val title: String,
    val description: String,
    val instructions: List<String>, // 문자열 리스트
    val estimatedTime: Int,
    val difficulty: String
)

// 'arObjects' 배열의 각 요소에 해당하는 DTO
data class ARObjectDto(
    val objectId: String,
    val type: String,
    val position: PositionDto,
    val rotation: RotationDto,
    val scale: ScaleDto,
    val modelPath: String,
    val isInteractable: Boolean
)

// 'arObjects.position' 필드에 해당하는 DTO
data class PositionDto(
    val x: Double,
    val y: Double,
    val z: Double
)

// 'arObjects.rotation' 필드에 해당하는 DTO
data class RotationDto(
    val x: Int,
    val y: Int,
    val z: Int
)

// 'arObjects.scale' 필드에 해당하는 DTO
data class ScaleDto(
    val x: Double,
    val y: Double,
    val z: Double
)

// 'hints' 배열의 각 요소에 해당하는 DTO
data class HintDto(
    val level: Int,
    val text: String,
    val availableAfter: LocalDateTime // ISO 8601 형식 문자열을 LocalDateTime으로 파싱
)