package com.example.bogoargo.domain.repository

import com.example.bogoargo.domain.model.Location
import com.example.bogoargo.domain.model.StudentLocationData
import com.example.bogoargo.domain.model.UserCoordinates


interface ILocationRepository {
    suspend fun getLocations(): List<Location>
    suspend fun sendLocationToServer(
        latitude: Double,
        longitude: Double
    ): Result<Unit>
    suspend fun getStudentLocationsByClass(
        classId: Long
    ): Result<StudentLocationData>
    suspend fun getCoordinatesByClass(
        classId: Long
    ): Result<List<UserCoordinates>>
}