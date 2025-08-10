package com.example.bogoargo.data.api

import com.example.bogoargo.data.dto.response.CommonCardGameResponseDto
import com.example.bogoargo.data.dto.response.CardGameBattleHistoryResponseDto
import com.example.bogoargo.data.dto.response.TeamCardCollectionDataDto
import com.example.bogoargo.data.dto.response.BattleResponse
import com.example.bogoargo.data.dto.response.BattleOpponentDto
import com.example.bogoargo.data.dto.response.TeamStatsResponseDto
import com.example.bogoargo.data.dto.request.BattleRequestDto
import com.example.bogoargo.data.dto.request.BattleResponseDto
import retrofit2.Response
import retrofit2.http.*

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

    /**
     * 팀 카드 컬렉션 조회
     * GET /api/teams/{teamId}/cards
     */
    @GET("api/teams/{teamId}/cards")
    suspend fun getTeamCardCollection(
        @Path("teamId") teamId: Long
    ): Response<CommonCardGameResponseDto<TeamCardCollectionDataDto>>

    /**
     * 대전 가능한 팀 목록 조회
     * GET /api/teams/{teamId}/battle-opponents
     */
    @GET("api/teams/{teamId}/battle-opponents")
    suspend fun getBattleOpponents(
        @Path("teamId") teamId: Long
    ): Response<CommonCardGameResponseDto<List<BattleOpponentDto>>>

    /**
     * 대전 신청
     * POST /api/battles
     */
    @POST("api/battles")
    suspend fun createBattle(
        @Body request: BattleRequestDto
    ): Response<CommonCardGameResponseDto<BattleResponse>>

    /**
     * 대전 응답 (수락/거절)
     * PUT /api/battles/{matchId}/respond
     */
    @PUT("api/battles/{matchId}/respond")
    suspend fun respondToBattle(
        @Path("matchId") matchId: Long,
        @Body request: BattleResponseDto
    ): Response<CommonCardGameResponseDto<BattleResponse>>

    /**
     * 대전 신청 취소
     * DELETE /api/battles/{matchId}
     */
    @DELETE("api/battles/{matchId}")
    suspend fun cancelBattle(
        @Path("matchId") matchId: Long
    ): Response<CommonCardGameResponseDto<BattleResponse>>

    /**
     * 대전 결과 확인
     * PUT /api/battles/{matchId}/view-result
     */
    @PUT("api/battles/{matchId}/view-result")
    suspend fun viewBattleResult(
        @Path("matchId") matchId: Long
    ): Response<CommonCardGameResponseDto<BattleResponse>>

    /**
     * 팀 카드 게임 통계 조회
     * GET /api/teams/{teamId}/stats
     */
    @GET("api/teams/{teamId}/stats")
    suspend fun getTeamStats(
        @Path("teamId") teamId: Long
    ): Response<CommonCardGameResponseDto<TeamStatsResponseDto>>
}