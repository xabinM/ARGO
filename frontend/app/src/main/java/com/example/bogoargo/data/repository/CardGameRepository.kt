package com.example.bogoargo.data.repository

import com.example.bogoargo.data.api.CardGameApiService
import com.example.bogoargo.data.mapper.BattleHistoryPagination
import com.example.bogoargo.data.mapper.toDomainModel
import com.example.bogoargo.domain.model.DataException
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.ICardGameRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CardGameRepositoryImpl @Inject constructor(
    private val cardGameApiService: CardGameApiService
) : ICardGameRepository {

    override suspend fun getBattleHistory(teamId: Long, page: Int, size: Int): DataResult<BattleHistoryPagination> {
        return try {
            val response = cardGameApiService.getBattleHistory(teamId, page, size)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    val domainModel = apiResponse.data.toDomainModel(teamId)
                    DataResult.Success(domainModel)
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
}