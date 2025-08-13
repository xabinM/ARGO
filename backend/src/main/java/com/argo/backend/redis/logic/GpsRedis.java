package com.argo.backend.redis.logic;

import com.argo.backend.gps.dto.UserCoordinatesRequest;
import com.argo.backend.redis.common.RedisKeyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GpsRedis {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisKeyFactory redisKeyFactory;

    // todo 유저 좌표 정보 현재는 테스트 때문에 300초이지만 이후 폴링 방식 시간에 따라 변경 해야함.
    private static final Duration USER_COORDINATES_TTL = Duration.ofSeconds(1800);
    private static final Duration USER_CLASS_IDS_TTL = Duration.ofHours(1);


    public void saveUserCoordinates(Long userId, UserCoordinatesRequest userCoordinatesRequest) {
        Map<String, String> coordinatesMap = new HashMap<>();
        coordinatesMap.put("lat", userCoordinatesRequest.getLatitude().toString());
        coordinatesMap.put("lng", userCoordinatesRequest.getLongitude().toString());
        coordinatesMap.put("timestamp", String.valueOf(System.currentTimeMillis()));

        String key = redisKeyFactory.getUserCoordinatesKey(userId);
        redisTemplate.opsForValue().set(key, coordinatesMap, USER_COORDINATES_TTL);
    }

    public void addUserToClass(Long classId, Long userId) {
        String key = redisKeyFactory.getClassUserCoordinatesKey(classId);
        redisTemplate.opsForSet().add(key, userId);
        redisTemplate.expire(key, USER_COORDINATES_TTL);
    }

    // 유저가 속한 클래스 목록에 클래스 추가 -> 이 방식은 기존 classIds와 추가된 classId 사이의 TTL 차이로 인해 특정 시점 문제 발생
    public void addClassToUser(Long userId, Long classId) {
        String key = redisKeyFactory.getUserClassIdsKey(userId);
        redisTemplate.opsForSet().add(key, classId);
        redisTemplate.expire(key, USER_CLASS_IDS_TTL);
    }

    // 3. 유저가 속한 class의 Id 저장
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

    // 4. 유저가 속한 class의 Id 목록 가져오기
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

    // 조회
    public Map<Long, Map<String, String>> getUserCoordinatesByClassId(Long classId) {
        String classKey = redisKeyFactory.getClassUserCoordinatesKey(classId);
        Set<Object> userIds = redisTemplate.opsForSet().members(classKey);

        if (userIds == null) {
            return Map.of();
        }

        Map<Long, Map<String, String>> result = new HashMap<>();
        for (Object userIdObj : userIds) {
            Long userId = Long.valueOf(userIdObj.toString());
            String userKey = redisKeyFactory.getUserCoordinatesKey(userId);
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
