package com.argo.backend.redis.logic;

import com.argo.backend.redis.common.RedisKeyFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AuthRedis {

    private final RedisTemplate<String, String> redisTemplate;
    private final long refreshTokenExpirationMs;
    private final RedisKeyFactory redisKeyFactory;

    public AuthRedis(
            @Qualifier("authRedisTemplate") RedisTemplate<String, String> redisTemplate,
            @Value("${jwt.refreshTokenExpirationMs}") long refreshTokenExpirationMs,
            RedisKeyFactory redisKeyFactory
    ) {
        this.redisTemplate = redisTemplate;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.redisKeyFactory = redisKeyFactory;
    }

    public void saveRefreshToken(String username, String refreshToken) {
        redisTemplate.opsForValue().set(username, refreshToken, refreshTokenExpirationMs, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get(username);
    }

    public void reissueRefreshToken(String username, String newToken) {
        redisTemplate.opsForValue().set(username, newToken, refreshTokenExpirationMs, TimeUnit.MILLISECONDS);
    }

    public void addToBlacklist(String token, String value) {
        String key = redisKeyFactory.getBlacklistKey(token);
        redisTemplate.opsForValue().set(key, value, refreshTokenExpirationMs, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String token) {
        String key = redisKeyFactory.getBlacklistKey(token);
        return redisTemplate.hasKey(key);
    }

    public void deleteRefreshToken(String username) {
        redisTemplate.delete(username);
    }
}
