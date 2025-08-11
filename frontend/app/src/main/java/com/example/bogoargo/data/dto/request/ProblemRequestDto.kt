package com.example.bogoargo.data.dto.request

data class ProblemRegisterRequest(
    val question: String,
    val choices: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val spotId: Long,
    val grade : Long
)

data class ProblemGenerateRequest(
    val spotId: Long,
    val grade: Int,
    val problemCnt: Int
)