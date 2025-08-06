package com.argo.backend.redis.logic;

import com.argo.backend.gps.dto.UserCoordinates;
import com.argo.backend.redis.common.RedisKeyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class GpsRedis {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisKeyFactory redisKeyFactory;

    private static final Duration LOCATION_TTL = Duration.ofSeconds(300);

    public void saveUserLocation(Long userId, UserCoordinates userCoordinates) {
        Map<String, String> coordinatesMap = new HashMap<>();
        coordinatesMap.put("lat", userCoordinates.getLatitude().toString());
        coordinatesMap.put("lng", userCoordinates.getLongitude().toString());
        coordinatesMap.put("timestamp", String.valueOf(System.currentTimeMillis()));

        String key = redisKeyFactory.getUserLocationKey(userId);
        redisTemplate.opsForValue().set(key, coordinatesMap, LOCATION_TTL);
    }

    public void addUserToClass(Long classId, Long userId) {
        String key = redisKeyFactory.getClassLocationKey(classId);
        redisTemplate.opsForSet().add(key, userId);
        redisTemplate.expire(key, LOCATION_TTL);
    }

    // 조회
}
