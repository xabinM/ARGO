package com.argo.backend.mission.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MissionResponse {
    private boolean success;
    private String message;
}
