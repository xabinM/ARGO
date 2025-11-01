package com.argo.backend.redis.logic;

import com.argo.backend.gps.dto.UserCoordinatesRequest;
import com.argo.backend.redis.common.RedisKeyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Distance;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Repository
@RequiredArgsConstructor
public class GpsRedis {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisKeyFactory redisKeyFactory;

    private static final Duration USER_COORDINATES_TTL = Duration.ofSeconds(1800);
    private static final Duration USER_CLASS_IDS_TTL = Duration.ofHours(1);

    public void saveUserCoordinates(Long userId, UserCoordinatesRequest userCoordinatesRequest, List<Long> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return;
        }

        Point point = new Point(userCoordinatesRequest.getLongitude().doubleValue(), userCoordinatesRequest.getLatitude().doubleValue());

        for (Long classId : classIds) {
            String key = redisKeyFactory.getClassGeoKey(classId);
            redisTemplate.opsForGeo().add(key, point, userId.toString());
            redisTemplate.expire(key, USER_COORDINATES_TTL);
        }
    }

    public void setUserClassIds(Long userId, List<Long> classIds) {
        String key = redisKeyFactory.getUserClassIdsKey(userId);
        if (classIds != null && !classIds.isEmpty()) {
            for (Long classId : classIds) {
                redisTemplate.opsForSet().add(key, classId);
            }
            redisTemplate.expire(key, USER_CLASS_IDS_TTL);
        }
    }

    public List<Long> getUserClassIds(Long userId) {
        String key = redisKeyFactory.getUserClassIdsKey(userId);
        Set<Object> members = redisTemplate.opsForSet().members(key);
        if (members == null || members.isEmpty()) {
            return null;
        }
        return members.stream()
                .map(obj -> Long.valueOf(obj.toString()))
                .toList();
    }

    public void invalidateClassIdsPerUserId(Long userId) {
        String key = redisKeyFactory.getUserClassIdsKey(userId);
        redisTemplate.delete(key);
    }


    public Map<Long, Map<String, String>> getUserCoordinatesByClassId(Long classId) {
        String key = redisKeyFactory.getClassGeoKey(classId);

        GeoResults<RedisGeoCommands.GeoLocation<Object>> results = redisTemplate.opsForGeo()
                .radius(key, new Circle(new Point(0, 0), new Distance(Double.MAX_VALUE)));

        if (results == null) {
            return Map.of();
        }

        Map<Long, Map<String, String>> result = new HashMap<>();
        results.getContent().forEach(geoResult -> {
            RedisGeoCommands.GeoLocation<Object> location = geoResult.getContent();
            Long userId = Long.valueOf(location.getName().toString());
            Point point = location.getPoint();

            Map<String, String> coordinatesMap = new HashMap<>();
            coordinatesMap.put("lat", String.valueOf(point.getY()));
            coordinatesMap.put("lng", String.valueOf(point.getX()));
            coordinatesMap.put("timestamp", String.valueOf(System.currentTimeMillis())); // 실시간 조회이므로 현재시간

            result.put(userId, coordinatesMap);
        });

        return result;
    }
}
