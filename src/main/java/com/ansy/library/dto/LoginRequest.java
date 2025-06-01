package com.ansy.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @Email(message = "{login.invalidEmail}")
    @NotBlank(message = "{login.required.email}")
    private String email;

    @NotBlank(message = "{login.required.password}")
    private String password;
}
