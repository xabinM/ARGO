package com.argo.backend.gps.controller;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.gps.dto.RequestUsersCoordinatesResponse;
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
                                            @RequestBody UserCoordinatesRequest coordinates) {
        gpsService.saveUserCoordinates(userId, coordinates);
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
}
