package com.example.bogoargo.data.api

import com.example.bogoargo.data.dto.mission.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface MissionApiService {

    // 미션 생성 API
    @POST("api/missions/create/team/{teamId}/spot/{spotId}")
    suspend fun createMission(
        @Path("teamId") teamId: Long,
        @Path("spotId") spotId: Long
    ): Response<MissionCreateResponseDto>

    // 미션 제출 API (퀴즈 미션용)
    @POST("api/missions/{missionId}/submit")
    suspend fun submitMission(
        @Path("missionId") missionId: Long,
        @Body request: MissionSubmitRequestDto
    ): Response<MissionSubmitResponseDto>

    // TODO: 셀피 미션 제출 API (백엔드 API 완성 후 구현)
    @Multipart
    @POST("api/missions/{missionId}/submit/selfie")
    suspend fun submitSelfieMission(
        @Path("missionId") missionId: Long,
        @Part image: MultipartBody.Part,
        @Part("pose") pose: String
    ): Response<MissionSubmitResponseDto>

    // TODO: 셀피 미션 검증 API (백엔드 API 완성 후 구현)
    @POST("api/missions/validate/selfie")
    suspend fun validateSelfie(
        @Body request: SelfieMissionSubmitRequestDto
    ): Response<SelfieMissionValidationResponseDto>

    // 미션 지점 가능 여부 확인 API
    @GET("api/missions/checkPossible/team/{teamId}/spot/{spotId}")
    suspend fun checkPossibleMissionSpot(
        @Path("teamId") teamId: Long,
        @Path("spotId") spotId: Long
    ): Response<MissionPossibleCheckResponseDto>
}