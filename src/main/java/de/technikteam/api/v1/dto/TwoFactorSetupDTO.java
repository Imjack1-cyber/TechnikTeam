package de.technikteam.api.v1.dto;

public record TwoFactorSetupDTO(String secret, String qrCodeDataUri) {
}