package com.example.bogoargo.domain.model

data class TeamCardStats(
    val teamId: Long,
    val teamName: String,
    val wins: Int,
    val losses: Int,
    val totalScore: Int,
    val rank: Int
)

data class GameCard(
    val cardId: Long,
    val name: String,
    val attack: Int,
    val defense: Int,
    val rarity: CardRarity,
    val imageUrl: String,
    val description: String
)

enum class CardRarity(val displayName: String, val color: String) {
    COMMON("일반", "#8E8E93"),
    RARE("레어", "#007AFF"),
    EPIC("에픽", "#AF52DE"),
    LEGENDARY("전설", "#FF9500")
}

enum class BattleStatus(val displayName: String) {
    COMPLETED("완료"),
    WAITING_OPPONENT("상대 응답 대기"),
    WAITING_MY_CARDS("카드 선택 대기"),
    RESULT_PENDING("결과 확인 대기")
}

data class TeamCardCollection(
    val teamId: Long,
    val cards: List<GameCard>
)

data class BattleHistory(
    val battleId: Long,
    val opponentTeamName: String,
    val isWin: Boolean?,
    val scoreGained: Int,
    val battleDate: String,
    val requestDate: String?,
    val status: BattleStatus,
    val myCards: List<GameCard>,
    val opponentCards: List<GameCard>
) {
    val canCancel: Boolean
        get() = status == BattleStatus.WAITING_OPPONENT
    
    val needsCardSelection: Boolean
        get() = status == BattleStatus.WAITING_MY_CARDS
    
    val hasUnviewedResult: Boolean
        get() = status == BattleStatus.RESULT_PENDING
}

data class BattleTeam(
    val teamId: Long,
    val teamName: String,
    val memberCount: Int,
    val averageScore: Int,
    val isAvailable: Boolean
)

data class BattleRequest(
    val requestingTeamId: Long,
    val targetTeamId: Long,
    val selectedCards: List<GameCard>,
    val message: String
)