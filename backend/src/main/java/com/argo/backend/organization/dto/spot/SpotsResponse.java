package com.argo.backend.organization.dto.spot;

import com.argo.backend.domain.common.Coordinates;
import com.argo.backend.domain.spot.entity.Spot;
import lombok.Getter;

@Getter
public class SpotsResponse {

    private Long spotId;
    private String name;
    private String description;
    private Coordinates coordinates;

    private SpotsResponse(Long spotId, String name, String description, Coordinates coordinates) {
        this.spotId = spotId;
        this.name = name;
        this.description = description;
        this.coordinates = coordinates;
    }

    // 내부 팩토리 메서드
    public static SpotsResponse from(Spot spot) {
        return new SpotsResponse(
                spot.getId(),
                spot.getName(),
                spot.getDescription(),
                spot.getCoordinates()
        );
    }
}
