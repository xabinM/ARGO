package com.example.bogoargo.domain.use_case.user

import com.example.bogoargo.data.dto.UserSignUpRequest
import com.example.bogoargo.data.dto.response.MessageResponseDto
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.IUserRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(
        username: String,
        password: String,
        name: String,
        role: String,
        agreeTerms: Boolean
    ): DataResult<MessageResponseDto> {
        val request = UserSignUpRequest(
            username = username,
            password = password,
            name = name,
            role = role,
            agreeTerms = agreeTerms
        )
        return userRepository.signUp(request)
    }
}