package com.example.bogoargo.data.repository

import com.example.bogoargo.data.api.TeamApiService
import com.example.bogoargo.data.dto.TeamCreateRequest
import com.example.bogoargo.data.dto.response.TeamAssignResponse
import com.example.bogoargo.data.mapper.toDomainModel
import com.example.bogoargo.domain.model.DataException
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.Team
import com.example.bogoargo.domain.model.User
import com.example.bogoargo.domain.repository.ITeamRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class TeamRepositoryImpl @Inject constructor(
    private val teamApiService: TeamApiService
) : ITeamRepository {
    
    override suspend fun createTeam(classId: Long, teamName: String, maxMembers: Int): DataResult<Team> {
        return try {
            val teamCreateRequest = TeamCreateRequest(
                teamName = teamName,
                maxMembers = maxMembers
            )
            val response = teamApiService.createTeam(classId, teamCreateRequest)
            if (response.isSuccessful) {
                val teamCreateResponse = response.body()
                if (teamCreateResponse?.success == true && teamCreateResponse.data != null) {
                    DataResult.Success(teamCreateResponse.data.toDomainModel())
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

    override suspend fun getTeams(): DataResult<List<Team>> {
        return DataResult.Error(DataException.UnknownError("Not implemented"))
    }

    override suspend fun getTeamById(teamId: Long): DataResult<Team> {
        return DataResult.Error(DataException.UnknownError("Not implemented"))
    }

    override suspend fun updateTeam(teamId: Long): DataResult<Team> {
        return DataResult.Error(DataException.UnknownError("Not implemented"))
    }

    override suspend fun deleteTeam(teamId: Long): DataResult<Unit> {
        return DataResult.Error(DataException.UnknownError("Not implemented"))
    }

    override suspend fun joinTeam(teamId: Long): DataResult<Unit> {
        return DataResult.Error(DataException.UnknownError("Not implemented"))
    }

    override suspend fun leaveTeam(teamId: Long): DataResult<Unit> {
        return DataResult.Error(DataException.UnknownError("Not implemented"))
    }

    override suspend fun deleteTeam(classId: Long, teamId: Long): DataResult<List<User>> {
        return try {
            val response = teamApiService.deleteTeam(classId, teamId)
            if (response.isSuccessful) {
                val deleteResponse = response.body()
                if (deleteResponse?.success == true) {
                    // TODO: 실제 User 리스트로 변환 구현 필요
                    DataResult.Success(emptyList())
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

    override suspend fun assignTeam(classId: Long, teamId: Long): DataResult<TeamAssignResponse> {
        return try {
            val response = teamApiService.AssignTeam(classId, teamId)
            if (response.isSuccessful) {
                val assignResponse = response.body()
                if (assignResponse?.success == true) {
                    DataResult.Success(assignResponse)
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

    override suspend fun assignTeamRandom(classId: Long): DataResult<TeamAssignResponse> {
        return try {
            val response = teamApiService.AssignTeamRandom(classId)
            if (response.isSuccessful) {
                val assignResponse = response.body()
                if (assignResponse?.success == true) {
                    DataResult.Success(assignResponse)
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