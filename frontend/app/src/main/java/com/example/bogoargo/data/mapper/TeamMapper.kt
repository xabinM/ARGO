package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.dto.response.TeamDataDto
import com.example.bogoargo.domain.model.Team
import java.time.LocalDate
import java.time.format.DateTimeParseException


fun TeamDataDto.toDomainModel(): Team {
    return Team(
        id = this.teamId,
        name = this.teamName,
        classId = this.classId,
        currentMembers = this.currentMembers,
        maxMembers = this.maxMembers ?: 99,
        createdAt = try {
            LocalDate.parse(this.createdAt.substringBefore("T"))
        } catch (e: DateTimeParseException) {
            LocalDate.MIN
        }
    )
}