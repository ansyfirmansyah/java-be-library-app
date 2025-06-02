package com.ansy.library.controller;

import com.ansy.library.dto.AdminRentViewResponse;
import com.ansy.library.dto.ApiResponse;
import com.ansy.library.dto.RentRequest;
import com.ansy.library.dto.RentResponse;
import com.ansy.library.entity.Rent;
import com.ansy.library.service.RentService;
import com.ansy.library.utils.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/rent")
@RequiredArgsConstructor
@Tag(name = "Rent")
public class RentController {

    private final RentService rentService;
    private final MessageSource messageSource;

    @PostMapping
    public ResponseEntity<ApiResponse> rentBook(@Valid @RequestBody RentRequest request,
                                                @AuthenticationPrincipal Jwt jwt) {
        String userId = JwtUtil.getCurrentUserId();
        RentResponse rent = rentService.rentBook(java.util.UUID.fromString(userId), request);
        return ResponseEntity.ok(ApiResponse.success(
                messageSource.getMessage("rent.success", null, LocaleContextHolder.getLocale()),
                rent
        ));
    }

    @PostMapping("/return")
    public ResponseEntity<ApiResponse> returnBook(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnedDate
    ) {
        String userId = JwtUtil.getCurrentUserId();
        Instant returnedInstant = returnedDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        rentService.returnBook(userId, returnedInstant);
        return ResponseEntity.ok(ApiResponse.success(
                messageSource.getMessage("rent.return.success", null, LocaleContextHolder.getLocale())
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/active")
    public ResponseEntity<ApiResponse> getAllUnreturnedBooks() {
        List<AdminRentViewResponse> result = rentService.getAllUnreturnedBooks();
        return ResponseEntity.ok(ApiResponse.success(
                messageSource.getMessage("rent.list.active", null, LocaleContextHolder.getLocale()),
                result
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/overdue")
    public ResponseEntity<ApiResponse> getOverdueBooks() {
        List<AdminRentViewResponse> result = rentService.getOverdueBooks();
        return ResponseEntity.ok(ApiResponse.success(
                messageSource.getMessage("rent.list.overdue", null, LocaleContextHolder.getLocale()),
                result
        ));
    }
}
