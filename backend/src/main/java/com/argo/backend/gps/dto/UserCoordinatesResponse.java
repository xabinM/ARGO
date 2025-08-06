package com.argo.backend.gps.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class UserCoordinatesResponse {

    private Long userId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Long timestamp;
}
