package com.argo.backend.mission.dto.problemGenerate;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProblemGenerateRequestFromCli {

    @NotBlank
    private Long spotId;

    @NotBlank(message = "학년 정보를 입력해주세요.")
    private Integer grade;

    @NotBlank(message = "요청 문제 개수를 입력해주세요.")
    private int problemCnt;
}
