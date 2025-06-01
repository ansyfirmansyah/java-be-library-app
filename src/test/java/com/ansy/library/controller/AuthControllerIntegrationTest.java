package com.ansy.library.controller;

import com.ansy.library.dto.LoginRequest;
import com.ansy.library.dto.RegisterRequest;
import com.ansy.library.entity.User;
import com.ansy.library.entity.VerificationToken;
import com.ansy.library.repository.UserRepository;
import com.ansy.library.repository.VerificationTokenRepository;
import com.ansy.library.service.JwtService;
import com.ansy.library.service.RedisSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RedisSessionService redisSessionService;

    @AfterEach
    void cleanupRedisKeys() {
        redisTemplate.delete("RATE_LIMIT:LOGIN_FAIL:ratelimit@example.com:127.0.0.1");
        redisTemplate.delete("RATE_LIMIT:LOGIN_FAIL:failuser@example.com:127.0.0.1");
    }


    @Test
    @Transactional
    void register_shouldReturnSuccessOrFailure() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("testuser@example.com");
        request.setPassword("Password1");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Transactional
    void register_shouldFailIfEmailAlreadyExists() throws Exception {
        User user = new User();
        user.setEmail("existuser@example.com");
        user.setPassword("Password1");
        user.setEmailVerified(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("existuser@example.com");
        request.setPassword("Password1");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email sudah terdaftar"));
    }

    @Test
    @Transactional
    void verifyEmail_invalidToken_shouldReturnErrorMessage() throws Exception {
        mockMvc.perform(get("/auth/verify")
                        .param("token", "invalid-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Transactional
    void verifyEmail_validToken_shouldReturnSuccessMessage() throws Exception {
        User user = new User();
        user.setEmail("validuser@example.com");
        user.setPassword("Password1");
        user.setEmailVerified(false);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user = userRepository.save(user);

        VerificationToken token = new VerificationToken();
        token.setId(UUID.randomUUID());
        token.setToken("valid-token");
        token.setUser(user);
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        verificationTokenRepository.save(token);

        mockMvc.perform(get("/auth/verify")
                        .param("token", "valid-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Transactional
    void register_shouldFailIfEmailDomainInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@invalid-domain-xyz123.com");
        request.setPassword("Password1");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Domain email tidak valid"));
    }

    @Test
    @Transactional
    void register_shouldFailIfPasswordWeak() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("12345678"); // no uppercase or lowercase mix

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Transactional
    void login_shouldSucceedWithCorrectCredentials() throws Exception {
        User user = new User();
        user.setEmail("loginuser@example.com");
        user.setPassword("$2a$10$eUIidNd7dWn6CN5XLqg8E.VBCiAfq6a6xfQBKFAqox7KW2NvqSQiS"); // Password1 (bcrypt)
        user.setEmailVerified(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user = userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("loginuser@example.com");
        request.setPassword("Password1");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.expiredAt").exists());
    }

    @Test
    @Transactional
    void login_shouldFailWithInvalidPassword() throws Exception {
        User user = new User();
        user.setEmail("failuser@example.com");
        user.setPassword("$2a$10$DdDQ3lrbgb62aXXFzQGrve7jWeapAQ7UtFG9XbwVXRa9Mkp/Kj14i"); // Password1
        user.setEmailVerified(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user = userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("failuser@example.com");
        request.setPassword("WrongPassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email dan/atau Password tidak valid"));
    }

    @Test
    @Transactional
    void login_shouldFailIfRateLimited() throws Exception {
        User user = new User();
        user.setEmail("ratelimit@example.com");
        user.setPassword("$2a$10$eUIidNd7dWn6CN5XLqg8E.VBCiAfq6a6xfQBKFAqox7KW2NvqSQiS"); // Password1
        user.setEmailVerified(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("ratelimit@example.com");
        request.setPassword("WrongPassword");

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Transactional
    void logout_shouldInvalidateSessionAndReturnSuccess() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("logoutuser@example.com");
        user.setPassword("$2a$10$eUIidNd7dWn6CN5XLqg8E.VBCiAfq6a6xfQBKFAqox7KW2NvqSQiS"); // Password1
        user.setEmailVerified(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user = userRepository.save(user);

        String sessionId = UUID.randomUUID().toString();
        Instant issuedAt = Instant.now();
        Instant expiredAt = issuedAt.plusSeconds(jwtService.getExpiration());

        String token = jwtService.generateToken(user.getId(), "USER", sessionId, issuedAt, expiredAt);
        redisSessionService.storeSession(user.getId(), sessionId, expiredAt);

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Berhasil logout"));
    }

    @Test
    void logout_shouldReturnUnauthorized_whenSessionMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        String sessionId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        String token = jwtService.generateToken(userId, "USER", sessionId, now, now.plusSeconds(3600));

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expiry session"));
    }
}
