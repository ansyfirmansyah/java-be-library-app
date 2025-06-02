package com.ansy.library.service;

import com.ansy.library.dto.*;
import com.ansy.library.entity.*;
import com.ansy.library.exception.RateLimitException;
import com.ansy.library.exception.UnauthorizedException;
import com.ansy.library.repository.PasswordResetTokenRepository;
import com.ansy.library.repository.UserActivityAuditRepository;
import com.ansy.library.repository.UserRepository;
import com.ansy.library.repository.VerificationTokenRepository;
import com.ansy.library.security.RedisRateLimiter;
import com.ansy.library.utils.EmailValidatorUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserActivityAuditRepository auditRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;
    private final MessageSource messageSource;
    private final JwtService jwtService;
    private final RedisSessionService redisSessionService;
    private final RedisRateLimiter rateLimiter;

    public void register(RegisterRequest request, HttpServletRequest http) {
        String email = request.getEmail();
        String ip = http.getRemoteAddr();
        boolean success = false;
        UUID userId = null;

        try {
            Instant limit = Instant.now().minus(60, ChronoUnit.MINUTES);
            if (auditRepository.countByIpAddressAndActivityTypeAndActivityTimeAfter(ip, "REGISTER", limit) > 10) {
                String message = messageSource.getMessage("registration.rateLimit", null, LocaleContextHolder.getLocale());
                throw new RateLimitException(message);
            }

            if (!EmailValidatorUtil.isEmailDomainValid(email)) {
                String message = messageSource.getMessage("registration.invalidDomain", null, LocaleContextHolder.getLocale());
                throw new IllegalArgumentException(message);
            }

            if (userRepository.existsByEmail(email)) {
                String message = messageSource.getMessage("registration.duplicateEmail", null, LocaleContextHolder.getLocale());
                throw new IllegalStateException(message);
            }

            String salt = BCrypt.gensalt();
            String hashedPassword = BCrypt.hashpw(request.getPassword(), salt);

            User user = User.builder()
                    .email(email)
                    .password(hashedPassword)
                    .role(Role.USER)
                    .emailVerified(false)
                    .build();

            user = userRepository.save(user);
            userId = user.getId();
            success = true;

            // Send email verification
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = VerificationToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                    .used(false)
                    .build();

            tokenRepository.save(verificationToken);

            mailService.sendVerificationEmail(user.getEmail(), token);
        } finally {
            auditRepository.save(UserActivityAudit.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .email(email)
                    .activityType("REGISTER")
                    .success(success)
                    .ipAddress(http.getRemoteAddr())
                    .userAgent(http.getHeader("User-Agent"))
                    .build());
        }
    }

    public boolean verifyEmail(String token) {
        Optional<VerificationToken> opt = tokenRepository.findByToken(token);
        if (opt.isEmpty()) return false;

        VerificationToken v = opt.get();
        if (v.isUsed() || v.getExpiresAt().isBefore(Instant.now())) return false;

        v.setUsed(true);
        v.getUser().setEmailVerified(true);

        tokenRepository.save(v);
        userRepository.save(v.getUser());
        return true;
    }

    public ApiResponse<LoginResponse> login(LoginRequest request, HttpServletRequest http) {
        String email = request.getEmail();
        String ip = http.getRemoteAddr();
        String ua = http.getHeader("User-Agent");
        UUID userId = null;
        boolean success = false;

        if (rateLimiter.isBlocked(email, ip)) {
            String message = messageSource.getMessage("login.rateLimit", null, LocaleContextHolder.getLocale());
            throw new RateLimitException(message);
        }

        try {
            User user = (User) userRepository.findByEmailIgnoreCase(request.getEmail())
                    .orElseThrow(() -> new UnauthorizedException("login.invalidCredentials"));

            if (!user.isEmailVerified()) {
                String message = messageSource.getMessage("login.unverifiedEmail", null, LocaleContextHolder.getLocale());
                throw new UnauthorizedException(message);
            }

            if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
                String message = messageSource.getMessage("login.invalidCredentials", null, LocaleContextHolder.getLocale());
                throw new UnauthorizedException(message);
            }

            String sessionId = UUID.randomUUID().toString();
            Instant issuedAt = Instant.now();
            Instant expiredAt = issuedAt.plusSeconds(jwtService.getExpiration());
            userId = user.getId();

            String token = jwtService.generateToken(user.getId(), String.valueOf(user.getRole()), sessionId, issuedAt, expiredAt);
            redisSessionService.storeSession(user.getId(), sessionId, expiredAt);

            String message = messageSource.getMessage("login.success", null, LocaleContextHolder.getLocale());
            return ApiResponse.success(message,
                    LoginResponse.builder()
                            .token(token)
                            .expiredAt(expiredAt)
                            .build()
            );
        } catch (UnauthorizedException e) {
            rateLimiter.recordFailure(email, ip);
            throw e;
        } finally {
            auditRepository.save(UserActivityAudit.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .email(email)
                    .activityType("LOGIN")
                    .success(success)
                    .ipAddress(ip)
                    .userAgent(ua)
                    .build());
        }
    }

    public void logout(String userId, String sessionId) {
        redisSessionService.invalidateSession(UUID.fromString(userId), sessionId);

        auditRepository.save(UserActivityAudit.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(userId))
                .email(null) // opsional, bisa ambil dari user repo
                .activityType("LOGOUT")
                .success(true)
                .ipAddress(null) // bisa diisi dari request kalau mau
                .userAgent(null)
                .build()
        );
    }

    public void forgotPassword(ForgotPasswordRequest request, HttpServletRequest http) {
        String email = request.getEmail();
        boolean success = false;
        UUID userId = null;

        try {
            // Rate Limit untuk forgot password
            String emailKey = "RATE_LIMIT:FORGOT_PASSWORD:" + request.getEmail().toLowerCase();
            boolean acquireRateLimit = rateLimiter.acquireRateLimit(emailKey, 60, 5);
            if (!acquireRateLimit) {
                log.warn("🚫 Rate limit hit for forgot-password: {}", request.getEmail());
                return; // Silent for security
            }

            Optional<Object> userOpt = userRepository.findByEmailIgnoreCase(request.getEmail());
            if (userOpt.isEmpty()) {
                log.warn("🔒 Forgot password: Email not found -> {}", request.getEmail());
                return; // Silent for security
            }
            User user = (User) userOpt.get();
            userId = user.getId();

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .used(false)
                    .build();

            passwordResetTokenRepository.save(resetToken);
            mailService.sendForgotPasswordEmail(user.getEmail(), token);
            success = true;
        } finally {
            auditRepository.save(UserActivityAudit.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .email(email)
                    .activityType("FORGOT PASSWORD")
                    .success(success)
                    .ipAddress(http.getRemoteAddr())
                    .userAgent(http.getHeader("User-Agent"))
                    .build());
        }
    }

    public void resetPassword(ResetPasswordRequest request, HttpServletRequest http) {
        String email = null;
        boolean success = false;
        UUID userId = null;
        try{
            Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(request.getToken());
            if (tokenOpt.isEmpty()) {
                String message = messageSource.getMessage("resetPassword.token.notFound", null, LocaleContextHolder.getLocale());
                throw new IllegalArgumentException(message);
            }

            PasswordResetToken resetToken = tokenOpt.get();
            if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
                String message = messageSource.getMessage("resetPassword.token.invalid", null, LocaleContextHolder.getLocale());
                throw new IllegalArgumentException(message);
            }

            User user = resetToken.getUser();
            email = user.getEmail();
            userId = user.getId();
            String salt = BCrypt.gensalt();
            String hashedPassword = BCrypt.hashpw(request.getNewPassword(), salt);
            user.setPassword(hashedPassword);
            userRepository.save(user);

            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);
            // Hapus semua session atas user terkait
            redisSessionService.invalidateAllSessionUser(user.getId());

            log.info("🔑 Password berhasil direset untuk user {}", user.getEmail());
        } finally {
            auditRepository.save(UserActivityAudit.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .email(email)
                    .activityType("RESET PASSWORD")
                    .success(success)
                    .ipAddress(http.getRemoteAddr())
                    .userAgent(http.getHeader("User-Agent"))
                    .build());
        }
    }
}
