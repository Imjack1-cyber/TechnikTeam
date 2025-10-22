package de.technikteam.api.v1.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetInitiateRequest(
        @NotBlank String usernameOrEmail
) {}