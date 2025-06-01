package com.ansy.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @Email(message = "{registration.invalidEmail}")
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, message = "{registration.invalidShortPassword}")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "{registration.invalidSimplePassword}"
    )
    private String password;
}
