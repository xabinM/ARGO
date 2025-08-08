package com.example.bogoargo.domain.repository

import com.example.bogoargo.domain.model.*
import com.example.bogoargo.data.mapper.BattleHistoryPagination
import com.example.bogoargo.data.mapper.TeamCardCollection

interface ICardGameRepository {
    suspend fun getBattleHistory(teamId: Long, page: Int = 0, size: Int = 10): DataResult<BattleHistoryPagination>
    suspend fun getTeamCardCollection(teamId: Long): DataResult<TeamCardCollection>
    suspend fun getBattleOpponents(teamId: Long): DataResult<List<BattleOpponent>>
    suspend fun createBattle(battleRequest: BattleRequest): DataResult<BattleResult>
    suspend fun respondToBattle(matchId: Long, action: String, selectedCardTeamCardId: Long?, battleStance: BattleStance?): DataResult<BattleResult>
    suspend fun cancelBattle(matchId: Long): DataResult<BattleResult>
    suspend fun viewBattleResult(matchId: Long): DataResult<BattleResult>
    suspend fun getTeamStats(teamId: Long): DataResult<TeamCardStats>
}