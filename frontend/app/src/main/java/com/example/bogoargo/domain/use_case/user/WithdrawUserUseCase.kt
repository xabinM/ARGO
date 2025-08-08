package com.example.bogoargo.domain.use_case.user

import com.example.bogoargo.data.dto.UserWithdrawRequest
import com.example.bogoargo.data.dto.response.UserWithdrawResponse
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.IUserRepository
import javax.inject.Inject

class WithdrawUserUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(password: String): DataResult<UserWithdrawResponse> {
        val request = UserWithdrawRequest(password = password)
        return userRepository.withdrawUser(request)
    }
}