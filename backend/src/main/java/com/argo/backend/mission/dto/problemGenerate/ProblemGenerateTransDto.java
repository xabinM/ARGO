package com.argo.backend.mission.dto.problemGenerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProblemGenerateTransDto {

    private Integer grade;
    private String spotName;
    private ProblemGenerateDto problems;
}
