package com.ansy.library.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class RentResponse {
    private UUID rentId;
    private UUID bookId;
    private String bookTitle;
    private Instant rentedAt;
    private Instant dueAt;
}
