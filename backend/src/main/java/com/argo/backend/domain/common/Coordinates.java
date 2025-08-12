package com.argo.backend.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Embeddable
public class Coordinates {

    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;

    // DataLoader용 정적 팩토리 메서드
    public static Coordinates create(BigDecimal latitude, BigDecimal longitude) {
        Coordinates coordinates = new Coordinates();
        coordinates.latitude = latitude;
        coordinates.longitude = longitude;
        return coordinates;
    }
}
