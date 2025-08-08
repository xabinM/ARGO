package com.example.bogoargo.domain.use_case.cardgame

import com.example.bogoargo.domain.model.BattleResult
import com.example.bogoargo.domain.model.BattleStance
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.ICardGameRepository
import javax.inject.Inject

class RespondToBattleUseCase @Inject constructor(
    private val cardGameRepository: ICardGameRepository
) {
    suspend operator fun invoke(
        matchId: Long, 
        action: String, // "accept" or "reject"
        selectedCardTeamCardId: Long? = null, 
        battleStance: BattleStance? = null
    ): DataResult<BattleResult> {
        return cardGameRepository.respondToBattle(matchId, action, selectedCardTeamCardId, battleStance)
    }
}