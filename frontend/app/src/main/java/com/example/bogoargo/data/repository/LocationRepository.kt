package com.example.bogoargo.data.repository

import android.util.Log
import com.example.bogoargo.data.api.LocationApiService
import com.example.bogoargo.data.dto.request.UserCoordinatesRequest
import com.example.bogoargo.data.dto.response.LocationResponseDto
import com.example.bogoargo.data.dto.response.StudentsLocationResponse
import com.example.bogoargo.data.mapper.toDomainModel
import com.example.bogoargo.domain.model.Location
import com.example.bogoargo.domain.model.StudentLocationData
import com.example.bogoargo.domain.model.UserCoordinates
import com.example.bogoargo.domain.repository.ILocationRepository
import java.math.BigDecimal
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val locationApiService: LocationApiService
) : ILocationRepository{

    override suspend fun getLocations(): List<Location> {
        val response = locationApiService.getLocations()
        return response.data.map { it.toDomainModel() }
    }

    override suspend fun sendLocationToServer(
        latitude: Double,
        longitude: Double
    ): Result<Unit> {
        return try {
            val request = UserCoordinatesRequest(
                latitude = BigDecimal.valueOf(latitude),
                longitude = BigDecimal.valueOf(longitude)
            )

            val response = locationApiService.updateUserCoordinates(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "Location sent successfully: $latitude, $longitude")
                Result.success(Unit)
            } else {
                val errorMessage = "Failed to send location: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending location to server", e)
            Result.failure(e)
        }
    }

    override suspend fun getStudentLocationsByClass(
        classId: Long
    ): Result<StudentLocationData> {
        return try {
            val response = locationApiService.getStudentLocationsByClass(classId)

            if (response.isSuccessful) {
                val studentsLocation = response.body()
                if (studentsLocation != null) {
                    Log.d(TAG, "Successfully retrieved ${studentsLocation.students.size} student locations for class $classId")
                    Result.success(studentsLocation.toDomainModel())
                } else {
                    val errorMessage = "Empty response for class $classId student locations"
                    Log.e(TAG, errorMessage)
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = "Failed to get student locations for class $classId: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting student locations for class $classId", e)
            Result.failure(e)
        }
    }

    override suspend fun getCoordinatesByClass(
        classId: Long
    ): Result<List<UserCoordinates>> {
        return try {
            val response = locationApiService.getCoordinatesByClass(classId)

            if (response.isSuccessful) {
                val coordinatesResponse = response.body()
                if (coordinatesResponse != null && coordinatesResponse.success) {
                    Log.d(TAG, "Successfully retrieved ${coordinatesResponse.coordinates.size} coordinates for class $classId")
                    Result.success(coordinatesResponse.toDomainModel())
                } else {
                    val errorMessage = coordinatesResponse?.message ?: "Empty response for class $classId coordinates"
                    Log.e(TAG, errorMessage)
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = "Failed to get coordinates for class $classId: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting coordinates for class $classId", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "LocationRepository"
    }
}