package com.argo.backend.gps.controller;

import com.argo.backend.gps.dto.UserCoordinates;
import com.argo.backend.gps.service.GpsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gps")
public class GpsController {

    private final GpsService gpsService;

    @PostMapping("/{userId}")
    public ResponseEntity<Void> updateLocation(@PathVariable Long userId,
                                               @RequestBody UserCoordinates coordinates) {
        gpsService.saveUserLocation(userId, coordinates);
        return ResponseEntity.ok().build();
    }

//    @GetMapping("/class/{classId}/students")
//    public ResponseEntity<List<LocationResponse>> getLocationsByClass(
//            @PathVariable Long classId
//    ) {
//        List<LocationResponse> locations = gpsService.getLocationsByClass(classId);
//        return ResponseEntity.ok(locations);
//    }
}
