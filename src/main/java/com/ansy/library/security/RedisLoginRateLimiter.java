package com.ansy.library.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisLoginRateLimiter {

    private final StringRedisTemplate redis;

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    public boolean isBlocked(String email, String ip) {
        String key = getKey(email, ip);
        String attempts = redis.opsForValue().get(key);
        return attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS;
    }

    public void recordFailure(String email, String ip) {
        String key = getKey(email, ip);
        Long current = redis.opsForValue().increment(key);
        if (current == 1) {
            redis.expire(key, BLOCK_DURATION);
        }
    }

    public void clear(String email, String ip) {
        redis.delete(getKey(email, ip));
    }

    private String getKey(String email, String ip) {
        return "RATE_LIMIT:LOGIN_FAIL:" + email + ":" + ip;
    }
}
