package com.argo.backend.mission.dto.problemGenerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProblemGenerateResponse {

    private boolean success;
    private String message;
    private Integer grade;
    private String spotName;
    private ProblemGenerateDto problems;
}
