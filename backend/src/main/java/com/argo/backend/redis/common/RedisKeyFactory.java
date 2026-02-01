package com.argo.backend.redis.common;

import org.springframework.stereotype.Component;

@Component
public class RedisKeyFactory {

    private static final String BLACKLIST_PREFIX = "BL:";
    private static final String USER_CLASSID_LIST_PREFIX = "classIdList:userId:";
    private static final String CLASS_GEO_PREFIX = "class-geo:";
    private static final String TRACKING_STATUS_PREFIX = "tracking:class:";

    public String getBlacklistKey(String token) {
        return BLACKLIST_PREFIX + token;
    }

    public String getUserClassIdsKey(Long userId) {
        return USER_CLASSID_LIST_PREFIX + userId;
    }

    public String getClassGeoKey(Long classId) {
        return CLASS_GEO_PREFIX + classId;
    }

    public String getTrackingStatusKey(Long classId) {
        return TRACKING_STATUS_PREFIX + classId;
    }
}
