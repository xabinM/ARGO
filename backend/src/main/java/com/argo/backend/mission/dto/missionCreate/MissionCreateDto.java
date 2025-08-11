package com.argo.backend.mission.dto.missionCreate;

import com.argo.backend.mission.dto.common.ProblemDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MissionCreateDto {

    private Long missionId;
    private ProblemDetail problemDetail;
}
