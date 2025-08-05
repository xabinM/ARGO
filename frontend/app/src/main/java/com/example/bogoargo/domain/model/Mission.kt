package com.example.bogoargo.domain.model

data class Mission(
    val id: String = "",
    val programId: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val order: Int = 0,
    val type: MissionType = MissionType.LOCATION,
    val isRequired: Boolean = true,
    val points: Int = 0,
    val timeLimit: Int = 0, // 제한 시간 (분)
    val createdAt: String = "",
    val updatedAt: String = ""
)

enum class MissionType {
    LOCATION,   // 위치 기반 미션
    PHOTO,      // 사진 촬영 미션
    QUIZ,       // 퀴즈 미션
    SCAN        // QR/바코드 스캔 미션
}