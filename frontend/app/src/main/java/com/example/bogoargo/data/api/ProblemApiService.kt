package com.example.bogoargo.data.api

import com.example.bogoargo.data.dto.response.ProblemListSpotResponseDto
import com.example.bogoargo.data.dto.response.ProblemListSpotTypeResponseDto
import com.example.bogoargo.data.dto.response.ProblemMessageResponse
import com.example.bogoargo.data.dto.response.SelfieResultResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

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

    // 셀피 포즈 검증 요청 (학생 기능)
    @Multipart
    @POST("api/problem/selfie/determine")
    suspend fun determineSelfiePose(
        @Part image: MultipartBody.Part,
        @Part("pose") pose: RequestBody
    ): Response<SelfieResultResponseDto>


}