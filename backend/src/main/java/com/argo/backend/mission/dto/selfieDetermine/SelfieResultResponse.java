package com.argo.backend.mission.dto.selfieDetermine;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SelfieResultResponse {

    private boolean result;
    private String image;
}
