package com.example.bogoargo.domain.repository

import com.example.bogoargo.domain.model.AR3DObject
import com.example.bogoargo.domain.model.DataResult

interface IAR3DObjectRepository {
    suspend fun getAR3DObjects(): DataResult<List<AR3DObject>>
    suspend fun getAR3DObjectById(objectId: String): DataResult<AR3DObject>
}