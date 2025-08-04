package com.argo.backend.mission.dto.missionCreate;

import com.argo.backend.domain.ploblem.Problem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MissionCreateDto {

    private Problem problem;
}
