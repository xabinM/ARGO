package com.argo.backend.mission.dto.problemGenerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProblemGenerateRequestToAI {

    private String spotName;
    private int problemCnt;
}
