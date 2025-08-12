package com.example.bogoargo.data.api

import com.example.bogoargo.domain.model.LoginRequest
import com.example.bogoargo.domain.model.LoginResponse
import com.example.bogoargo.domain.model.MissionSpotsResponse
import com.example.bogoargo.domain.model.RefreshTokenRequest
import com.example.bogoargo.domain.model.RefreshTokenResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApiService {
    @POST("api/users/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @POST("api/users/reissue")
    fun refreshToken(@Header("Authorization") authHeader: String): Call<RefreshTokenResponse>
    
    @GET("api/auth/validate")
    suspend fun validateToken(): Response<Unit>
}