package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.response.*
import com.example.bogoargo.domain.model.*
import java.time.LocalDate
import java.time.format.DateTimeParseException

// StudentClassDetailResponse -> StudentClassDetail 변환
fun StudentClassDetailResponse.toDomainModel(): StudentClassDetail? {
    return this.data?.let { data ->
        StudentClassDetail(
            classInfo = data.classInfo.toDomainModel(),
            students = data.students.map { it.toDomainModel() },
            teams = data.teams.map { it.toDomainModel() },
            statistics = data.statistics.toDomainModel()
        )
    }
}

// StudentClassDetailData -> StudentClassDetail 변환
fun StudentClassDetailData.toDomainModel(): StudentClassDetail {
    return StudentClassDetail(
        classInfo = classInfo.toDomainModel(),
        students = students.map { it.toDomainModel() },
        teams = teams.map { it.toDomainModel() },
        statistics = statistics.toDomainModel()
    )
}

// ClassInfoDetailDto -> ClassDetailInfo 변환
fun ClassInfoDetailDto.toDomainModel(): ClassDetailInfo {
    return ClassDetailInfo(
        classId = classId,
        className = className,
        description = description,
        location = location,
        activityDate = try {
            LocalDate.parse(activityDate.substringBefore("T"))
        } catch (e: DateTimeParseException) {
            LocalDate.MIN
        },
        maxStudents = maxStudents,
        status = status,
        inviteCode = inviteCode,
        teacherId = teacherId,
        teacherName = teacherName,
        createdAt = createdAt?.let {
            try {
                LocalDate.parse(it.substringBefore("T"))
            } catch (e: DateTimeParseException) {
                null
            }
        }
    )
}

// StudentDto -> StudentInfo 변환
fun StudentDto.toDomainModel(): StudentInfo {
    return StudentInfo(
        studentId = studentId,
        studentName = studentName,
        teamId = teamId,
        teamName = teamName,
        joinedAt = try {
            LocalDate.parse(joinedAt.substringBefore("T"))
        } catch (e: DateTimeParseException) {
            LocalDate.MIN
        }
    )
}

// TeamDetailDto -> TeamDetail 변환
fun TeamDetailDto.toDomainModel(): TeamDetail {
    return TeamDetail(
        teamId = teamId,
        teamName = teamName,
        memberCount = memberCount,
        totalScore = totalScore,
        members = members.map { it.toDomainModel() }
    )
}

// TeamMemberDto -> TeamMemberInfo 변환
fun TeamMemberDto.toDomainModel(): TeamMemberInfo {
    return TeamMemberInfo(
        studentId = studentId,
        studentName = studentName
    )
}

// StatisticsDto -> ClassStatistics 변환
fun StatisticsDto.toDomainModel(): ClassStatistics {
    return ClassStatistics(
        totalStudents = totalStudents,
        totalTeams = totalTeams
    )
}