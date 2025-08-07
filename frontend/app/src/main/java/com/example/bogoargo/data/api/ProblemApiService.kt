package com.example.bogoargo.data.api

import com.example.bogoargo.data.dto.response.ProblemListSpotResponseDto
import com.example.bogoargo.data.dto.response.ProblemListSpotTypeResponseDto
import com.example.bogoargo.data.dto.response.ProblemMessageResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProblemApiService {


    // 문제 생성 요청 (교사 기능)
    @POST("api/problem/create/register/spot/{spotId}")
    suspend fun createProblem(
        @Path("spotId") spotId: Long
    ): ProblemMessageResponse

    // 스팟 별 문제 리스트 요청
    @GET("api/problem/spot/{spotId}")
    suspend fun getProblemBySpot(
        @Path("spotId") spotId: Long
    ): ProblemListSpotResponseDto

    // 스팟 + serfie타입 문제 리스트 요청
    @GET("api/problem/spot/{spotId}/perType")
    suspend fun getProblemBySpotAndType(
        @Path("spotId") spotId: Long,
        @Query("type") type: String
    ): ProblemListSpotTypeResponseDto




}