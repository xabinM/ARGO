package com.example.bogoargo.domain.use_case.cardgame

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.data.mapper.BattleHistoryPagination
import com.example.bogoargo.domain.repository.ICardGameRepository
import javax.inject.Inject

class GetBattleHistoryUseCase @Inject constructor(
    private val cardGameRepository: ICardGameRepository
) {
    suspend operator fun invoke(teamId: Long, page: Int = 0, size: Int = 10): DataResult<BattleHistoryPagination> {
        return cardGameRepository.getBattleHistory(teamId, page, size)
    }
}