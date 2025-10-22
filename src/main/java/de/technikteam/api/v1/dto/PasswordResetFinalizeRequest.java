package de.technikteam.api.v1.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetFinalizeRequest(
        @NotBlank String token,
        @NotBlank String newPassword
) {}