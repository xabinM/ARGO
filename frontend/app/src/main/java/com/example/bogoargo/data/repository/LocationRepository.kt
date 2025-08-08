package com.example.bogoargo.data.repository

import com.example.bogoargo.data.api.LocationApiService
import com.example.bogoargo.data.mapper.toDomainModel
import com.example.bogoargo.domain.model.Location
import com.example.bogoargo.domain.repository.ILocationRepository
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val locationApiService: LocationApiService
) : ILocationRepository{

    override suspend fun getLocations(): List<Location> {
        val response = locationApiService.getLocations()
        return response.data.map { it.toDomainModel() }
    }
}