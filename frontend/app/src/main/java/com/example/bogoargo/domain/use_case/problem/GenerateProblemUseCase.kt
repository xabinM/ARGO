package com.example.bogoargo.domain.use_case.problem

import com.example.bogoargo.data.dto.request.ProblemGenerateRequest
import com.example.bogoargo.data.dto.response.ProblemResponseDto
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.IProblemRepository
import javax.inject.Inject

class GenerateProblemUseCase @Inject constructor(
    private val problemRepository: IProblemRepository
) {
    suspend operator fun invoke(
        spotId: Long,
        grade: Int,
        problemCount: Int
    ): DataResult<ProblemResponseDto> {
        val request = ProblemGenerateRequest(
            spotId = spotId,
            grade = grade,
            problemCnt = problemCount
        )
        return problemRepository.generateProblem(request)
    }
}
