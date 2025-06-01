package com.ansy.library.service;

import com.ansy.library.dto.ApiResponse;
import com.ansy.library.dto.LoginRequest;
import com.ansy.library.dto.LoginResponse;
import com.ansy.library.dto.RegisterRequest;
import com.ansy.library.entity.Role;
import com.ansy.library.entity.User;
import com.ansy.library.entity.UserActivityAudit;
import com.ansy.library.entity.VerificationToken;
import com.ansy.library.exception.RateLimitException;
import com.ansy.library.exception.UnauthorizedException;
import com.ansy.library.repository.UserActivityAuditRepository;
import com.ansy.library.repository.UserRepository;
import com.ansy.library.repository.VerificationTokenRepository;
import com.ansy.library.security.RedisLoginRateLimiter;
import com.ansy.library.utils.EmailValidatorUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserActivityAuditRepository auditRepository;
    private final VerificationTokenRepository tokenRepository;
    private final MailService mailService;
    private final MessageSource messageSource;
    private final JwtService jwtService;
    private final RedisSessionService redisSessionService;
    private final RedisLoginRateLimiter rateLimiter;

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
}
