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

        // 1. Redis에 위치 정보 저장
        gpsRedis.saveUserCoordinates(userId, request);

        // 2. 유저가 속한 모든 classId를 redis에서 조회, 만약 없다면 그 때, DB에서 조회 후 redis에 저장
        // todo 반 신청 status 값 현재 테스트 용으로 PENDING 이지만 APPROVED 수정 해야함
        List<Long> classIds = gpsRedis.getUserClassIds(userId);
        if (classIds == null) {
            classIds = classApplicationRepository
                    .findAllByUser_UserIdAndStatus(userId, ApplicationStatus.PENDING)
                    .stream()
                    .map(app -> app.getClassRoom().getClassId())
                    .toList();
            gpsRedis.setUserClassIds(userId, classIds);
        }

        // 3. 각 classId에 유저 ID 추가
        for (Long classId : classIds) {
            gpsRedis.addUserToClass(classId, userId);
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
                            new BigDecimal(coordinates.get("lng"))                    );
                })
                .toList();
    }

}
