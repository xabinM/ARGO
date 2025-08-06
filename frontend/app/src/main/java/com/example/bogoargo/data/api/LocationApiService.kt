package com.example.bogoargo.data.api

import com.example.bogoargo.data.dto.response.LocationResponseDto
import retrofit2.http.GET

interface LocationApiService {

    @GET("api/teacher/classes/locations")
    suspend fun getLocations(): List<LocationResponseDto>
}