package com.ansy.library.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RentRequest(
        @NotNull
        UUID bookId,

        @Min(1)
        @Max(30)
        int days // durasi pinjam buku dalam hari
) {
}
