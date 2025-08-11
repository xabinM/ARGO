package com.example.bogoargo.data.dto.mission

import com.google.gson.annotations.SerializedName

// 미션 생성 응답 DTO
data class MissionCreateResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("missionId") val missionId: Long,
    @SerializedName("problemDetail") val problemDetail: ProblemDetailDto
)

// 문제 상세 DTO (다형성 처리를 위한 기본 클래스)
abstract class ProblemDetailDto {
    abstract val id: Long
    abstract val dtype: String
}

// 퀴즈 문제 DTO
data class QuizProblemDto(
    @SerializedName("id") override val id: Long,
    @SerializedName("dtype") override val dtype: String,
    @SerializedName("question") val question: String,
    @SerializedName("choices") val choices: List<String>,
    @SerializedName("correctIndex") val correctIndex: Int,
    @SerializedName("explanation") val explanation: String
) : ProblemDetailDto()

// 셀피 문제 DTO
data class SelfieProblemDto(
    @SerializedName("id") override val id: Long,
    @SerializedName("dtype") override val dtype: String,
    @SerializedName("guideline") val guideline: String,
    @SerializedName("pose") val pose: String,
    @SerializedName("poseHint") val poseHint: String
) : ProblemDetailDto()

// 미션 제출 요청 DTO
data class MissionSubmitRequestDto(
    @SerializedName("isSuccess") val isSuccess: Boolean
)

// 미션 제출 응답 DTO
data class MissionSubmitResponseDto(
    @SerializedName("successful") val successful: Boolean,
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