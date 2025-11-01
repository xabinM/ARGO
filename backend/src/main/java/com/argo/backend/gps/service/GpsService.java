package com.argo.backend.gps.service;

import com.argo.backend.domain.classroom.enums.ApplicationStatus;
import com.argo.backend.domain.classroom.repository.ClassApplicationRepository;
import com.argo.backend.gps.dto.UserCoordinatesRequest;
import com.argo.backend.gps.dto.UserCoordinatesDto;
import com.argo.backend.redis.logic.GpsRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GpsService {

    private final GpsRedis gpsRedis;
    private final ClassApplicationRepository classApplicationRepository;

    public void saveUserCoordinates(Long userId, UserCoordinatesRequest request) {

        if (request.getLatitude() == null || request.getLongitude() == null) {
            return;
        }

        List<Long> classIds = gpsRedis.getUserClassIds(userId);
        if (classIds == null) {
            classIds = classApplicationRepository
                    .findAllByUser_UserIdAndStatus(userId, ApplicationStatus.APPROVED)
                    .stream()
                    .map(app -> app.getClassRoom().getClassId())
                    .toList();
            gpsRedis.setUserClassIds(userId, classIds);
        }

        gpsRedis.saveUserCoordinates(userId, request, classIds);
    }

    public List<UserCoordinatesDto> getUserCoordinatesByClass(Long classId) {
        Map<Long, Map<String, String>> userCoordinates = gpsRedis.getUserCoordinatesByClassId(classId);

        return userCoordinates.entrySet().stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    Map<String, String> coordinates = entry.getValue();
                    return new UserCoordinatesDto(
                            userId,
                            new BigDecimal(coordinates.get("lat")),
                            new BigDecimal(coordinates.get("lng"))
                    );
                })
                .toList();
    }

}
