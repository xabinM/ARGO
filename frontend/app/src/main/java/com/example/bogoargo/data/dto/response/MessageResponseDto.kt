package com.example.bogoargo.data.dto.response


// 특정 요청에 대해 성공|실패 여부를 판단하는 DTO
data class MessageResponseDto (
    val success: Boolean,
    val message: String
)
