package com.example.bogoargo.data.api

import com.example.bogoargo.data.dto.response.SpotListResponse
import retrofit2.http.GET
import retrofit2.http.Path


interface SpotApiService {

    // 스팟 목록 조회
    @GET("api/teacher/classes/{classId}/spots")
    suspend fun getSpotList(
        @Path("classId") classId: Long
    ): SpotListResponse
}