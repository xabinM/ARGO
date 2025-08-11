package com.example.bogoargo.domain.use_case.cardgame

import com.example.bogoargo.domain.model.BattleResult
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.DataException
import com.example.bogoargo.domain.repository.ICardGameRepository
import javax.inject.Inject

class CancelBattleUseCase @Inject constructor(
    private val cardGameRepository: ICardGameRepository
) {
    suspend operator fun invoke(matchId: Long): DataResult<BattleResult> {
        return try {
            cardGameRepository.cancelBattle(matchId)
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Cancel battle failed"))
        }
    }
}