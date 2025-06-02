package com.ansy.library.security;

import com.ansy.library.service.JwtService;
import com.ansy.library.service.RedisSessionService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RedisSessionService redisSessionService;
    private final MessageSource messageSource;

    private void writeError(HttpServletResponse response, int status, boolean successStatus, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":" + successStatus + ",\"message\":\"" + message + "\"}");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            Claims claims = jwtService.parseToken(token);
            String sessionId = claims.get("sid", String.class);
            UUID userId = UUID.fromString(claims.get("uid", String.class));
            String role = claims.get("role", String.class);
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

            if (!redisSessionService.sessionExists(userId, sessionId)) {
//                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                String message = messageSource.getMessage("general.invalidSession", null, LocaleContextHolder.getLocale());
//                response.getWriter().write();
                writeError(response, HttpStatus.UNAUTHORIZED.value(), false, message);
                return;
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId.toString(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (JwtException | IllegalArgumentException e) {
//            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            String message = messageSource.getMessage("general.invalidToken", null, LocaleContextHolder.getLocale());
//            response.getWriter().write(message);
            writeError(response, HttpStatus.UNAUTHORIZED.value(), false, message);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
