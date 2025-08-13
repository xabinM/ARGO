package com.example.bogoargo.domain.model

// 문제 기본 클래스
sealed class ProblemDetail {
    abstract val id: Long
    abstract val dtype: String
}

// 퀴즈 문제
data class QuizProblem(
    override val id: Long,
    override val dtype: String = "QUIZ",
    val question: String,
    val choices: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val formattedQuestion: String = question
) : ProblemDetail()

// 셀피 문제  
data class SelfieProblem(
    override val id: Long,
    override val dtype: String = "SELFIE",
    val guideline: String,
    val pose: String,
    val poseHint: String,
    val displayImageUrl: String = ""
) : ProblemDetail()

// 미션 생성 응답
data class MissionCreateResult(
    val missionId: Long,
    val problemDetail: ProblemDetail
)

// 미션 제출 결과
data class MissionSubmitResult(
    val successful: Boolean,
    val cardId: Long?,
    val tier: String?
)

// 포즈 타입
enum class PoseType {
    HANDS_UP,
    PEACE_SIGN,
    HEART_SHAPE,
    THUMBS_UP,
    WAVE,
    FINGER_FRAME
}