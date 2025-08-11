package com.example.bogoargo.data.api

import com.example.bogoargo.data.dto.request.UserCoordinatesRequest
import com.example.bogoargo.data.dto.response.LocationResponseDto
import com.example.bogoargo.data.dto.response.StudentsLocationResponse
import com.example.bogoargo.data.dto.response.UpdateCoordinatesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LocationApiService {

    @POST("api/gps")
    suspend fun updateUserCoordinates(
        @Body request: UserCoordinatesRequest
    ): Response<UpdateCoordinatesResponse>

    @GET("api/teacher/class/{classId}/students/locations")
    suspend fun getStudentLocationsByClass(
        @Path("classId") classId: Long
    ): Response<StudentsLocationResponse>

    suspend fun getLocations(): LocationResponseDto
}