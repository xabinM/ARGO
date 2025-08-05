package com.example.bogoargo.data.dto.response

import com.example.bogoargo.data.model.UserRole
import com.example.bogoargo.data.response.ClassDataDto

// 유저 정보 응답 DTO
data class UserDataDto(
    val userId: Long,
    val name: String,
    val role: UserRole,
    val team: TeamDataDto?
)

// 로그인 응답 DTO
data class UserLoginResponse(
    val success: Boolean,
    val message: String,
    val data: UserDataDto?
)

// 회원 정보 수정 응답 DTO
data class UserUpdateResponse(
    val success: Boolean,
    val message: String
)

// 회원 탈퇴 응답 DTO
data class UserWithdrawResponse(
    val success: Boolean,
    val message: String
)

// 신청 현황 정보 응답 DTO
data class UserMyappResponse(
    val success: Boolean,
    val message: String,
    val data: ClassDataDto?
)