package com.example.demo.user.web.dto;

import java.util.UUID;

public record UserProfileDto(
        UUID id,
        String username,
        String fullName,
        String email,
        String role
) {}
