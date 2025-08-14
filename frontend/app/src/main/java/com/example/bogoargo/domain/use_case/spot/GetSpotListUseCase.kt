package com.example.bogoargo.domain.use_case.spot

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.Spot
import com.example.bogoargo.domain.repository.ISpotRepository
import javax.inject.Inject

class GetSpotListUseCase @Inject constructor(
    private val spotRepository: ISpotRepository
) {
    suspend operator fun invoke(classId: Long): DataResult<List<Spot>> {
        return spotRepository.getSpotList(classId)
    }
}