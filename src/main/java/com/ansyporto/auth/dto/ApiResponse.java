package com.ansyporto.auth.dto;

import lombok.Builder;
import lombok.Getter;
import org.slf4j.MDC;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Getter
@Builder
public class ApiResponse<T> {
    private final boolean success;
    private final int code;
    private final String message;
    private final T data;
    private final String responseId;
    private final String timestamp;
    private final String locale;

    public static <T> ApiResponse<T> success(String message, T data) {
        String id = UUID.randomUUID().toString();
        Locale currentLocale = LocaleContextHolder.getLocale();
        MDC.put("responseId", id);
        return ApiResponse.<T>builder()
                .success(true)
                .code(200)
                .message(message)
                .data(data)
                .responseId(id)
                .timestamp(Instant.now().toString())
                .locale(currentLocale.getLanguage())
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        String id = UUID.randomUUID().toString();
        Locale currentLocale = LocaleContextHolder.getLocale();
        MDC.put("responseId", id);
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .data(null)
                .responseId(id)
                .timestamp(Instant.now().toString())
                .locale(currentLocale.getLanguage())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(200, message);
    }
}

