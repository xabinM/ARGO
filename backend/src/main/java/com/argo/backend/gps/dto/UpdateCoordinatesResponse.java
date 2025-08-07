package com.argo.backend.gps.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateCoordinatesResponse {

    private boolean success;
    private String message;
}
