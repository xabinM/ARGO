package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.dto.response.*
import com.example.bogoargo.data.dto.request.*
import com.example.bogoargo.domain.model.*

// 대전 기록 응답 매핑
fun CardGameBattleHistoryResponseDto.toDomainModel(currentTeamId: Long): BattleHistoryPagination {
    return BattleHistoryPagination(
        battles = battles.map { it.toDomainModel(currentTeamId) },
        totalElements = totalElements,
        totalPages = totalPages,
        currentPage = currentPage
    )
}

// 대전 기록 항목 매핑
fun BattleHistoryItemDto.toDomainModel(currentTeamId: Long): BattleHistory {
    return BattleHistory(
        matchId = matchId,
        challengerTeamId = challengerTeamId,
        challengedTeamId = challengedTeamId,
        challengerTeamName = challengerTeamName,
        challengedTeamName = challengedTeamName,
        status = BattleStatus.fromString(status),
        resultView = ResultView.fromString(resultView),
        winnerTeamId = winnerTeamId,
        loserTeamId = loserTeamId,
        myCard = myCard?.toBattleCard(),
        opponentCard = opponentCard?.toBattleCard(),
        createdAt = createdAt,
        endedAt = endedAt,
        myTeamId = currentTeamId
    )
}

// 대전 카드 상세 매핑
fun BattleCardDetailsDto.toBattleCard(): BattleCard {
    val cardTier = CardTier.fromString(tier)
    val battleStance = BattleStance.fromString(battleStance)
    val gameCard = GameCard.create(cardId, cardTier, teamCardId)
    return BattleCard(gameCard, battleStance)
}

// 페이지네이션 정보를 포함한 대전 기록 모델
data class BattleHistoryPagination(
    val battles: List<BattleHistory>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int
)

// 팀 카드 컬렉션 매핑
fun TeamCardCollectionDataDto.toDomainModel(): TeamCardCollection {
    return TeamCardCollection(
        teamId = teamId,
        cards = teamCards.map { it.toDomainModel() },
        totalCount = totalCount,
        tierStats = tierStats
    )
}

// 팀 카드 항목 매핑
fun TeamCardItemDto.toDomainModel(): GameCard {
    val cardTier = CardTier.fromString(tier)
    return GameCard.create(cardId, cardTier, teamCardId, isLost, isLocked)
}

// 팀 카드 컬렉션 도메인 모델
data class TeamCardCollection(
    val teamId: Long,
    val cards: List<GameCard>,
    val totalCount: Int,
    val tierStats: Map<String, Int>
)

// 대전 관련 매핑 함수들

// 대전 신청 요청 (도메인 → DTO)
fun BattleRequest.toRequestDto(): BattleRequestDto {
    return BattleRequestDto(
        challengerTeamId = requestingTeamId,
        challengedTeamId = targetTeamId,
        selectedCard = SelectedCardDto(
            teamCardId = selectedCardTeamCardId,
            battleStance = battleStance.name
        )
    )
}

// 대전 응답 결과 매핑 (DTO → 도메인)
fun BattleResponse.toDomainModel(): BattleResult {
    return BattleResult(
        success = true, // API 호출이 성공했으므로 true
        message = message
    )
}

// 대전 가능한 상대팀 매핑 (DTO → 도메인)
fun BattleOpponentDto.toDomainModel(): BattleOpponent {
    return BattleOpponent(
        teamId = teamId,
        teamName = teamName,
        leaderName = leaderName,
        totalGames = totalGames,
        wins = wins,
        losses = losses,
        draws = draws,
        totalPoints = totalPoints
    )
}