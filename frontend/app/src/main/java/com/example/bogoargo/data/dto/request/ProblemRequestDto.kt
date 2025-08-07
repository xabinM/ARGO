package com.example.bogoargo.data.dto.request

data class ProblemCreateRequest(
    val question: String,
    val choices: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val spotId: Long
)