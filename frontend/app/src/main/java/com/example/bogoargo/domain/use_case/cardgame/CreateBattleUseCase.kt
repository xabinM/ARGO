package com.example.bogoargo.domain.use_case.cardgame

import com.example.bogoargo.domain.model.BattleRequest
import com.example.bogoargo.domain.model.BattleResult
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.ICardGameRepository
import javax.inject.Inject

class CreateBattleUseCase @Inject constructor(
    private val cardGameRepository: ICardGameRepository
) {
    suspend operator fun invoke(battleRequest: BattleRequest): DataResult<BattleResult> {
        return cardGameRepository.createBattle(battleRequest)
    }
}