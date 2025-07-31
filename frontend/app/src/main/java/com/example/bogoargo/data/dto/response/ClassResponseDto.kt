package com.example.bogoargo.data.response

import com.example.bogoargo.data.dto.response.ApplicationResponseDto
import com.example.bogoargo.data.dto.response.TeamDataDto
import com.example.bogoargo.data.dto.response.UserDataDto

// 반 정보 응답 DTO
data class ClassDataDto(
    val classId: Long,
    val className: String,
    val description: String,
    val location: String,
    val activityDate: String,
    val studentCount:Int,
    val maxStudents: Int,
    val teamCount: Int,
    val status: String,
    val inviteCode: String,
    val createdAt: String,
)

// 반 리스트 조회 응답 DTO
data class ClassListResponse(
    val success: Boolean,
    val message: String,
    val data: List<ClassDataDto>?
)

// 반 상세 정보 응답 DTO
data class ClassDetailResponse(
    val success: Boolean,
    val message: String,
    val data: ClassDataDto?
)

// 참여 신청 학생 목록 응답 DTO
data class InviteStudentListResponse(
    val success: Boolean,
    val message: String,
    val data: List<UserDataDto>?
)

// 신청 상태 응답 DTO
data class InviteStatusResponse(
    val success: Boolean,
    val message: String,
    val data: ApplicationResponseDto?
)

// 반 구성원 조회 응답 DTO
data class ClassMemberResponse(
    val success: Boolean,
    val message: String,
    val data: List<UserDataDto>?
)

// 신청 완료 응답 DTO
data class applyClassResponse(
    val success: Boolean,
    val message: String,
    val data: ApplicationResponseDto?
)

// 반 탈퇴 응답 DTO
data class ClassLeaveResponse(
    val success: Boolean,
    val message: String,
    val data: ClassLeaveDataDto
)

// 반 탈퇴 응답 데이터
data class ClassLeaveDataDto(
    val leftClass: ClassDataDto,
    val teamInfo: TeamDataDto,
    val studentInfo: UserDataDto,
)