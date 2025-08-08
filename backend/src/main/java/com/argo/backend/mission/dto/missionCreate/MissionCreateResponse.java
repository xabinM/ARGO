package com.argo.backend.mission.dto.missionCreate;

import com.argo.backend.mission.dto.common.ProblemResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MissionCreateResponse {

    private boolean success;
    private String message;
    private ProblemResponseDto problem;
}
