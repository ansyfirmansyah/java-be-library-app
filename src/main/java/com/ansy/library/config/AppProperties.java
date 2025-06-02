package com.ansy.library.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private String verificationUrl;
    private String forgotPasswordUrl;
    private String frontendBaseUrl;
    private String mailFrom;
    private String jwtSecret;
    private Long jwtExpiration;

    @PostConstruct
    public void validate() {
        if (verificationUrl == null || verificationUrl.isBlank()) {
            throw new IllegalStateException("Missing required property: app.verification-url");
        }
        if (forgotPasswordUrl == null || forgotPasswordUrl.isBlank()) {
            throw new IllegalStateException("Missing required property: app.forgot-password-url");
        }
        if (mailFrom == null || mailFrom.isBlank()) {
            throw new IllegalStateException("Missing required property: app.mail-from");
        }
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("Missing required property: app.jwt-secret");
        }
        if (jwtExpiration == null) {
            throw new IllegalStateException("Missing required property: app.jwt-expiration");
        }
    }
}
