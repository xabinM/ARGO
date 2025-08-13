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
    val rarity: CardTier,
    val description: String,
    val teamCardId: Long? = null, // API 응답에서 받은 teamCardId (옵셔널)
    val isLost: Boolean = false,   // 제거된 카드 여부
    val isLocked: Boolean = false  // 대전에 사용중인 카드 여부
) {
    // 활성 상태 (사용 가능한 카드) 계산 속성
    val isActive: Boolean
        get() = !isLost && !isLocked
        
    // 상태별 투명도 계산
    val displayAlpha: Float
        get() = when {
            isLost -> 0.3f      // 제거된 카드는 매우 흐림
            isLocked -> 0.7f    // 사용중은 약간 흐림  
            else -> 1.0f        // 정상
        }
    
    // 상태 표시 라벨
    val statusLabel: String?
        get() = when {
            isLost -> "❌ 제거됨"
            isLocked -> "🔒 사용중"
            else -> null
        }
    companion object {
        // cardId와 rarity만으로 GameCard 생성 (기존 방식)
        fun create(cardId: Long, rarity: CardTier): GameCard {
            return create(cardId, rarity, null, false, false)
        }
        
        // cardId, rarity, teamCardId로 GameCard 생성 (API 응답용)
        fun create(cardId: Long, rarity: CardTier, teamCardId: Long?): GameCard {
            return create(cardId, rarity, teamCardId, false, false)
        }
        
        // 모든 매개변수를 포함한 완전한 GameCard 생성 (API 응답용)
        fun create(
            cardId: Long, 
            rarity: CardTier, 
            teamCardId: Long?, 
            isLost: Boolean, 
            isLocked: Boolean
        ): GameCard {
            // 임시로 cardId에 따른 기본 정보 설정 (추후 매퍼로 대체)
            val cardInfo = getCardInfo(cardId)
            val rarityMultiplier = when (rarity) {
                CardTier.COMMON -> 1.0
                CardTier.RARE -> 1.2
                CardTier.EPIC -> 1.5
                CardTier.LEGENDARY -> 2.0
            }
            
            return GameCard(
                cardId = cardId,
                name = cardInfo.name,
                attack = (cardInfo.baseAttack * rarityMultiplier).toInt(),
                defense = (cardInfo.baseDefense * rarityMultiplier).toInt(),
                rarity = rarity,
                description = cardInfo.description,
                teamCardId = teamCardId,
                isLost = isLost,
                isLocked = isLocked
            )
        }

        private fun getCardInfo(cardId: Long): CardInfo {
            return when (cardId) {
                1L -> CardInfo("불사조", 45, 35, "재생의 힘을 가진 전설적인 새")
                2L -> CardInfo("그림자 늑대", 40, 30, "어둠 속에서 빠르게 움직이는 늑대")
                3L -> CardInfo("치유의 요정", 25, 50, "아군을 치유하는 신비한 요정")
                4L -> CardInfo("바위 골렘", 35, 55, "견고한 방어력을 자랑하는 골렘")
                5L -> CardInfo("번개 마법사", 50, 25, "강력한 번개 마법을 구사하는 마법사")
                6L -> CardInfo("숲의 수호자", 40, 40, "자연의 힘을 다루는 수호자")
                7L -> CardInfo("얼음 용", 55, 45, "차가운 얼음 브레스를 내뿜는 용")
                8L -> CardInfo("기사", 45, 40, "정의로운 검술을 구사하는 기사")
                else -> CardInfo("알 수 없는 카드", 30, 30, "정체불명의 카드")
            }
        }
        
        private data class CardInfo(
            val name: String,
            val baseAttack: Int,
            val baseDefense: Int,
            val description: String
        )
    }
    
    // 계산된 속성들 - CardImageMapper를 통해 이미지 리소스 ID를 가져옴
    val characterImageRes: Int
        get() = com.example.bogoargo.util.CardImageMapper.getCharacterImage(cardId)
    
    val borderImageRes: Int
        get() = com.example.bogoargo.util.CardImageMapper.getBorderImage(rarity)
}

enum class CardTier(val displayName: String, val color: String) {
    COMMON("일반", "#CD7F32"),     // 브론즈(동색)
    RARE("레어", "#C0C0C0"),       // 실버(은색)
    EPIC("에픽", "#FFD700"),       // 골드(금색)
    LEGENDARY("전설", "#4FC3F7");  // 다이아몬드(밝은 파란색)
    
    companion object {
        fun fromString(tier: String): CardTier {
            return when (tier.uppercase()) {
                "COMMON" -> COMMON
                "RARE" -> RARE
                "EPIC" -> EPIC
                "LEGENDARY", "LEGEND" -> LEGENDARY
                else -> COMMON
            }
        }
    }
}

enum class BattleStance(val displayName: String, val emoji: String) {
    ATTACK("공격", "⚔️"),
    DEFENSE("방어", "🛡️");
    
    companion object {
        fun fromString(battleStance: String): BattleStance {
            return when (battleStance.uppercase()) {
                "ATTACK" -> ATTACK
                "DEFENSE" -> DEFENSE
                else -> ATTACK
            }
        }
    }
}

data class BattleCard(
    val gameCard: GameCard,
    val battleStance: BattleStance
) {
    // API 응답에서 BattleCard 생성하는 헬퍼 함수
    companion object {
        fun fromApiResponse(
            teamCardId: Long,
            cardId: Long, 
            tier: String,
            battleStance: String
        ): BattleCard {
            val rarity = when (tier) {
                "LEGEND" -> CardTier.LEGENDARY
                "EPIC" -> CardTier.EPIC
                "RARE" -> CardTier.RARE
                "COMMON" -> CardTier.COMMON
                else -> CardTier.COMMON
            }
            
            val stance = when (battleStance) {
                "ATTACK" -> BattleStance.ATTACK
                "DEFENSE" -> BattleStance.DEFENSE
                else -> BattleStance.ATTACK
            }
            
            val gameCard = GameCard.create(cardId, rarity, teamCardId)
            return BattleCard(gameCard, stance)
        }
    }
}

enum class BattleStatus(val displayName: String) {
    PENDING("신청 중"),
    CANCELLED("취소됨"),
    EXPIRED("만료됨"),
    COMPLETED("완료");
    
    companion object {
        fun fromString(status: String): BattleStatus {
            return when (status.uppercase()) {
                "PENDING" -> PENDING
                "CANCELLED" -> CANCELLED
                "EXPIRED" -> EXPIRED
                "COMPLETED" -> COMPLETED
                else -> PENDING
            }
        }
    }
}

enum class ResultView {
    SEE_CHALLENGER,
    SEE_CHALLENGED, 
    BOTH_SEE,
    BOTH_NOT_SEE;
    
    companion object {
        fun fromString(resultView: String): ResultView {
            return when (resultView.uppercase()) {
                "SEE_CHALLENGER" -> SEE_CHALLENGER
                "SEE_CHALLENGED" -> SEE_CHALLENGED
                "BOTH_SEE" -> BOTH_SEE
                "BOTH_NOT_SEE" -> BOTH_NOT_SEE
                else -> BOTH_NOT_SEE
            }
        }
    }
}

data class BattleHistory(
    val matchId: Long,
    val challengerTeamId: Long,
    val challengedTeamId: Long,
    val challengerTeamName: String,
    val challengedTeamName: String,
    val status: BattleStatus,
    val resultView: ResultView,
    val winnerTeamId: Long?,
    val loserTeamId: Long?,
    val myCard: BattleCard?,
    val opponentCard: BattleCard?,
    val createdAt: String,
    val endedAt: String?,
    val myTeamId: Long // 현재 사용자의 팀 ID
) {
    // 상대방 팀 이름 계산
    val opponentTeamName: String
        get() = if (myTeamId == challengerTeamId) challengedTeamName else challengerTeamName
    
    // 내가 challenger인지 확인
    val isMyChallenge: Boolean
        get() = myTeamId == challengerTeamId
    
    // 실제 승패 결과 (내부적으로만 사용)
    private val actualResult: Boolean?
        get() = when {
            status != BattleStatus.COMPLETED -> null
            winnerTeamId == null -> null
            winnerTeamId == myTeamId -> true
            else -> false
        }
    
    // 이미 결과를 본 상태인지 (resultView 기반으로 계산)
    val hasViewedResult: Boolean
        get() = when (resultView) {
            ResultView.BOTH_SEE -> true
            ResultView.SEE_CHALLENGER -> isMyChallenge
            ResultView.SEE_CHALLENGED -> !isMyChallenge
            ResultView.BOTH_NOT_SEE -> false
        }
    
    // 승패 결과 계산 (이미 확인했을 때만 반환)
    val isWin: Boolean?
        get() = if (hasViewedResult) actualResult else null
    
    // 점수 획득량 계산 (이미 확인했을 때만 반환)
    val scoreGained: Int
        get() = when {
            status != BattleStatus.COMPLETED -> 0
            !hasViewedResult -> 0 // 아직 확인하지 않았으면 0
            myCard == null || opponentCard == null -> 0 // 카드 정보가 없으면 0
            else -> calculateScore(
                myStance = myCard.battleStance,
                opponentStance = opponentCard.battleStance,
                isMyWin = actualResult == true,
                isDraw = actualResult == null
            )
        }
    
    // 점수 계산 로직 (최신 버전)
    private fun calculateScore(
        myStance: BattleStance,
        opponentStance: BattleStance,
        isMyWin: Boolean,
        isDraw: Boolean
    ): Int {
        return when {
            // 방 vs 방 특수 조건
            myStance == BattleStance.DEFENSE && opponentStance == BattleStance.DEFENSE -> 50
            // 공격 선택 시
            myStance == BattleStance.ATTACK -> when {
                isMyWin -> 100
                isDraw -> 100  // 비김 = 카드제거 + 100점
                else -> 0      // 짐 = 카드제거
            }
            // 방어 선택 시
            myStance == BattleStance.DEFENSE -> when {
                isMyWin -> 50
                else -> 0      // 비김/짐 = pass
            }
            else -> 0
        }
    }
    
    // UI에 표시할 상태 정보
    val displayStatus: String
        get() = when (status) {
            BattleStatus.COMPLETED -> {
                if (hasViewedResult) {
                    // 이미 확인한 경우만 실제 결과 표시
                    when (actualResult) {
                        true -> "승리 🏆"
                        false -> "패배 💔"
                        null -> "무승부 🤝"
                    }
                } else {
                    // 아직 확인하지 않은 경우
                    "완료됨 ✅"
                }
            }
            BattleStatus.PENDING -> "대기중 ⏳"
            BattleStatus.CANCELLED -> "취소됨 ❌"
            BattleStatus.EXPIRED -> "만료됨 ⏰"
        }
    
    // UI에 표시할 카드 색상
    val displayColor: Long
        get() = when (status) {
            BattleStatus.COMPLETED -> {
                if (hasViewedResult) {
                    // 이미 확인한 경우만 실제 결과에 따른 색상
                    when (actualResult) {
                        true -> 0xFF4CAF50  // 승리 - 초록
                        false -> 0xFFF44336 // 패배 - 빨강
                        null -> 0xFF9E9E9E  // 무승부 - 회색
                    }
                } else {
                    // 아직 확인하지 않은 경우
                    0xFF2196F3 // 완료됨 - 파랑
                }
            }
            BattleStatus.PENDING -> 0xFFFF9800    // 대기중 - 주황
            BattleStatus.CANCELLED -> 0xFF9E9E9E  // 취소됨 - 회색
            BattleStatus.EXPIRED -> 0xFFF44336    // 만료됨 - 빨강
        }
    
    // 결과 보기 가능 여부 (완료된 대전은 누구나 볼 수 있음)
    val canViewResult: Boolean
        get() = status == BattleStatus.COMPLETED && !hasViewedResult
    
    // 신청 취소 가능 여부 (내가 신청한 대전이고 PENDING 상태)
    val canCancel: Boolean
        get() = status == BattleStatus.PENDING && isMyChallenge
    
    // 대전 거절 가능 여부 (상대가 신청한 대전이고 PENDING 상태)
    val canReject: Boolean
        get() = status == BattleStatus.PENDING && !isMyChallenge
    
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
    val selectedCardTeamCardId: Long, // teamCardId만 전송
    val battleStance: BattleStance,
    val message: String = ""
)

// API 응답용 BattleCard 데이터 클래스  
data class BattleCardResponse(
    val teamCardId: Long,
    val cardId: Long,
    val tier: String,
    val battleStance: String
) {
    fun toBattleCard(): BattleCard {
        return BattleCard.fromApiResponse(teamCardId, cardId, tier, battleStance)
    }
}

// 대전 신청 결과 (API 응답)
data class BattleResult(
    val success: Boolean,
    val message: String,
    val matchId: Long? = null
)

// 대전 가능한 상대팀 정보
data class BattleOpponent(
    val teamId: Long,
    val teamName: String,
    val leaderName: String,
    val totalGames: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val totalPoints: Int
) {
    // 계산된 속성들
    val winRate: Double
        get() = if (totalGames > 0) wins.toDouble() / totalGames else 0.0
    
    val averageScore: Int
        get() = if (totalGames > 0) totalPoints / totalGames else 0
        
    val isAvailable: Boolean
        get() = true // 백엔드에서 대전 가능한 팀만 반환하므로 항상 true
        
    // UI 표시용 멤버 수 (임시로 총 게임 수 기반 계산, 실제로는 백엔드에서 제공해야 함)
    val memberCount: Int
        get() = minOf(4, maxOf(1, totalGames / 5 + 2)) // 2-4명 사이 값
}