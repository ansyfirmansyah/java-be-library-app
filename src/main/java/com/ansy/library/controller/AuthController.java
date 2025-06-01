package com.ansy.library.controller;

import com.ansy.library.dto.ApiResponse;
import com.ansy.library.dto.LoginRequest;
import com.ansy.library.dto.LoginResponse;
import com.ansy.library.dto.RegisterRequest;
import com.ansy.library.service.AuthService;
import com.ansy.library.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MessageSource messageSource;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http) {
        authService.register(request, http);
        String message = messageSource.getMessage("registration.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {
        boolean result = authService.verifyEmail(token);
        String key = result ? "verification.success" : "verification.invalid";
        String message = messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(result ? ApiResponse.success(message) : ApiResponse.error(message));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return authService.login(request, http);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Token tidak ditemukan"));
        }

        String token = authHeader.substring(7);
        Claims claims = jwtService.parseToken(token);

        String sessionId = claims.get("sid", String.class);
        String userId = claims.get("uid", String.class);

        authService.logout(userId, sessionId);
        String message = messageSource.getMessage("logout.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(message));
    }
}

