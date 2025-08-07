package com.example.bogoargo.domain.repository

import com.example.bogoargo.data.dto.request.ProblemCreateRequest
import com.example.bogoargo.data.dto.response.ProblemListSpotResponseDto
import com.example.bogoargo.data.dto.response.ProblemListSpotTypeResponseDto
import com.example.bogoargo.data.dto.response.ProblemMessageResponse

interface IProblemRepository {
    suspend fun createProblem(problemCreateRequest: ProblemCreateRequest): ProblemMessageResponse
    suspend fun getProblemBySpot(spotId: Long): ProblemListSpotResponseDto
    suspend fun getProblemBySpotAndType(spotId: Long, type: String): ProblemListSpotTypeResponseDto


}