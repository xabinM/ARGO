package com.argo.backend.mission.dto.problemGenerate;

import com.argo.backend.domain.ploblem.QuizProblem;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProblemGenerateResponse {

    private List<QuizProblem> problems;
}
