package com.ansy.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @Email(message = "{forgotPassword.invalidEmail}")
        @NotBlank(message = "{forgotPassword.required.email}")
        String email
) {
}
