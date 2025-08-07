package com.example.bogoargo.domain.use_case.location

import com.example.bogoargo.domain.model.Location
import com.example.bogoargo.domain.repository.ILocationRepository
import javax.inject.Inject

class getLocationsUseCase @Inject constructor(
    private val locationRepository: ILocationRepository
){
    suspend operator fun invoke(): List<Location> {
        return locationRepository.getLocations()
    }
}