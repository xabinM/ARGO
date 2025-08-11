package com.example.bogoargo.domain.repository

import com.example.bogoargo.data.dto.response.StudentsLocationResponse
import com.example.bogoargo.domain.model.Location


interface ILocationRepository {
    suspend fun getLocations(): List<Location>
    suspend fun sendLocationToServer(
        latitude: Double,
        longitude: Double
    ): Result<Unit>
    suspend fun getStudentLocationsByClass(
        classId: Long
    ): Result<StudentsLocationResponse>
}