package com.example.bogoargo.domain.use_case.auth

import com.example.bogoargo.data.dto.UserLoginRequest
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.User
import com.example.bogoargo.domain.repository.IUserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(username: String, password: String): DataResult<User> {
        val request = UserLoginRequest(username = username, password = password)
        return userRepository.login(request)
    }
}