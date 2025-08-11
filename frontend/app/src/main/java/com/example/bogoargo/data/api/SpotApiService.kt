package com.example.bogoargo.data.api

import com.example.bogoargo.data.dto.response.SpotDataDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface SpotApiService {

    // 스팟 목록 조회
    @POST("api/teacher/classes/{classId}/spots")
    suspend fun getSpotList(
        @Query("classId") classId: Long
    ): List<SpotDataDto >
}