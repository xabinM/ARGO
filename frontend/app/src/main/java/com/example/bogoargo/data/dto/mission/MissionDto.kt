package com.example.bogoargo.data.dto.mission

import com.google.gson.annotations.SerializedName

// 미션 생성 응답 DTO
data class MissionCreateResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("missionId") val missionId: Long,
    @SerializedName("problem") val problem: ProblemDetailDto?
)

// 문제 상세 DTO (모든 필드를 포함하는 통합 클래스)
data class ProblemDetailDto(
    @SerializedName("id") val id: Long,
    @SerializedName("dtype") val dtype: String,
    // 퀴즈 문제 필드들 (nullable)
    @SerializedName("question") val question: String? = null,
    @SerializedName("choices") val choices: List<String>? = null,
    @SerializedName("correctIndex") val correctIndex: Int? = null,
    @SerializedName("explanation") val explanation: String? = null,
    // 셀피 문제 필드들 (nullable)
    @SerializedName("guideline") val guideline: String? = null,
    @SerializedName("pose") val pose: String? = null,
    @SerializedName("poseHint") val poseHint: String? = null
)

// 미션 제출 요청 DTO
data class MissionSubmitRequestDto(
    @SerializedName("success") val success: Boolean
)

// 미션 제출 응답 DTO
data class MissionSubmitResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("cardId") val cardId: Long?,
    @SerializedName("tier") val tier: String?
)

// 셀피 미션 제출 요청 DTO (TODO: 백엔드 API 완성 후 구현)
data class SelfieMissionSubmitRequestDto(
    @SerializedName("imageBase64") val imageBase64: String,
    @SerializedName("pose") val pose: String
)

// 셀피 미션 검증 응답 DTO (TODO: 백엔드 API 완성 후 구현)
data class SelfieMissionValidationResponseDto(
    @SerializedName("isValid") val isValid: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("confidence") val confidence: Float?
)

// 미션 지점 가능 여부 확인 응답 DTO
data class MissionPossibleCheckResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?
)