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
    val description: String
) {
    companion object {
        // cardId와 rarity만으로 GameCard 생성 (임시 구현)
        fun create(cardId: Long, rarity: CardRarity): GameCard {
            // 임시로 cardId에 따른 기본 정보 설정 (추후 매퍼로 대체)
            val cardInfo = getCardInfo(cardId)
            val rarityMultiplier = when (rarity) {
                CardRarity.COMMON -> 1.0
                CardRarity.RARE -> 1.3
                CardRarity.EPIC -> 1.6  
                CardRarity.LEGENDARY -> 2.0
            }
            
            return GameCard(
                cardId = cardId,
                name = cardInfo.name,
                attack = (cardInfo.baseAttack * rarityMultiplier).toInt(),
                defense = (cardInfo.baseDefense * rarityMultiplier).toInt(),
                rarity = rarity,
                description = cardInfo.description
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

enum class CardRarity(val displayName: String, val color: String) {
    COMMON("일반", "#8E8E93"),
    RARE("레어", "#007AFF"),
    EPIC("에픽", "#AF52DE"),
    LEGENDARY("전설", "#FF9500")
}

enum class BattleStance(val displayName: String, val emoji: String) {
    ATTACK("공격", "⚔️"),
    DEFENSE("방어", "🛡️")
}

data class BattleCard(
    val gameCard: GameCard,
    val battleStance: BattleStance
)

enum class BattleStatus(val displayName: String) {
    PENDING("신청 중"),
    CANCELLED("취소됨"),
    EXPIRED("만료됨"),
    COMPLETED("완료")
}

enum class ResultView {
    SeeChallenger,
    SeeChallenged, 
    BothSee,
    BothNotSee
}

data class TeamCardCollection(
    val teamId: Long,
    val cards: List<GameCard>
)

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
            ResultView.BothSee -> true
            ResultView.SeeChallenger -> isMyChallenge
            ResultView.SeeChallenged -> !isMyChallenge
            ResultView.BothNotSee -> false
        }
    
    // 승패 결과 계산 (이미 확인했을 때만 반환)
    val isWin: Boolean?
        get() = if (hasViewedResult) actualResult else null
    
    // 점수 획득량 계산 (이미 확인했을 때만 반환)
    val scoreGained: Int
        get() = when {
            status != BattleStatus.COMPLETED -> 0
            !hasViewedResult -> 0 // 아직 확인하지 않았으면 0
            actualResult == true -> 150 // 승리 시 150점
            actualResult == false -> -30 // 패배 시 -30점
            else -> 0 // 무승부
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
    val selectedCards: List<GameCard>,
    val message: String
)