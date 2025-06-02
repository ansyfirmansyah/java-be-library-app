package com.ansy.library.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RentRequest {
    @NotNull
    private UUID bookId;

    @Min(1)
    @Max(30)
    private int days; // durasi pinjam buku dalam hari
}
