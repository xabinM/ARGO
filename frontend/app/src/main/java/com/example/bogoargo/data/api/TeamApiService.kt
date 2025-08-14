package com.example.bogoargo.data.api

import com.example.bogoargo.data.dto.TeamCreateRequest
import com.example.bogoargo.data.dto.response.TeamAssignResponse
import com.example.bogoargo.data.dto.response.TeamCreateResponse
import com.example.bogoargo.data.dto.response.TeamDeleteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface TeamApiService {

    // 팀 생성 (교사 기능)
    @POST("api/teacher/classes/{classId}/teams")
    suspend fun createTeam(
        @Path("classId") classId: Long,
        @Body teamCreateRequest: TeamCreateRequest
    ): Response<TeamCreateResponse>

    // 팀 배정 (교사 기능)
    @POST("api/teacher/classes/{classId}/teams/{teamId}/assign")
    suspend fun AssignTeam(
        @Path("classId") classId: Long,
        @Path("teamId") teamId: Long
    ): Response<TeamAssignResponse>

    // 팀 랜덤 배정 (교사 기능)
    @POST("api/teacher/classes/{classId}/teams/random-assign")
    suspend fun AssignTeamRandom(
        @Path("classId") classId: Long,
    ): Response<TeamAssignResponse>

    // 팀 삭제 (교사 기능)
    @DELETE("api/teacher/classes/{classId}/teams/{teamId}")
    suspend fun deleteTeam(
        @Path("classId") classId: Long,
        @Path("teamId") teamId: Long
    ): Response<TeamDeleteResponse>



}