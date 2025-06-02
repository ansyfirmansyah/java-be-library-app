package com.ansy.library.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@DataRedisTest
class RedisLoginRateLimiterTest {

    @Autowired
    private StringRedisTemplate redis;

    private RedisRateLimiter limiter;

    @BeforeEach
    void setup() {
        limiter = new RedisRateLimiter(redis);
    }

    @Test
    void shouldLimitAfterMaxAttempts() {
        String email = "user@example.com";
        String ip = "192.168.0.1";

        assertThat(limiter.isBlocked(email, ip)).isFalse();

        for (int i = 0; i < 5; i++) {
            limiter.recordFailure(email, ip);
        }

        limiter.recordFailure(email, ip);
        assertThat(limiter.isBlocked(email, ip)).isTrue();

        limiter.clear(email, ip);
        assertThat(limiter.isBlocked(email, ip)).isFalse();
    }
}
