package com.argo.backend.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final long refreshTokenExpirationMs;
    private final long blacklistExpirationMs;

    public RedisService(
            RedisTemplate<String, String> redisTemplate,
            @Value("${jwt.refreshTokenExpirationMs}") long refreshTokenExpirationMs,
            @Value("${jwt.accessTokenExpirationMs}") long accessTokenExpirationMs
    ) {
        this.redisTemplate = redisTemplate;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.blacklistExpirationMs = accessTokenExpirationMs;
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

    public void addToBlacklist(String token) {
        redisTemplate.opsForValue().set("BL:" + token, "logout", blacklistExpirationMs, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey("BL:" + token);
    }

    public void deleteRefreshToken(String username) {
        redisTemplate.delete(username);
    }
}
