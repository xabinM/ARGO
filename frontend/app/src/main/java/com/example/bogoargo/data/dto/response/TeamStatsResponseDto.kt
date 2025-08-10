package com.example.bogoargo.data.dto.response

data class TeamStatsResponseDto(
    val teamId: Long,
    val teamName: String,
    val totalGames: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val totalPoints: Int,
    val averageScore: Double,
    val rank: Int,
    val winRate: Double
)