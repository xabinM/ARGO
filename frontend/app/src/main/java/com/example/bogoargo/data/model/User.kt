package com.example.bogoargo.data.model

data class User(
    val id: String = "",
    val userName: String = "",
    val email: String = "",
    val bio: String = "",
    val profilePictureUrl: String? = null,
    val studentId: String = "",
    val phoneNumber: String = "",
    val role: UserRole = UserRole.STUDENT,
    val classId: String = "",
    val teamId: String? = null,
    val joinedAt: String = ""
)

enum class UserRole {
    TEACHER,    // 선생님
    STUDENT     // 학생
}