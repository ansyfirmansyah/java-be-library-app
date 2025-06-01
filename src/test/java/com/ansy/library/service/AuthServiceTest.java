package com.ansy.library.service;

import com.ansy.library.dto.RegisterRequest;
import com.ansy.library.entity.Role;
import com.ansy.library.entity.User;
import com.ansy.library.entity.VerificationToken;
import com.ansy.library.exception.RateLimitException;
import com.ansy.library.repository.UserRepository;
import com.ansy.library.repository.UserActivityAuditRepository;
import com.ansy.library.repository.VerificationTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserActivityAuditRepository auditRepository;

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private RedisSessionService redisSessionService;

    @Mock
    private MailService mailService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerSuccess() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@gmail.com");
        registerRequest.setPassword("Password1!");

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(false);
        when(auditRepository.countByIpAddressAndActivityTypeAndActivityTimeAfter(any(), any(), any())).thenReturn(0L);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("JUnit");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@gmail.com")
                .password("hashed")
                .role(Role.USER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        assertDoesNotThrow(() -> authService.register(registerRequest, request));

        verify(userRepository).save(any(User.class));
        verify(auditRepository).save(any());
    }

    @Test
    void registerFail_EmailAlreadyExists() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@gmail.com");
        registerRequest.setPassword("Password1!");

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(true);
        when(auditRepository.countByIpAddressAndActivityTypeAndActivityTimeAfter(any(), any(), any())).thenReturn(0L);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("JUnit");

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                authService.register(registerRequest, request)
        );

        verify(auditRepository).save(any());
    }

    @Test
    void registerFail_RateLimitExceeded() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@gmail.com");
        registerRequest.setPassword("Password1!");

        when(auditRepository.countByIpAddressAndActivityTypeAndActivityTimeAfter(any(), any(), any())).thenReturn(99L);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        RateLimitException thrown = assertThrows(RateLimitException.class, () ->
                authService.register(registerRequest, request)
        );

        verify(auditRepository).save(any());
    }

    @Test
    void verifyEmail_tokenValid_success() {
        User user = User.builder()
                .email("user@example.com")
                .emailVerified(false)
                .build();

        VerificationToken token = VerificationToken.builder()
                .token("valid-token")
                .user(user)
                .used(false)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

        boolean result = authService.verifyEmail("valid-token");

        assertTrue(result);
        assertTrue(user.isEmailVerified());
        assertTrue(token.isUsed());
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void verifyEmail_tokenExpiredOrUsed_fail() {
        VerificationToken token = VerificationToken.builder()
                .token("expired-token")
                .used(true)
                .expiresAt(Instant.now().minusSeconds(1))
                .user(new User())
                .build();

        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        boolean result = authService.verifyEmail("expired-token");

        assertFalse(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyEmail_tokenNotFound_fail() {
        when(tokenRepository.findByToken("not-found-token")).thenReturn(Optional.empty());

        boolean result = authService.verifyEmail("not-found-token");

        assertFalse(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void logout_shouldInvalidateSessionAndLogAudit() {
        String userId = UUID.randomUUID().toString();
        String sessionId = UUID.randomUUID().toString();

        authService.logout(userId, sessionId);

        verify(redisSessionService).invalidateSession(UUID.fromString(userId), sessionId);
        verify(auditRepository).save(any());
    }
}
