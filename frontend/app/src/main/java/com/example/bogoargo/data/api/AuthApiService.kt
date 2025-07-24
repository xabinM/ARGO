package com.example.bogoargo.data.api

import com.example.bogoargo.data.model.LoginRequest
import com.example.bogoargo.data.model.LoginResponse
import com.example.bogoargo.data.model.MissionSpotsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApiService {
    @POST("api/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @GET("api/auth/validate")
    suspend fun validateToken(): Response<Unit>
    
    @GET("api/classes/{classId}/missions/spots")
    suspend fun getMissionSpots(@Path("classId") classId: Long): Response<MissionSpotsResponse>
}