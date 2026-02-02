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
import java.util.Map;


@Repository
@RequiredArgsConstructor
public class GpsRedis {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisKeyFactory redisKeyFactory;

    private static final Duration USER_COORDINATES_TTL = Duration.ofSeconds(1800);
    private static final Duration TRACKING_STATUS_TTL = Duration.ofHours(2); // 2시간 후 자동 만료

    public void saveUserCoordinates(Long userId, UserCoordinatesRequest userCoordinatesRequest, Long classId) {
        if (classId == null) {
            return;
        }

        Point point = new Point(userCoordinatesRequest.getLongitude().doubleValue(), userCoordinatesRequest.getLatitude().doubleValue());
        String key = redisKeyFactory.getClassGeoKey(classId);
        
        redisTemplate.opsForGeo().add(key, point, userId.toString());
        redisTemplate.expire(key, USER_COORDINATES_TTL);
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

    public boolean isTrackingActive(Long classId) {
        String key = redisKeyFactory.getTrackingStatusKey(classId);
        Object status = redisTemplate.opsForValue().get(key);
        return status != null && Boolean.parseBoolean(status.toString());
    }

    public void setTrackingActive(Long classId, boolean isActive) {
        String key = redisKeyFactory.getTrackingStatusKey(classId);
        if (isActive) {
            redisTemplate.opsForValue().set(key, "true", TRACKING_STATUS_TTL);
        } else {
            redisTemplate.delete(key);
        }
    }
}
