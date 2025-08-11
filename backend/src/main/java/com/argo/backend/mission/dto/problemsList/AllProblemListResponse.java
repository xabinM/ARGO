package com.argo.backend.mission.dto.problemsList;

import com.argo.backend.mission.dto.common.ProblemDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AllProblemListResponse {

    private boolean success;
    private List<ProblemDetail> problems;
}
