package com.argo.backend.gps.service;

import com.argo.backend.domain.classroom.enums.ApplicationStatus;
import com.argo.backend.domain.classroom.repository.ClassApplicationRepository;
import com.argo.backend.gps.dto.UserCoordinates;
import com.argo.backend.redis.logic.GpsRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GpsService {

    private final GpsRedis gpsRedis;
    private final ClassApplicationRepository classApplicationRepository;

    public void saveUserLocation(Long userId, UserCoordinates userCoordinates) {

        // 1. Redis에 위치 정보 저장
        gpsRedis.saveUserLocation(userId, userCoordinates);

        // 2. 유저가 속한 모든 classId 조회
        List<Long> classIds = classApplicationRepository
                .findAllByUser_UserIdAndStatus(userId, ApplicationStatus.APPROVED)
                .stream()
                .map(app -> app.getClassRoom().getClassId())
                .toList();

        // 3. 각 classId에 유저 ID 추가
        for (Long classId : classIds) {
            gpsRedis.addUserToClass(classId, userId);
        }
    }
}
