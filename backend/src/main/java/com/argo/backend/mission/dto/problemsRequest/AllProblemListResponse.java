package com.argo.backend.mission.dto.problemsRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AllProblemListResponse {

    private List<ProblemResponseDto> problems;
}
