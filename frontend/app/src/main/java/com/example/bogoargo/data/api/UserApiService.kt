package com.example.bogoargo.data.api

import com.example.bogoargo.data.dto.UserLoginRequest
import com.example.bogoargo.data.dto.UserSignUpRequest
import com.example.bogoargo.data.dto.UserUpdateRequest
import com.example.bogoargo.data.dto.UserWithdrawRequest
import com.example.bogoargo.data.dto.response.MessageResponseDto
import com.example.bogoargo.data.dto.response.UserDataDto
import com.example.bogoargo.data.dto.response.UserLoginResponse
import com.example.bogoargo.data.dto.response.UserUpdateResponse
import com.example.bogoargo.data.dto.response.UserWithdrawResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApiService {

    // 회원 가입
    @POST("api/users/signup")
    suspend fun sighup(
        @Body userSignUpRequest: UserSignUpRequest
    ): Response<MessageResponseDto>

    // 로그인
    @POST("api/users/login")
    suspend fun login(
        @Body userLoginRequest: UserLoginRequest
    ): Response<UserLoginResponse>

    // 회원 정보 수정
    @PUT("api/users/update")
    suspend fun updateUserInfo(
        @Body userUpdateRequest: UserUpdateRequest
    ): Response<UserUpdateResponse>

    // 회원 탈퇴
    @DELETE("api/users/withdraw")
    suspend fun withrawUser(
        @Body userWithdrawRequest: UserWithdrawRequest
    ): Response<UserWithdrawResponse>
}