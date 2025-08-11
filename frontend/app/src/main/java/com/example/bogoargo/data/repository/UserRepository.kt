package com.example.bogoargo.data.repository

import com.example.bogoargo.data.api.UserApiService
import com.example.bogoargo.data.dto.UserSignUpRequest
import com.example.bogoargo.data.dto.UserLoginRequest
import com.example.bogoargo.data.dto.UserUpdateRequest
import com.example.bogoargo.data.dto.UserWithdrawRequest
import com.example.bogoargo.data.dto.response.UserUpdateResponse
import com.example.bogoargo.data.dto.response.UserWithdrawResponse
import com.example.bogoargo.data.dto.response.MessageResponseDto
import com.example.bogoargo.data.mapper.toDomainModel
import com.example.bogoargo.data.preferences.UserPreferences
import com.example.bogoargo.data.storage.TokenStorage
import com.example.bogoargo.domain.model.DataException
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.User
import com.example.bogoargo.domain.model.UserRole
import com.example.bogoargo.domain.repository.IUserRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
    private val tokenStorage: TokenStorage,
    private val userPreferences: UserPreferences
) : IUserRepository {
    
    override suspend fun signUp(userSignUpRequest: UserSignUpRequest): DataResult<MessageResponseDto> {
        return try {
            val response = userApiService.signup(userSignUpRequest)
            if (response.isSuccessful) {
                val messageResponse = response.body()
                if (messageResponse?.success == true) {
                    DataResult.Success(messageResponse)
                } else {
                    DataResult.Error(DataException.ServerError)
                }
            } else {
                DataResult.Error(DataException.ServerError)
            }
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            DataResult.Error(
                when (e.code()) {
                    401 -> DataException.AuthenticationError
                    403 -> DataException.UnauthorizedError
                    404 -> DataException.NotFoundError
                    else -> DataException.ServerError
                }
            )
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun login(request: UserLoginRequest): DataResult<User> {
        return try {
            val response = userApiService.login(request)
            if (response.isSuccessful) {
                val loginResponse = response.body()
                val accessToken = response.body()?.tokens?.accessToken
                val refreshToken = response.body()?.tokens?.refreshToken
                //TODO: success 변경
                if (loginResponse?.message.equals("로그인에 성공했습니다.") && loginResponse?.name != null &&
                    accessToken != null && refreshToken != null) {
                    
                    // 토큰 저장
                    tokenStorage.saveTokens(accessToken, refreshToken)

                    // 유저 저장
                    val loggedInUser = User(
                        userId = loginResponse.userId,
                        name = loginResponse.name,
                        role = UserRole.valueOf(loginResponse.role),
                        team = null
                    )
                    userPreferences.saveUser(loggedInUser)
                    
                    DataResult.Success(loggedInUser)
                } else {
                    DataResult.Error(DataException.AuthenticationError)
                }
            } else {
                DataResult.Error(DataException.ServerError)
            }
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            DataResult.Error(
                when (e.code()) {
                    401 -> DataException.AuthenticationError
                    403 -> DataException.UnauthorizedError
                    404 -> DataException.NotFoundError
                    else -> DataException.ServerError
                }
            )
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun getUserProfile(): DataResult<User> {
        return try {
            // TODO: API 호출 구현
            DataResult.Error(DataException.UnknownError("Not implemented"))
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun updateUserProfile(user: User): DataResult<User> {
        return try {
            // TODO: API 호출 구현
            DataResult.Error(DataException.UnknownError("Not implemented"))
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun updateUserInfo(userUpdateRequest: UserUpdateRequest): DataResult<UserUpdateResponse> {
        return try {
            val response = userApiService.updateUserInfo(userUpdateRequest)
            if (response.isSuccessful) {
                val updateResponse = response.body()
                if (updateResponse?.success == true) {
                    DataResult.Success(updateResponse)
                } else {
                    DataResult.Error(DataException.ServerError)
                }
            } else {
                DataResult.Error(DataException.ServerError)
            }
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            DataResult.Error(
                when (e.code()) {
                    401 -> DataException.AuthenticationError
                    403 -> DataException.UnauthorizedError
                    404 -> DataException.NotFoundError
                    else -> DataException.ServerError
                }
            )
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }
    
    override suspend fun withdrawUser(userWithdrawRequest: UserWithdrawRequest): DataResult<UserWithdrawResponse> {
        return try {
            val response = userApiService.withrawUser(userWithdrawRequest)
            if (response.isSuccessful) {
                val withdrawResponse = response.body()
                if (withdrawResponse?.success == true) {
                    DataResult.Success(withdrawResponse)
                } else {
                    DataResult.Error(DataException.ServerError)
                }
            } else {
                DataResult.Error(DataException.ServerError)
            }
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            DataResult.Error(
                when (e.code()) {
                    401 -> DataException.AuthenticationError
                    403 -> DataException.UnauthorizedError
                    404 -> DataException.NotFoundError
                    else -> DataException.ServerError
                }
            )
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun getLoggedInUser(): User? {
        return userPreferences.getUser()
    }
}