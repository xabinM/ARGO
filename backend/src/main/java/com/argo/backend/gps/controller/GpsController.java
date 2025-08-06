package com.argo.backend.gps.controller;

import com.argo.backend.gps.dto.UserCoordinatesRequest;
import com.argo.backend.gps.dto.UserCoordinatesResponse;
import com.argo.backend.gps.service.GpsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gps")
public class GpsController {

    private final GpsService gpsService;

    @PostMapping()
    public ResponseEntity<Void> updateLocation(@AuthenticationPrincipal Long userId,
                                               @RequestBody UserCoordinatesRequest coordinates) {
        gpsService.saveUserLocation(userId, coordinates);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/class/{classId}/students")
    public ResponseEntity<List<UserCoordinatesResponse>> getLocationsByClass(
            @PathVariable Long classId
    ) {
        List<UserCoordinatesResponse> locations = gpsService.getLocationsByClass(classId);
        return ResponseEntity.ok(locations);
    }
}
