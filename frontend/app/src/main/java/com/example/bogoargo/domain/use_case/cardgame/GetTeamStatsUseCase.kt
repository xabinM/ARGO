package com.example.bogoargo.domain.use_case.cardgame

import com.example.bogoargo.domain.model.TeamCardStats
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.DataException
import com.example.bogoargo.domain.repository.ICardGameRepository
import javax.inject.Inject

class GetTeamStatsUseCase @Inject constructor(
    private val cardGameRepository: ICardGameRepository
) {
    suspend operator fun invoke(teamId: Long): DataResult<TeamCardStats> {
        return try {
            cardGameRepository.getTeamStats(teamId)
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Get team stats failed"))
        }
    }
}