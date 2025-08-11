package com.example.bogoargo.domain.use_case.cardgame

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.data.mapper.TeamCardCollection
import com.example.bogoargo.domain.repository.ICardGameRepository
import javax.inject.Inject

class GetTeamCardCollectionUseCase @Inject constructor(
    private val cardGameRepository: ICardGameRepository
) {
    suspend operator fun invoke(teamId: Long): DataResult<TeamCardCollection> {
        return cardGameRepository.getTeamCardCollection(teamId)
    }
}