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

        // 1. 유저가 속한 클래스 ID 목록을 먼저 Redis에 로드합니다.
        List<Long> classIds = gpsRedis.getUserClassIds(userId);
        if (classIds == null) {
            classIds = classApplicationRepository
                    .findAllByUser_UserIdAndStatus(userId, ApplicationStatus.APPROVED)
                    .stream()
                    .map(app -> app.getClassRoom().getClassId())
                    .toList();
            gpsRedis.setUserClassIds(userId, classIds);
        }

        // 2. 클래스 ID가 Redis에 저장된 후, 좌표를 저장합니다.
        // 이제 GpsRedis는 내부적으로 올바른 classIds를 사용하여 Geo 자료구조를 업데이트합니다.
        gpsRedis.saveUserCoordinates(userId, request);
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
