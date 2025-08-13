package com.example.bogoargo.data.repository

import com.example.bogoargo.data.api.CardGameApiService
import com.example.bogoargo.data.mapper.*
import com.example.bogoargo.data.mapper.toDomainModel
import com.example.bogoargo.data.dto.request.BattleResponseDto
import com.example.bogoargo.data.dto.request.SelectedCardDto
import com.example.bogoargo.domain.model.*
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

    override suspend fun getTeamCardCollection(teamId: Long): DataResult<TeamCardCollection> {
        return try {
            val response = cardGameApiService.getTeamCardCollection(teamId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    val domainModel = apiResponse.data.toDomainModel()
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

    override suspend fun getBattleOpponents(teamId: Long): DataResult<List<BattleOpponent>> {
        return try {
            val response = cardGameApiService.getBattleOpponents(teamId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    val domainModels = apiResponse.data.map { it.toDomainModel() }
                    DataResult.Success(domainModels)
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

    override suspend fun createBattle(battleRequest: BattleRequest): DataResult<BattleResult> {
        return try {
            val requestDto = battleRequest.toRequestDto()
            val response = cardGameApiService.createBattle(requestDto)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    val domainModel = apiResponse.data.toDomainModel()
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

    override suspend fun respondToBattle(
        matchId: Long, 
        action: String, 
        selectedCardTeamCardId: Long?, 
        battleStance: BattleStance?
    ): DataResult<BattleResult> {
        return try {
            val selectedCard = if (action == "ACCEPT" && selectedCardTeamCardId != null && battleStance != null) {
                SelectedCardDto(
                    teamCardId = selectedCardTeamCardId,
                    battleStance = battleStance.name
                )
            } else null

            val requestDto = BattleResponseDto(
                action = action,
                selectedCard = selectedCard
            )
            
            val response = cardGameApiService.respondToBattle(matchId, requestDto)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    val domainModel = apiResponse.data.toDomainModel()
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

    override suspend fun cancelBattle(matchId: Long): DataResult<BattleResult> {
        return try {
            val response = cardGameApiService.cancelBattle(matchId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    val domainModel = apiResponse.data.toDomainModel()
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

    override suspend fun viewBattleResult(matchId: Long): DataResult<BattleResult> {
        return try {
            val response = cardGameApiService.viewBattleResult(matchId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    val domainModel = apiResponse.data.toDomainModel()
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

    override suspend fun getTeamStats(teamId: Long): DataResult<TeamCardStats> {
        return try {
            val response = cardGameApiService.getTeamStats(teamId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    val domainModel = apiResponse.data.toDomainModel()
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