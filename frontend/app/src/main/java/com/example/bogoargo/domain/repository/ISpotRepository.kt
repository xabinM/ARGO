package com.example.bogoargo.domain.repository

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.Spot

interface ISpotRepository {
    suspend fun getSpotList(classId: Long): DataResult<List<Spot>>
}