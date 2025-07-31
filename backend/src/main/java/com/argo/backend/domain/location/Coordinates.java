package com.argo.backend.domain.location;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class Coordinates {
    private BigDecimal latitude;
    private BigDecimal longitude;
}
