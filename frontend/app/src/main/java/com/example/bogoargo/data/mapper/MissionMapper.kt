package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.dto.response.*
import com.example.bogoargo.data.model.Mission
import com.example.bogoargo.data.model.MissionType
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

// MissionDataDto를 Mission Model로 변환하는 확장 함수
fun MissionDataDto.toMissionModel(): Mission {
    return Mission(
        id = this.mission.title, // title을 id로 사용
        title = this.mission.title,
        description = this.mission.description,
        location = this.spotInfo.location.address,
        latitude = this.spotInfo.location.latitude,
        longitude = this.spotInfo.location.longitude,
        type = when (this.mission.missionType.uppercase()) {
            "LOCATION" -> MissionType.LOCATION
            "PHOTO" -> MissionType.PHOTO
            "QUIZ" -> MissionType.QUIZ
            "SCAN" -> MissionType.SCAN
            else -> MissionType.LOCATION
        },
        timeLimit = this.mission.estimatedTime,
        points = 0 // DTO에 points 정보가 없어서 기본값 사용
    )
}

// MissionSessionDto 변환 함수들
fun MissionSessionDto.getSessionId(): String = this.sessionId
fun MissionSessionDto.getSpotId(): Long = this.spotId
fun MissionSessionDto.getTeamId(): Long = this.teamId
fun MissionSessionDto.getStartedAt(): LocalDateTime = this.startedAt
fun MissionSessionDto.getExpiresAt(): LocalDateTime = this.expiresAt

// SpotInfoDto 변환 함수들
fun SpotInfoDto.getSpotId(): Long = this.spotId
fun SpotInfoDto.getLatitude(): Double = this.location.latitude
fun SpotInfoDto.getLongitude(): Double = this.location.longitude
fun SpotInfoDto.getAddress(): String = this.location.address
fun SpotInfoDto.getLandmark(): String = this.location.landmark

// ARObjectDto 변환 함수들
fun ARObjectDto.getObjectId(): String = this.objectId
fun ARObjectDto.getType(): String = this.type
fun ARObjectDto.getModelPath(): String = this.modelPath
fun ARObjectDto.isInteractable(): Boolean = this.isInteractable
fun ARObjectDto.getPositionX(): Double = this.position.x
fun ARObjectDto.getPositionY(): Double = this.position.y
fun ARObjectDto.getPositionZ(): Double = this.position.z
fun ARObjectDto.getRotationX(): Int = this.rotation.x
fun ARObjectDto.getRotationY(): Int = this.rotation.y
fun ARObjectDto.getRotationZ(): Int = this.rotation.z
fun ARObjectDto.getScaleX(): Double = this.scale.x
fun ARObjectDto.getScaleY(): Double = this.scale.y
fun ARObjectDto.getScaleZ(): Double = this.scale.z

// HintDto 변환 함수들
fun HintDto.getLevel(): Int = this.level
fun HintDto.getText(): String = this.text
fun HintDto.getAvailableAfter(): LocalDateTime = this.availableAfter