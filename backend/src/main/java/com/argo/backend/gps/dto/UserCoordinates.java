package com.argo.backend.gps.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class UserCoordinates {

    private BigDecimal latitude;
    private BigDecimal longitude;
}
