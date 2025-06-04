package com.ansy.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

public record ResetPasswordRequest(

        @NotBlank(message = "{resetPassword.required.token}")
        String token,

        @NotBlank
        @Size(min = 8, message = "{resetPassword.invalidShortPassword}")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$",
                message = "{resetPassword.invalidSimplePassword}"
        )
        String newPassword
) {
}

