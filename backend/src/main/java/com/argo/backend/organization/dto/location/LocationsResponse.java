package com.argo.backend.organization.dto.location;

import com.argo.backend.domain.common.Coordinates;
import com.argo.backend.domain.location.entity.Location;

public class LocationsResponse {

    private Long locationId;
    private String name;
    private Coordinates coordinates;

    private LocationsResponse(Long locationId, String name, Coordinates coordinates) {
        this.locationId = locationId;
        this.name = name;
        this.coordinates = coordinates;
    }

    // 내부 팩토리 메서드
    public static LocationsResponse from(Location location) {
        return new LocationsResponse(
                location.getLocationId(),
                location.getName(),
                location.getCoordinates()
        );
    }
}
