package com.ansy.library.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class AdminRentViewResponse {
    private UUID rentId;
    private UUID userId;
    private String userEmail;
    private UUID bookId;
    private String bookTitle;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate rentedAt;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueAt;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate returnedDate;
}
