package com.argo.backend.mission.dto.problemGenerate;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProblemGenerateRequestToAI {

    @NotBlank
    private String spotName;

    @NotBlank
    private Integer grade;

    @NotBlank
    private int problemCnt;
}
