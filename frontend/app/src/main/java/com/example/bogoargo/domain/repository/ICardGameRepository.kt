package com.example.bogoargo.domain.repository

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.data.mapper.BattleHistoryPagination

interface ICardGameRepository {
    suspend fun getBattleHistory(teamId: Long, page: Int = 0, size: Int = 10): DataResult<BattleHistoryPagination>
}