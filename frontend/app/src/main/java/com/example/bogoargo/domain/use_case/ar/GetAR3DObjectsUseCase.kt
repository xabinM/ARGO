package com.example.bogoargo.domain.use_case.ar

import com.example.bogoargo.domain.model.AR3DObject
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.DataException
import com.example.bogoargo.data.repository.AR3DObjectRepository
import javax.inject.Inject

class GetAR3DObjectsUseCase @Inject constructor(
    private val ar3DObjectRepository: AR3DObjectRepository
) {
    fun getAllObjects(): DataResult<List<AR3DObject>> {
        return try {
            DataResult.Success(ar3DObjectRepository.getAllObjects())
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Failed to get AR3D objects"))
        }
    }
    
    fun getObjectById(objectId: String): DataResult<AR3DObject> {
        return try {
            val obj = ar3DObjectRepository.getObjectById(objectId)
            if (obj != null) {
                DataResult.Success(obj)
            } else {
                DataResult.Error(DataException.NotFoundError)
            }
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Failed to get AR3D object"))
        }
    }
}