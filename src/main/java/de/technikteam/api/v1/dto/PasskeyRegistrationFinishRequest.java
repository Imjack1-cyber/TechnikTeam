package de.technikteam.api.v1.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PasskeyRegistrationFinishRequest(
    @NotNull(message = "Credential data cannot be null.")
    JsonNode credential,

    @NotBlank(message = "Device name cannot be blank.")
    String deviceName
) {}