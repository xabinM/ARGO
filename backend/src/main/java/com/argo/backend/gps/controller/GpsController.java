package com.argo.backend.gps.controller;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.gps.dto.RequestUsersCoordinatesResponse;
import com.argo.backend.gps.dto.TrackingStatusResponse;
import com.argo.backend.gps.dto.UpdateCoordinatesResponse;
import com.argo.backend.gps.dto.UserCoordinatesRequest;
import com.argo.backend.gps.dto.UserCoordinatesDto;
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
    public ResponseEntity<?> updateLocation(@AuthenticationPrincipal Long userId,
                                            @RequestBody UserCoordinatesRequest coordinates,
                                            @RequestBody Long classId) {
        gpsService.saveUserCoordinates(userId, coordinates, classId);
        return ResponseEntity.ok(
                new UpdateCoordinatesResponse(true,
                        ResponseMessage.SUCCESS_USER_COORDINATES_POST.getMessage())
        );
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/class/{classId}/students")
    public ResponseEntity<?> getCoordinatesByClass(@PathVariable Long classId) {
        List<UserCoordinatesDto> coordinates = gpsService.getUserCoordinatesByClass(classId);
        return ResponseEntity.ok(new RequestUsersCoordinatesResponse(
                true,
                coordinates,
                ResponseMessage.SUCCESS_USERS_COORDINATES_RESPONSE.getMessage())
        );
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/class/{classId}/start-tracking")
    public ResponseEntity<?> startTracking(@PathVariable Long classId) {
        gpsService.startTracking(classId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/class/{classId}/stop-tracking")
    public ResponseEntity<?> stopTracking(@PathVariable Long classId) {
        gpsService.stopTracking(classId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/class/{classId}/tracking-status")
    public ResponseEntity<TrackingStatusResponse> getTrackingStatus(@PathVariable Long classId) {
        boolean isTracking = gpsService.getTrackingStatus(classId);
        return ResponseEntity.ok(new TrackingStatusResponse(isTracking));
    }
}
