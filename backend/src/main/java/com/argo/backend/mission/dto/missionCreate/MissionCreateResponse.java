package com.argo.backend.mission.dto.missionCreate;

import com.argo.backend.domain.ploblem.entity.Problem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MissionCreateResponse {

    private String message;
    private Problem problem;
}
