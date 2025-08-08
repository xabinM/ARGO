package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.dto.response.*
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