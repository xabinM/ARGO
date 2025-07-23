package com.argo.backend.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final long refreshTokenExpirationMs;

    public RedisService(
            RedisTemplate<String, String> redisTemplate,
            @Value("${jwt.refreshTokenExpirationMs}") long refreshTokenExpirationMs
    ) {
        this.redisTemplate = redisTemplate;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
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

    public void deleteRefreshToken(String username) {
        redisTemplate.delete(username);
    }
}
