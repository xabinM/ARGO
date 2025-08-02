package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.dto.response.UserDataDto
import com.example.bogoargo.domain.model.User

// UserDataDto를 User Model로 변환
fun UserDataDto.toDomainModel(): User {
    return User(
        id = this.userId,
        nickname = this.nickname,
        role = when (this.role) {
            "TEACHER" -> User.UserRole.TEACHER
            else -> User.UserRole.STUDENT
        },
        team = this.team?.toDomainModel()
    )
}
