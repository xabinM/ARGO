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

// 팀 카드 컬렉션 응답 DTO (백엔드 TeamCardCollectionResponse와 일치)
data class CardGameTeamCardCollectionResponseDto(
    val success: Boolean,
    val message: String,
    val data: TeamCardCollectionDataDto?
)

data class TeamCardCollectionDataDto(
    val teamId: Long,
    val teamCards: List<TeamCardItemDto>,
    val totalCount: Int,
    val tierStats: Map<String, Int>
)

// 팀 카드 항목 DTO (백엔드 TeamCardDto와 일치)
data class TeamCardItemDto(
    val teamCardId: Long,
    val cardId: Long,
    val tier: String, // CardTier enum string
    val obtainedAt: String, // LocalDateTime string
    val isLost: Boolean,
    val isLocked: Boolean
)

// 대전 관련 응답 DTOs
data class BattleResponse(
    val message: String
)

// 대전 상대 팀 정보 DTO (백엔드 BattleOpponentDto와 일치)
data class BattleOpponentDto(
    val teamId: Long,
    val teamName: String,
    val leaderName: String,
    val totalGames: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val totalPoints: Int
)