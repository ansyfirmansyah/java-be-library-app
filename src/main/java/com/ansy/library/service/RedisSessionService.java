package com.ansy.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
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
}
