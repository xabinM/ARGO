package com.argo.backend.mission.dto.problemGenerate;

import com.argo.backend.domain.ploblem.entity.QuizProblem;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProblemGenerateDto {

    private List<QuizProblem> problems;
}
