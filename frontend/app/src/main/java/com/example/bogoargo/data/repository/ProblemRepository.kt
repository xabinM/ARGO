package com.example.bogoargo.data.repository

import com.example.bogoargo.data.api.ProblemApiService
import com.example.bogoargo.data.dto.request.ProblemGenerateRequest
import com.example.bogoargo.data.dto.request.ProblemRegisterRequest
import com.example.bogoargo.data.dto.response.ProblemListSpotResponseDto
import com.example.bogoargo.data.dto.response.ProblemListSpotTypeResponseDto
import com.example.bogoargo.data.dto.response.ProblemResponseDto
import com.example.bogoargo.domain.model.DataException
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.IProblemRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ProblemRepositoryImpl @Inject constructor(
    private val problemApiService: ProblemApiService
) : IProblemRepository {
    
    override suspend fun generateProblem(problemGenerateRequest: ProblemGenerateRequest): DataResult<ProblemResponseDto> {
        return try {
            val response = problemApiService.generateProblem(problemGenerateRequest)
            DataResult.Success(response)
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            val exception = when (e.code()) {
                401 -> DataException.AuthenticationError
                403 -> DataException.UnauthorizedError
                404 -> DataException.NotFoundError
                else -> DataException.ServerError
            }
            DataResult.Error(exception)
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun registerProblem(problemRegisterRequest: ProblemRegisterRequest): DataResult<ProblemResponseDto> {
        return try {
            val response = problemApiService.registerProblem(problemRegisterRequest.spotId, problemRegisterRequest)
            DataResult.Success(response)
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            val exception = when (e.code()) {
                401 -> DataException.AuthenticationError
                403 -> DataException.UnauthorizedError
                404 -> DataException.NotFoundError
                else -> DataException.ServerError
            }
            DataResult.Error(exception)
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun getProblemBySpot(spotId: Long): DataResult<ProblemListSpotResponseDto> {
        return try {
            val response = problemApiService.getProblemBySpot(spotId)
            DataResult.Success(response)
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            val exception = when (e.code()) {
                401 -> DataException.AuthenticationError
                403 -> DataException.UnauthorizedError
                404 -> DataException.NotFoundError
                else -> DataException.ServerError
            }
            DataResult.Error(exception)
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun getProblemBySpotAndType(
        spotId: Long,
        type: String
    ): DataResult<ProblemListSpotTypeResponseDto> {
        return try {
            val response = problemApiService.getProblemBySpotAndType(spotId, type)
            DataResult.Success(response)
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            val exception = when (e.code()) {
                401 -> DataException.AuthenticationError
                403 -> DataException.UnauthorizedError
                404 -> DataException.NotFoundError
                else -> DataException.ServerError
            }
            DataResult.Error(exception)
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }
}
