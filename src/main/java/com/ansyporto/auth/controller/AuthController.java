package com.ansyporto.auth.controller;

import com.ansyporto.auth.dto.ApiResponse;
import com.ansyporto.auth.dto.LoginRequest;
import com.ansyporto.auth.dto.LoginResponse;
import com.ansyporto.auth.dto.RegisterRequest;
import com.ansyporto.auth.service.AuthService;
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
}

