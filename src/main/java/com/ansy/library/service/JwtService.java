package com.ansy.library.service;

import com.ansy.library.config.AppProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final AppProperties properties;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(properties.getJwtSecret().getBytes());
    }

    public long getExpiration() {
        return properties.getJwtExpiration();
    }

    public String generateToken(UUID userId, String role, String sessionId, Instant issuedAt, Instant expiredAt) {
        return Jwts.builder()
                .claim("uid", userId.toString())
                .claim("role", role)
                .claim("sid", sessionId)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiredAt))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
