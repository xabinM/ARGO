package com.argo.backend.redis.common;

import org.springframework.stereotype.Component;

@Component
public class RedisKeyFactory {

    private static final String BLACKLIST_PREFIX = "BL:";
    private static final String USER_COORDINATE_PREFIX = "coordinate:userId:";
    private static final String CLASS_USERID_LIST_PREFIX = "userIdList:classId:";
    private static final String USER_CLASSID_LIST_PREFIX = "classIdList:userId:";

    public String getBlacklistKey(String token) {
        return BLACKLIST_PREFIX + token;
    }

    public String getUserCoordinatesKey(Long userId) {
        return USER_COORDINATE_PREFIX + userId;
    }

    public String getClassUserCoordinatesKey(Long classId) {
        return CLASS_USERID_LIST_PREFIX + classId;
    }

    public String getUserClassIdsKey(Long userId) {
        return USER_CLASSID_LIST_PREFIX + userId;
    }
}
