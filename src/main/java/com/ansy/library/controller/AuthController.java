package com.ansy.library.controller;

import com.ansy.library.dto.*;
import com.ansy.library.service.AuthService;
import com.ansy.library.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MessageSource messageSource;
    private final JwtService jwtService;

    @PostMapping("/register")
    @Tag(name="Auth")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http) {
        authService.register(request, http);
        String message = messageSource.getMessage("registration.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @GetMapping("/verify")
    @Tag(name="Auth")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {
        boolean result = authService.verifyEmail(token);
        String key = result ? "verification.success" : "verification.invalid";
        String message = messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(result ? ApiResponse.success(message) : ApiResponse.error(message));
    }

    @PostMapping("/login")
    @Tag(name="Auth")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return authService.login(request, http);
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Tag(name="Auth")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        Claims claims = jwtService.parseToken(token);

        String sessionId = claims.get("sid", String.class);
        String userId = claims.get("uid", String.class);

        authService.logout(userId, sessionId);
        String message = messageSource.getMessage("logout.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping("/forgot-password")
    @Tag(name="Auth")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request, HttpServletRequest http) {
        authService.forgotPassword(request, http);
        String message = messageSource.getMessage("forgotPassword.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping("/reset-password")
    @Tag(name="Auth")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody @Valid ResetPasswordRequest request, HttpServletRequest http) {
        authService.resetPassword(request, http);
        String message = messageSource.getMessage("resetPassword.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(message));
    }
}

