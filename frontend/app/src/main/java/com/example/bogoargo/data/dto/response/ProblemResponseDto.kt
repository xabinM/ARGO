package com.example.bogoargo.data.dto.response

// 성공, 실패 메시지
data class ProblemMessageResponse(
    val success: Boolean,
    val message: String
)

// 문제 생성 요청 후 반환 DTO (교사 기능)
data class ProblemResponseDto (
    val success : Boolean,
    val message : String,
    val grade : Long,
    val spotName : String,
    val problems: ProblemQuizListResponseDto
)

// 공통 인터페이스
interface ProblemData {
    val id: Long
    val dtype: String
}

// 셀카 문제 데이터 (ProblemData 구현)
data class ProblemDataSelfieDto(
    override val id: Long,
    override val dtype: String = "SELFIE",
    val guideline: String,
    val pose: String,
    val poseHint: String
) : ProblemData

// 객관식 문제 데이터 (ProblemData 구현)
data class ProblemDataQuizDto(
    //override val id: Long,
    //override val dtype: String = "QUIZ",
    val question: String,
    val choices: List<String>,
    val correctIndex: Int,
    val explanation: String
)

// spot + type별 문제 리스트
data class ProblemListSpotTypeResponseDto(
    val dto: List<ProblemData>?
)

// spot별 문제 리스트
data class ProblemListSpotResponseDto(
    val problems: List<ProblemData>?
)

data class ProblemQuizListResponseDto (
    val problems: List<ProblemDataQuizDto>?
)