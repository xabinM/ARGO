package com.argo.backend.mission.dto.common;

import com.argo.backend.domain.ploblem.entity.Problem;
import com.argo.backend.domain.ploblem.entity.QuizProblem;
import com.argo.backend.domain.ploblem.entity.SelfieProblem;
import com.argo.backend.mission.exception.problem.ProblemTypeNotExist;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public abstract class ProblemDetail {

    private Long id;
    private String dtype;

    public static List<ProblemDetail> from(List<Problem> problems) {
        return problems.stream()
                .map(ProblemDetail::from)
                .collect(Collectors.toList());
    }

    public static ProblemDetail from(Problem problem) {
        if (problem instanceof QuizProblem) {
            return QuizProblemDetail.from((QuizProblem) problem);
        } else if (problem instanceof SelfieProblem) {
            return SelfieProblemDetail.from((SelfieProblem) problem);
        } else {
            throw new ProblemTypeNotExist();
        }
    }
}
