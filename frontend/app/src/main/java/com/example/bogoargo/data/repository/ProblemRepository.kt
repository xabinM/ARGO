package com.example.bogoargo.data.repository

import com.example.bogoargo.data.api.ProblemApiService
import com.example.bogoargo.data.dto.request.ProblemCreateRequest
import com.example.bogoargo.data.dto.response.ProblemListSpotResponseDto
import com.example.bogoargo.data.dto.response.ProblemListSpotTypeResponseDto
import com.example.bogoargo.data.dto.response.ProblemMessageResponse
import com.example.bogoargo.domain.model.DataException
import com.example.bogoargo.domain.repository.IProblemRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ProblemRepositoryImpl @Inject constructor(
    private val problemApiService: ProblemApiService
) : IProblemRepository {
    
    override suspend fun createProblem(problemCreateRequest: ProblemCreateRequest): ProblemMessageResponse {
        return try {
            problemApiService.createProblem(problemCreateRequest.spotId)
        } catch (e: IOException) {
            throw DataException.NetworkError
        } catch (e: HttpException) {
            throw when (e.code()) {
                401 -> DataException.AuthenticationError
                403 -> DataException.UnauthorizedError
                404 -> DataException.NotFoundError
                else -> DataException.ServerError
            }
        } catch (e: Exception) {
            throw DataException.UnknownError(e.message ?: "Unknown error")
        }
    }

    override suspend fun getProblemBySpot(spotId: Long): ProblemListSpotResponseDto {
        return try {
            problemApiService.getProblemBySpot(spotId)
        } catch (e: IOException) {
            throw DataException.NetworkError
        } catch (e: HttpException) {
            throw when (e.code()) {
                401 -> DataException.AuthenticationError
                403 -> DataException.UnauthorizedError
                404 -> DataException.NotFoundError
                else -> DataException.ServerError
            }
        } catch (e: Exception) {
            throw DataException.UnknownError(e.message ?: "Unknown error")
        }
    }

    override suspend fun getProblemBySpotAndType(
        spotId: Long,
        type: String
    ): ProblemListSpotTypeResponseDto {
        return try {
            problemApiService.getProblemBySpotAndType(spotId, type)
        } catch (e: IOException) {
            throw DataException.NetworkError
        } catch (e: HttpException) {
            throw when (e.code()) {
                401 -> DataException.AuthenticationError
                403 -> DataException.UnauthorizedError
                404 -> DataException.NotFoundError
                else -> DataException.ServerError
            }
        } catch (e: Exception) {
            throw DataException.UnknownError(e.message ?: "Unknown error")
        }
    }
}
