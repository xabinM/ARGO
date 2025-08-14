package com.example.bogoargo.data.repository

import android.util.Log
import com.example.bogoargo.data.api.SpotApiService
import com.example.bogoargo.data.mapper.toDomain
import com.example.bogoargo.domain.model.DataException
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.Spot
import com.example.bogoargo.domain.repository.ISpotRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotRepository @Inject constructor(
    private val spotApiService: SpotApiService
) : ISpotRepository {

    override suspend fun getSpotList(classId: Long): DataResult<List<Spot>> {
        return try {
            val response = spotApiService.getSpotList(classId)
            DataResult.Success(response.data.toDomain())
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
            Log.d("에러", e.message.toString())
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }
}