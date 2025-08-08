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

    public void saveUserLocation(Long userId, UserCoordinatesRequest userCoordinatesRequest) {

        // 1. Redis에 위치 정보 저장
        gpsRedis.saveUserLocation(userId, userCoordinatesRequest);

        // 2. 유저가 속한 모든 classId 조회
        List<Long> classIds = classApplicationRepository
                .findAllByUser_UserIdAndStatus(userId, ApplicationStatus.PENDING)
                .stream()
                .map(app -> app.getClassRoom().getClassId())
                .toList();

        // 3. 각 classId에 유저 ID 추가
        for (Long classId : classIds) {
            gpsRedis.addUserToClass(classId, userId);
        }
    }

    public List<UserCoordinatesDto> getLocationsByClass(Long classId) {
        Map<Long, Map<String, String>> userLocations = gpsRedis.getUserLocationsByClassId(classId);

        return userLocations.entrySet().stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    Map<String, String> coordinates = entry.getValue();
                    return new UserCoordinatesDto(
                            userId,
                            new BigDecimal(coordinates.get("lat")),
                            new BigDecimal(coordinates.get("lng")),
                            Long.parseLong(coordinates.get("timestamp"))
                    );
                })
                .toList();
    }

}
