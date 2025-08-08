package com.example.bogoargo.domain.repository

import com.example.bogoargo.domain.model.Location


interface ILocationRepository {
    suspend fun getLocations(): List<Location>
}