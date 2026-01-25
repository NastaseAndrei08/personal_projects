package com.example.demo.auth.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password,
        @Pattern(regexp = "ADMIN|CLIENT", message = "role must be ADMIN or CLIENT")
        String role
) {}
