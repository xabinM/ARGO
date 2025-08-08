package com.example.bogoargo.data.repository

import com.example.bogoargo.data.api.LocationApiService
import com.example.bogoargo.data.dto.response.LocationResponseDto
import com.example.bogoargo.data.mapper.toDomainModel
import com.example.bogoargo.domain.model.Location
import com.example.bogoargo.domain.repository.ILocationRepository
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val locationApiService: LocationApiService
) : ILocationRepository{

    override suspend fun getLocations(): List<Location> {
        val list: List<LocationResponseDto> = locationApiService.getLocations()
        return list.map { it.toDomainModel() }
    }
}