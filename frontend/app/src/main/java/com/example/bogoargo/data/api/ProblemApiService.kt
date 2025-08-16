package com.example.bogoargo.data.api

import com.example.bogoargo.data.dto.request.ProblemRegisterRequest
import com.example.bogoargo.data.dto.request.ProblemGenerateRequest
import com.example.bogoargo.data.dto.response.ProblemListSpotResponseDto
import com.example.bogoargo.data.dto.response.ProblemListSpotTypeResponseDto
import com.example.bogoargo.data.dto.response.ProblemResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.example.bogoargo.data.dto.response.ProblemMessageResponse
import com.example.bogoargo.data.dto.response.SelfieResultResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ProblemApiService {


    // 문제 생성 요청 (교사 기능)
    @POST("api/problem/generate")
    suspend fun generateProblem(
        @Body problemRequest: ProblemGenerateRequest
    ): ProblemResponseDto

    // 선택한 문제 등록 요청 (교사 기능)
    @POST("api/problem/register/spot/{spotId}")
    suspend fun registerProblem(
        @Path("spotId") spotId: Long,
        @Body problemRegisterRequest: ProblemRegisterRequest
    ): ProblemResponseDto

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
    @POST("api/problem/selfie/determine/team/{teamId}")
    suspend fun determineSelfiePose(
        @Path("teamId") teamId: Long,
        @Part image: MultipartBody.Part,
        @Part("pose") pose: RequestBody
    ): Response<SelfieResultResponseDto>


}