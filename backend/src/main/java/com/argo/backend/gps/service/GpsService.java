package com.argo.backend.gps.service;

import com.argo.backend.domain.classroom.enums.ApplicationStatus;
import com.argo.backend.domain.classroom.repository.ClassApplicationRepository;
import com.argo.backend.gps.dto.GpsWebSocketCommand;
import com.argo.backend.gps.dto.UserCoordinatesRequest;
import com.argo.backend.gps.dto.UserCoordinatesDto;
import com.argo.backend.redis.logic.GpsRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GpsService {

    private final GpsRedis gpsRedis;
    private final ClassApplicationRepository classApplicationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public void saveUserCoordinates(Long userId, UserCoordinatesRequest request, Long classId) {
        if (request.getLatitude() == null || request.getLongitude() == null) {
            return;
        }

        boolean isMember = classApplicationRepository.existsByUser_UserIdAndClassRoom_ClassIdAndStatus(
                userId, classId, ApplicationStatus.APPROVED);

        if (isMember) {
            if (gpsRedis.isTrackingActive(classId)) {
                gpsRedis.saveUserCoordinates(userId, request, classId);
            }
        }
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

    public void startTracking(Long classId) {
        if (gpsRedis.isTrackingActive(classId)) {
            return;
        }
        gpsRedis.setTrackingActive(classId, true);

        String destination = "/topic/class/" + classId + "/command";
        messagingTemplate.convertAndSend(destination, new GpsWebSocketCommand("START", classId));
    }

    public void stopTracking(Long classId) {
        if (!gpsRedis.isTrackingActive(classId)) {
            return;
        }
        gpsRedis.setTrackingActive(classId, false);

        String destination = "/topic/class/" + classId + "/command";
        messagingTemplate.convertAndSend(destination, new GpsWebSocketCommand("STOP", classId));
    }

    public boolean getTrackingStatus(Long classId) {
        return gpsRedis.isTrackingActive(classId);
    }
}
