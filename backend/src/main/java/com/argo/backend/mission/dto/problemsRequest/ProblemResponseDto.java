package com.argo.backend.mission.dto.problemsRequest;

import com.argo.backend.domain.ploblem.Problem;
import com.argo.backend.domain.ploblem.QuizProblem;
import com.argo.backend.domain.ploblem.SelfieProblem;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public abstract class ProblemResponseDto {

    private Long id;
    private String dtype;

    public static List<ProblemResponseDto> from(List<Problem> problems) {
        return problems.stream()
                .map(ProblemResponseDto::from)
                .collect(Collectors.toList());
    }

    private static ProblemResponseDto from(Problem problem) {
        if (problem instanceof QuizProblem) {
            return QuizProblemResponseDto.from((QuizProblem) problem);
        } else if (problem instanceof SelfieProblem) {
            return SelfieProblemResponseDto.from((SelfieProblem) problem);
        } else {
            throw new IllegalArgumentException("지원하지 않는 문제 유형입니다.");
        }
    }
}
