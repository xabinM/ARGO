package com.example.bogoargo.data.model

import java.time.LocalDate

data class Class(
    val id: Long,
    val name: String,
    val description: String,
    val location: String,
    val activityDate: LocalDate,
    val currentStudents: Int,
    val maxStudents: Int,
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
