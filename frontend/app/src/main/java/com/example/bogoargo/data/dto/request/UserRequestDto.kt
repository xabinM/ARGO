package com.example.bogoargo.data.dto

// 회원가입 요청 DTO
data class UserSignUpRequest(
    val username: String,
    val password: String,
    val name: String,
    val role: String,
    val agreeTerms: Boolean
)

// 로그인 요청 DTO
data class UserLoginRequest(
    val username: String,
    val password: String
)

// 회원정보 변경 요청 DTO
data class UserUpdateRequest(
    val name: String,
    val password: String,
)

// 회원 탈퇴 요청 DTO
data class UserWithdrawRequest(
    val password: String
)