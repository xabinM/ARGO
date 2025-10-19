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

    // Sorted Set (Geo)을 사용하여 유저 좌표 저장
    public void saveUserCoordinates(Long userId, UserCoordinatesRequest userCoordinatesRequest) {
        List<Long> classIds = getUserClassIds(userId);
        if (classIds == null || classIds.isEmpty()) {
            return;
        }

        Point point = new Point(userCoordinatesRequest.getLongitude().doubleValue(), userCoordinatesRequest.getLatitude().doubleValue());

        for (Long classId : classIds) {
            String key = redisKeyFactory.getClassGeoKey(classId); // 새로운 키 팩토리 메소드
            redisTemplate.opsForGeo().add(key, point, userId.toString());
            redisTemplate.expire(key, USER_COORDINATES_TTL);
        }
    }

    public void setUserClassIds(Long userId, List<Long> classIds) {
        String key = redisKeyFactory.getUserClassIdsKey(userId);
        redisTemplate.delete(key);
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

    // Sorted Set (Geo)을 사용하여 한 번의 쿼리로 모든 유저의 좌표를 가져옴
    public Map<Long, Map<String, String>> getUserCoordinatesByClassId(Long classId) {
        String key = redisKeyFactory.getClassGeoKey(classId);

        // GEORADIUS를 사용하여 모든 멤버를 가져옵니다. (중심점과 매우 큰 반경 사용)
        // 이 방식은 모든 멤버와 좌표를 한 번에 가져오는 효과적인 방법입니다.
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
