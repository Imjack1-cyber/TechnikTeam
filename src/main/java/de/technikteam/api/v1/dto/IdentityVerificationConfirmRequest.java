package de.technikteam.api.v1.dto;

import jakarta.validation.constraints.NotBlank;

public record IdentityVerificationConfirmRequest(
        @NotBlank String challengeToken,
        @NotBlank String decision // "approve" or "deny"
) {}