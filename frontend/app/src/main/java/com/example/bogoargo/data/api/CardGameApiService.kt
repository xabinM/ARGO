package com.example.bogoargo.data.api

import com.example.bogoargo.data.dto.response.CommonCardGameResponseDto
import com.example.bogoargo.data.dto.response.CardGameBattleHistoryResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CardGameApiService {

    /**
     * 팀 대전 기록 조회
     * GET /api/teams/{teamId}/battle-history
     */
    @GET("api/teams/{teamId}/battle-history")
    suspend fun getBattleHistory(
        @Path("teamId") teamId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<CommonCardGameResponseDto<CardGameBattleHistoryResponseDto>>
}