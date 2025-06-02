package com.ansy.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @Email(message = "{forgotPassword.invalidEmail}")
    @NotBlank(message = "{forgotPassword.required.email}")
    private String email;
}
