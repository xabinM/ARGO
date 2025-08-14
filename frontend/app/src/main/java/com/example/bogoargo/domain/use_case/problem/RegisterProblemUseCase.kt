package com.example.bogoargo.domain.use_case.problem

import com.example.bogoargo.data.dto.request.ProblemRegisterRequest
import com.example.bogoargo.data.dto.response.ProblemResponseDto
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.repository.IProblemRepository
import javax.inject.Inject

class RegisterProblemUseCase @Inject constructor(
    private val problemRepository: IProblemRepository
) {
    suspend operator fun invoke(
        spotId: Long,
        question: String,
        choices: List<String>,
        correctIndex: Int,
        explanation: String,
        grade: Long
    ): DataResult<ProblemResponseDto> {
        val request = ProblemRegisterRequest(
            question = question,
            choices = choices,
            correctIndex = correctIndex,
            explanation = explanation,
            spotId = spotId,
            grade = grade
        )
        return problemRepository.registerProblem(request)
    }
}