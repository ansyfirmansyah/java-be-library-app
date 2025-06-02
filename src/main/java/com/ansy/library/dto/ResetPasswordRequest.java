package com.ansy.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "{resetPassword.required.token}")
    private String token;

    @NotBlank
    @Size(min = 8, message = "{resetPassword.invalidShortPassword}")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "{resetPassword.invalidSimplePassword}"
    )
    private String newPassword;
}