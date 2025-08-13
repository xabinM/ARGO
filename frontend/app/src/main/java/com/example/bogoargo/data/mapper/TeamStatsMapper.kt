package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.dto.response.TeamStatsResponseDto
import com.example.bogoargo.domain.model.TeamCardStats

fun TeamStatsResponseDto.toDomainModel(): TeamCardStats {
    return TeamCardStats(
        teamId = teamId,
        teamName = teamName,
        wins = wins,
        losses = losses,
        totalScore = totalPoints
    )
}