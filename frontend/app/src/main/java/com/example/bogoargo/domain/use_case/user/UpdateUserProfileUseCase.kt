package com.example.bogoargo.domain.use_case.user

import com.example.bogoargo.data.dto.UserUpdateRequest
import com.example.bogoargo.data.dto.response.UserUpdateResponse
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(name: String, password: String): DataResult<UserUpdateResponse> {
        val request = UserUpdateRequest(name = name, password = password)
        return userRepository.updateUserInfo(request)
    }
}