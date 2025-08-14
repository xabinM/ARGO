package com.example.bogoargo.domain.repository

import com.example.bogoargo.data.dto.request.ProblemGenerateRequest
import com.example.bogoargo.data.dto.request.ProblemRegisterRequest
import com.example.bogoargo.data.dto.response.ProblemListSpotResponseDto
import com.example.bogoargo.data.dto.response.ProblemListSpotTypeResponseDto
import com.example.bogoargo.data.dto.response.ProblemResponseDto
import com.example.bogoargo.domain.model.DataResult

interface IProblemRepository {
    suspend fun generateProblem(problemGenerateRequest: ProblemGenerateRequest): DataResult<ProblemResponseDto>
    suspend fun registerProblem(problemRegisterRequest: ProblemRegisterRequest): DataResult<ProblemResponseDto>
    suspend fun getProblemBySpot(spotId: Long): DataResult<ProblemListSpotResponseDto>
    suspend fun getProblemBySpotAndType(spotId: Long, type: String): DataResult<ProblemListSpotTypeResponseDto>
}