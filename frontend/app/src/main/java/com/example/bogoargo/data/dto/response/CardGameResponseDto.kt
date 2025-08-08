package com.example.bogoargo.data.dto.response

// 카드게임 공통 API 응답 DTO
data class CommonCardGameResponseDto<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

// 대전 기록 응답 DTO (백엔드 BattleHistoryResponse와 일치)
data class CardGameBattleHistoryResponseDto(
    val battles: List<BattleHistoryItemDto>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int
)

// 대전 기록 항목 DTO (백엔드 BattleHistoryDto와 일치)
data class BattleHistoryItemDto(
    val matchId: Long,
    val challengerTeamId: Long,
    val challengedTeamId: Long,
    val challengerTeamName: String,
    val challengedTeamName: String,
    val status: String, // MatchStatus enum string
    val resultView: String, // ResultView enum string
    val winnerTeamId: Long?,
    val loserTeamId: Long?,
    val isDraw: Boolean,
    val myCard: BattleCardDetailsDto?,
    val opponentCard: BattleCardDetailsDto?,
    val createdAt: String, // LocalDateTime string
    val endedAt: String? // LocalDateTime string (nullable)
)

// 대전 카드 상세 DTO (백엔드 BattleCardDto와 일치)
data class BattleCardDetailsDto(
    val teamCardId: Long,
    val cardId: Long,
    val tier: String, // CardTier enum string
    val battleStance: String // BattleStrategy enum string
)