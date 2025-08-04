package com.example.bogoargo.data.repository

import com.example.bogoargo.data.api.UserApiService
import com.example.bogoargo.data.dto.UserSignUpRequest
import com.example.bogoargo.data.dto.UserLoginRequest
import com.example.bogoargo.data.dto.UserUpdateRequest
import com.example.bogoargo.data.dto.UserWithdrawRequest
import com.example.bogoargo.data.dto.response.UserLoginResponse
import com.example.bogoargo.data.dto.response.UserUpdateResponse
import com.example.bogoargo.data.dto.response.UserWithdrawResponse
import com.example.bogoargo.data.dto.response.MessageResponseDto
import com.example.bogoargo.data.mapper.toDomainModel
import com.example.bogoargo.data.model.User
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService,
    private val authRepository: AuthRepository
) {
    
    // 회원가입
    suspend fun signUp(userSignUpRequest: UserSignUpRequest): Result<MessageResponseDto?> {
        return try {
            val response = userApiService.signup(userSignUpRequest)
            if (response.isSuccessful) {
                val messageResponse = response.body()
                if (messageResponse?.success == true) {
                    Result.success(messageResponse)
                } else {
                    Result.failure(Exception(messageResponse?.message ?: "회원가입에 실패했습니다."))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 로그인
    suspend fun login(userLoginRequest: UserLoginRequest): Result<User?> {
        return try {
            val response = userApiService.login(userLoginRequest)
            if (response.isSuccessful) {
                val loginResponse = response.body()
                val jwtToken = response.headers()["Authorization"]?.replace("Bearer ", "")

                if (loginResponse?.success == true && loginResponse.data != null) {
                    //authRepository.saveAuthInfo(jwtToken, loginResponse) TODO: auth repo save기능
                    Result.success(loginResponse.data.toDomainModel())
                } else {
                    Result.failure(Exception(loginResponse?.message ?: "로그인에 실패했습니다."))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)

        }
    }

    // 회원 정보 수정
    suspend fun updateUserInfo(userUpdateRequest: UserUpdateRequest): Result<UserUpdateResponse?> {
        return try {
            val response = userApiService.updateUserInfo(userUpdateRequest)
            if (response.isSuccessful) {
                val updateResponse = response.body()
                if (updateResponse?.success == true) {
                    Result.success(updateResponse)
                } else {
                    Result.failure(Exception(updateResponse?.message ?: "회원 정보 수정에 실패했습니다."))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 회원 탈퇴
    suspend fun withdrawUser(userWithdrawRequest: UserWithdrawRequest): Result<UserWithdrawResponse?> {
        return try {
            val response = userApiService.withrawUser(userWithdrawRequest)
            if (response.isSuccessful) {
                val withdrawResponse = response.body()
                if (withdrawResponse?.success == true) {
                    Result.success(withdrawResponse)
                } else {
                    Result.failure(Exception(withdrawResponse?.message ?: "회원 탈퇴에 실패했습니다."))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}