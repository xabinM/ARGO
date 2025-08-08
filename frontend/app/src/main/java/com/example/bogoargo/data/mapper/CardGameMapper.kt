package com.example.bogoargo.data.mapper

import com.example.bogoargo.domain.model.*

/**
 * API 응답을 도메인 모델로 변환하는 매퍼
 */
object CardGameMapper {
    
    /**
     * 서버의 tier를 클라이언트의 CardTier로 변환
     */
    fun mapTierToTier(tier: String): CardTier {
        return when (tier.uppercase()) {
            "LEGENDARY" -> CardTier.LEGENDARY
            "EPIC" -> CardTier.EPIC
            "RARE" -> CardTier.RARE
            "COMMON" -> CardTier.COMMON
            else -> CardTier.COMMON
        }
    }
    
    /**
     * 클라이언트의 CardTier를 서버의 tier로 변환
     */
    fun mapTierToString(rarity: CardTier): String {
        return when (rarity) {
            CardTier.LEGENDARY -> "LEGENDARY"
            CardTier.EPIC -> "EPIC"
            CardTier.RARE -> "RARE"
            CardTier.COMMON -> "COMMON"
        }
    }
    
    /**
     * 서버의 battleStance 문자열을 BattleStance enum으로 변환
     */
    fun mapStringToBattleStance(battleStance: String): BattleStance {
        return when (battleStance.uppercase()) {
            "ATTACK" -> BattleStance.ATTACK
            "DEFENSE" -> BattleStance.DEFENSE
            else -> BattleStance.ATTACK
        }
    }
    
    /**
     * BattleStance enum을 서버용 문자열로 변환
     */
    fun mapBattleStanceToString(battleStance: BattleStance): String {
        return when (battleStance) {
            BattleStance.ATTACK -> "ATTACK"
            BattleStance.DEFENSE -> "DEFENSE"
        }
    }
    
    /**
     * TeamCardResponse를 GameCard로 변환
     */
    fun mapToGameCard(teamCard: TeamCardResponse): GameCard {
        val tier = mapTierToTier(teamCard.tier)
        return GameCard.create(teamCard.cardId, tier, teamCard.teamCardId)
    }
    
    /**
     * BattleCardResponse를 BattleCard로 변환
     */
    fun mapToBattleCard(battleCard: BattleCardResponse): BattleCard {
        val tier = mapTierToTier(battleCard.tier)
        val stance = mapStringToBattleStance(battleCard.battleStance)
        val gameCard = GameCard.create(battleCard.cardId, tier, battleCard.teamCardId)
        return BattleCard(gameCard, stance)
    }
    
    /**
     * 대전 신청 요청 생성
     */
    fun createBattleRequest(
        challengerTeamId: Long,
        challengedTeamId: Long,
        teamCardId: Long,
        battleStance: BattleStance
    ): Map<String, Any> {
        return mapOf(
            "challengerTeamId" to challengerTeamId,
            "challengedTeamId" to challengedTeamId,
            "selectedCard" to mapOf(
                "teamCardId" to teamCardId,
                "battleStance" to mapBattleStanceToString(battleStance)
            )
        )
    }
    
    /**
     * 대전 수락 요청 생성
     */
    fun createBattleAcceptRequest(
        teamCardId: Long,
        battleStance: BattleStance
    ): Map<String, Any> {
        return mapOf(
            "action" to "ACCEPT",
            "selectedCard" to mapOf(
                "teamCardId" to teamCardId,
                "battleStance" to mapBattleStanceToString(battleStance)
            )
        )
    }
}