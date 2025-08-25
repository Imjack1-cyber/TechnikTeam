package de.technikteam.api.v1.dto;

import jakarta.validation.constraints.NotBlank;

public record TwoFactorVerificationRequest(
        @NotBlank String token,
        String backupCode
) {
}