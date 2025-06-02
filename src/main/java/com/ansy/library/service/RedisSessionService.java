package com.ansy.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisSessionService {

    private final StringRedisTemplate redisTemplate;

    public void storeSession(UUID userId, String sessionId, Instant expiredAt) {
        String key = "SESSION:" + userId + ":" + sessionId;
        Duration ttl = Duration.between(Instant.now(), expiredAt);
        redisTemplate.opsForValue().set(key, "active", ttl);
    }

    public void invalidateSession(UUID userId, String sessionId) {
        String key = "SESSION:" + userId + ":" + sessionId;
        redisTemplate.delete(key);
    }

    public void invalidateAllSessionUser(UUID userId) {
        // Hapus semua sesi aktif user dari Redis
        String sessionPattern = "SESSION:" + userId + ":*";
        Set<String> keys = redisTemplate.keys(sessionPattern);
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public boolean sessionExists(UUID userId, String sessionId) {
        String key = "SESSION:" + userId + ":" + sessionId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
