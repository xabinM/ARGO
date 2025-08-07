package com.argo.backend.redis.common;

import org.springframework.stereotype.Component;

@Component
public class RedisKeyFactory {

    private static final String BLACKLIST_PREFIX = "BL:";
    private static final String USER_LOCATION_PREFIX = "location:user:";
    private static final String CLASS_LOCATION_PREFIX = "location:class:";

    public String getBlacklistKey(String token) {
        return BLACKLIST_PREFIX + token;
    }

    public String getUserLocationKey(Long userId) {
        return USER_LOCATION_PREFIX + userId;
    }

    public String getClassLocationKey(Long classId) {
        return CLASS_LOCATION_PREFIX + classId;
    }
}
