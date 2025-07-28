package com.example.bogoargo.data.model

data class Program(
    val id: String = "",
    val classId: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val maxParticipants: Int = 0,
    val participants: Int = 0,
    val status: ProgramStatus = ProgramStatus.UPCOMING,
    val createdAt: String = "",
    val updatedAt: String = ""
)

enum class ProgramStatus {
    UPCOMING,   // 예정
    ONGOING,    // 진행중
    COMPLETED   // 완료
}