package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.dto.response.UserDataDto
import com.example.bogoargo.domain.model.User

// UserDataDto를 User Model로 변환
fun UserDataDto.toDomainModel(): User {
    return User(
        userId = this.userId,
        name = this.name,
        role = this.role,
        team = this.team?.toDomainModel()
    )
}
