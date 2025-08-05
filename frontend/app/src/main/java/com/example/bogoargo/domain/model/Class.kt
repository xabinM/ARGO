package com.example.bogoargo.domain.model

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
    val status: ClassStatus,
    val inviteCode: String,
    val createdAt: LocalDate,
    val isFull: Boolean = currentStudents >= maxStudents
) {
    enum class ClassStatus {
        ACTIVE,
        ENDED,
    }
}
