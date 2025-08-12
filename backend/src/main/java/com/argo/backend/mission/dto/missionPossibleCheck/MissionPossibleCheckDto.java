package com.argo.backend.mission.dto.missionPossibleCheck;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MissionPossibleCheckDto {

    private boolean isSuccess;
    private String message;
}
