package com.example.bogoargo.data.repository

import com.example.bogoargo.data.api.ApiClient
import com.example.bogoargo.data.model.LoginRequest
import com.example.bogoargo.data.model.LoginResponse
import retrofit2.Response

class LoginRepository {
    private val authApiService = ApiClient.authApiService
    
    suspend fun login(id: String, password: String): Response<LoginResponse> {
        val loginRequest = LoginRequest(id, password)
        return authApiService.login(loginRequest)
    }
}