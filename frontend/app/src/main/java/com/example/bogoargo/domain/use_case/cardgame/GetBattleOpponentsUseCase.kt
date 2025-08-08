package com.example.bogoargo.domain.use_case.cardgame

import com.example.bogoargo.domain.model.BattleOpponent
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.ICardGameRepository
import javax.inject.Inject

class GetBattleOpponentsUseCase @Inject constructor(
    private val cardGameRepository: ICardGameRepository
) {
    suspend operator fun invoke(teamId: Long): DataResult<List<BattleOpponent>> {
        return cardGameRepository.getBattleOpponents(teamId)
    }
}