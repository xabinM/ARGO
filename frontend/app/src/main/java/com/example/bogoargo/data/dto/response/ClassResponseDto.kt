package com.example.bogoargo.data.response

import com.example.bogoargo.data.dto.response.ApplicationResponseDto
import com.example.bogoargo.data.dto.response.TeamDataDto
import com.example.bogoargo.data.dto.response.UserDataDto

// 반 정보 응답 DTO
data class ClassDataDto(
    val classId: Long,
    val className: String,
    val description: String?,
    val location: String?,
    val activityDate: String?,
    val maxStudents: Int?,
    val grade: Int?,
    val status: String?,
    val inviteCode: String,
    val createdAt: String,
    val students: List<UserDataDto>?,
    val teams: List<TeamDataDto>?
)

data class ClassInfo(
    val classInfo: ClassDataDto
)

// 반 리스트 조회 응답 DTO
data class ClassListResponse(
    val success: Boolean,
    val message: String,
    val data: ClassListData?
)

// 반 리스트 데이터 DTO
data class ClassListData(
    val classes: List<ClassInfoDto>,
    val pagination: PaginationDto
)

// 반 정보 간략 DTO (리스트용)
data class ClassInfoDto(
    val classId: Long,
    val className: String,
    val description: String,
    val location: String,
    val activityDate: String,
    val studentCount: Int,
    val maxStudents: Int,
    val teamCount: Int,
    val status: String,
    val inviteCode: String? = null,  // 학생에게는 null
    val createdAt: String
)

// 페이지네이션 정보 DTO
data class PaginationDto(
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
    val data: ClassListDataDto?
)

// 반 리스트 데이터 DTO
data class ClassListDataDto(
    val classes: List<ClassDataDto>
)

// 반 생성 후 정보 응답 DTO
data class ClassCreateResponse(
    val success: Boolean,
    val message: String,
    val data: ClassDataDto?
)

// 반 상세 정보 응답 DTO
data class ClassDetailResponse(
    val success: Boolean,
    val message: String,
    val data: ClassInfo?
)

// 참여 신청 학생 목록 응답 DTO
data class InviteStudentListResponse(
    val success: Boolean,
    val message: String,
    val data: List<UserDataDto>? //TODO: 어플리케이션 목록으로 변경 필요
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

// 학생용 반 상세 정보 응답 DTO (백엔드 ClassDetailResponse와 일치)
data class StudentClassDetailResponse(
    val success: Boolean,
    val message: String,
    val data: StudentClassDetailData?
)

// 학생용 반 상세 데이터 DTO
data class StudentClassDetailData(
    val classInfo: ClassInfoDetailDto,
    val students: List<StudentDto>,
    val teams: List<TeamDetailDto>,
    val statistics: StatisticsDto
)

// 반 상세 정보 DTO (학생용 - 백엔드 ClassInfoDetailDto와 일치)
data class ClassInfoDetailDto(
    val classId: Long,
    val className: String,
    val description: String,
    val location: String,
    val activityDate: String,
    val maxStudents: Int,
    val status: String,
    val inviteCode: String?, // 학생에게는 null
    val teacherId: Long?, // 학생에게는 null
    val teacherName: String,
    val createdAt: String? // 학생에게는 null
)

// 학생 정보 DTO (백엔드 StudentDto와 일치)
data class StudentDto(
    val studentId: Long,
    val studentName: String,
    val teamId: Long?,
    val teamName: String?,
    val joinedAt: String
)

// 팀 상세 정보 DTO (백엔드 TeamDetailDto와 일치)
data class TeamDetailDto(
    val teamId: Long,
    val teamName: String,
    val memberCount: Int,
    val totalScore: Int,
    val teamLeaderId: Long,
    val members: List<TeamMemberDto>
)

// 팀원 정보 DTO (백엔드 TeamMemberDto와 일치)
data class TeamMemberDto(
    val studentId: Long,
    val studentName: String
)

// 통계 정보 DTO (백엔드 StatisticsDto와 일치)
data class StatisticsDto(
    val totalStudents: Int,
    val totalTeams: Int
)