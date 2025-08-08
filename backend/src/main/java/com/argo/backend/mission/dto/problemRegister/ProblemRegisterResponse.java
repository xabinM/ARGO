package com.argo.backend.mission.dto.problemRegister;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProblemRegisterResponse {

    private boolean success;
    private String message;
}
