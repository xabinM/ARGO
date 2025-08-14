package com.example.bogoargo.domain.model

import com.example.bogoargo.data.response.ClassInfo
import com.example.bogoargo.data.response.StatisticsDto
import com.example.bogoargo.data.response.StudentDto
import com.example.bogoargo.data.response.TeamDetailDto
import java.time.LocalDate

data class Class(
    val classId: Long,
    val className: String,
    val description: String,
    val location: String,
    val activityDate: LocalDate,
    val currentStudents: Int,
    val maxStudents: Int,
    val studentCount: Int,
    val teamCount: Int,
    val grade: Int? = 1,
    val status: ClassStatus,
    val inviteCode: String,
    val createdAt: LocalDate,
    val isFull: Boolean?
) {
    enum class ClassStatus {
        ACTIVE,
        ENDED,
    }
}

data class TeaCherClassDetail (
    val data: Class?,
    val students: List<StudentInfo>?,
    val teams: List<TeamDetail>?,
    val statistics: ClassStatistics?
)
