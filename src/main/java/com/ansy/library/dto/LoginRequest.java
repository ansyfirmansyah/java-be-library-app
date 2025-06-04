package com.ansy.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public record LoginRequest(

        @Email(message = "{login.invalidEmail}")
        @NotBlank(message = "{login.required.email}")
        String email,

        @NotBlank(message = "{login.required.password}")
        String password
) {
}
