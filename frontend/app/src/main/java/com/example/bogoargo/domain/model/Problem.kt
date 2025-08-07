package com.example.bogoargo.domain.model

// 퀴즈 문제 모델
data class QuizProblem(
    val id: Long,
    val dtype: String,
    val question: String,
    val choices: List<String>,
    val formattedQuestion: String
)

// 셀카 문제 모델
data class SelfieProblem(
    val id: Long,
    val dtype: String,
    val guideline: String,
    val pose: String,
    val poseHint: String,
    val displayImageUrl: String
)