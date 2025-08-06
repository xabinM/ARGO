package com.argo.backend.redis.logic;

import com.argo.backend.gps.dto.UserCoordinatesRequest;
import com.argo.backend.redis.common.RedisKeyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GpsRedis {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisKeyFactory redisKeyFactory;

    private static final Duration LOCATION_TTL = Duration.ofSeconds(300);

    public void saveUserLocation(Long userId, UserCoordinatesRequest userCoordinatesRequest) {
        Map<String, String> coordinatesMap = new HashMap<>();
        coordinatesMap.put("lat", userCoordinatesRequest.getLatitude().toString());
        coordinatesMap.put("lng", userCoordinatesRequest.getLongitude().toString());
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
    public Map<Long, Map<String, String>> getUserLocationsByClassId(Long classId) {
        String classKey = redisKeyFactory.getClassLocationKey(classId);
        Set<Object> userIds = redisTemplate.opsForSet().members(classKey);

        if (userIds == null) {
            return Map.of();
        }

        Map<Long, Map<String, String>> result = new HashMap<>();
        for (Object userIdObj : userIds) {
            Long userId = Long.valueOf(userIdObj.toString());
            String userKey = redisKeyFactory.getUserLocationKey(userId);
            Object locationObj = redisTemplate.opsForValue().get(userKey);

            if (locationObj instanceof Map<?, ?> locationMap) {

                // 타입 캐스팅 처리
                Map<String, String> castedMap = locationMap.entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey().toString(),
                                e -> e.getValue().toString()
                        ));

                result.put(userId, castedMap);
            }
        }

        return result;
    }

}
