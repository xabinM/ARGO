package com.argo.backend.mission.dto.problemGenerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProblemGenerateRequestFromCli {

    private Long spotId;
    private Integer grade;
    private int problemCnt;
}
