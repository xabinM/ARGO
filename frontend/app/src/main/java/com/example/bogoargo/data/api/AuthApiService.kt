package com.example.bogoargo.data.api

import com.example.bogoargo.data.model.LoginRequest
import com.example.bogoargo.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @GET("api/auth/validate")
    suspend fun validateToken(): Response<Unit>
}