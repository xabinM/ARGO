package com.example.bogoargo.domain.model

import java.time.LocalDate

// 학생용 반 상세 정보 모델 (백엔드 응답과 일치)
data class StudentClassDetail(
    val classInfo: ClassDetailInfo,
    val students: List<StudentInfo>,
    val teams: List<TeamDetail>,
    val statistics: ClassStatistics
)

// 반 상세 정보 모델
data class ClassDetailInfo(
    val classId: Long,
    val className: String,
    val description: String,
    val location: String,
    val activityDate: LocalDate,
    val maxStudents: Int,
    val status: String,
    val inviteCode: String?, // 학생에게는 null
    val teacherId: Long?, // 학생에게는 null
    val teacherName: String,
    val createdAt: LocalDate? // 학생에게는 null
)

// 학생 정보 모델
data class StudentInfo(
    val studentId: Long,
    val studentName: String,
    val teamId: Long?,
    val teamName: String?,
    val joinedAt: LocalDate
)

// 팀 상세 정보 모델
data class TeamDetail(
    val teamId: Long,
    val teamName: String,
    val memberCount: Int,
    val totalScore: Int,
    val members: List<TeamMemberInfo>
)

// 팀원 정보 모델
data class TeamMemberInfo(
    val studentId: Long,
    val studentName: String
)

// 반 통계 정보 모델
data class ClassStatistics(
    val totalStudents: Int,
    val totalTeams: Int
)