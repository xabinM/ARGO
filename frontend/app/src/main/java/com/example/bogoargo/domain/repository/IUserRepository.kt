package com.example.bogoargo.domain.repository

import com.example.bogoargo.data.dto.UserLoginRequest
import com.example.bogoargo.data.dto.UserSignUpRequest
import com.example.bogoargo.data.dto.UserUpdateRequest
import com.example.bogoargo.data.dto.UserWithdrawRequest
import com.example.bogoargo.data.dto.response.MessageResponseDto
import com.example.bogoargo.data.dto.response.UserUpdateResponse
import com.example.bogoargo.data.dto.response.UserWithdrawResponse
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.User

interface IUserRepository {
    suspend fun login(request: UserLoginRequest): DataResult<User>
    suspend fun getUserProfile(): DataResult<User>
    suspend fun updateUserProfile(user: User): DataResult<User>
    suspend fun signUp(userSignUpRequest: UserSignUpRequest): DataResult<MessageResponseDto>
    suspend fun updateUserInfo(userUpdateRequest: UserUpdateRequest): DataResult<UserUpdateResponse>
    suspend fun withdrawUser(userWithdrawRequest: UserWithdrawRequest): DataResult<UserWithdrawResponse>
    suspend fun getLoggedInUser(): User?
}