package com.ansy.library.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public class JwtUtil {
    public static String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof Jwt) {
            return ((Jwt) auth.getPrincipal()).getSubject();
        }
        if (auth.getPrincipal() instanceof String) {
            return (String) auth.getPrincipal();
        }
        throw new IllegalStateException("JWT token is missing or invalid");
    }
}
