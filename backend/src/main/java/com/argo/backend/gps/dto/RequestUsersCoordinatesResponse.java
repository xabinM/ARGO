package com.argo.backend.gps.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RequestUsersCoordinatesResponse {

    private boolean success;
    private List<UserCoordinatesDto> coordinates;
    private String message;
}
