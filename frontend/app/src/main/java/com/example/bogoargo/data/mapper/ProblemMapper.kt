package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.dto.response.ProblemData
import com.example.bogoargo.data.dto.response.ProblemDataQuizDto
import com.example.bogoargo.data.dto.response.ProblemDataSelfieDto
import com.example.bogoargo.domain.model.QuizProblem
import com.example.bogoargo.domain.model.SelfieProblem

// ProblemDataQuizDto를 QuizProblem Model로 변환
//fun ProblemDataQuizDto.toDomainModel(): QuizProblem {
//    return QuizProblem(
//        id = ,
//        dtype = "QUIZ",
//        question = this.question,
//        choices = this.choices,
//        correctIndex = 0, // TODO: DTO에서 정답 인덱스 받아오기
//        explanation = "", // TODO: DTO에서 설명 받아오기
//        formattedQuestion = "Q. ${this.question}"
//    )
//}

// ProblemDataSelfieDto를 SelfieProblem Model로 변환
fun ProblemDataSelfieDto.toDomainModel(): SelfieProblem {
    return SelfieProblem(
        id = this.id,
        dtype = this.dtype,
        guideline = this.guideline,
        pose = this.pose,
        poseHint = this.poseHint,
        displayImageUrl = "" // 기본값 사용
    )
}

// ProblemData를 적절한 domain model로 변환
fun ProblemData.toDomainModel(): Any {
    return when (this) {
        is ProblemDataQuizDto -> this.toDomainModel()
        is ProblemDataSelfieDto -> this.toDomainModel()
        else -> throw IllegalArgumentException("Unknown problem data type: ${this.dtype}")
    }
}
