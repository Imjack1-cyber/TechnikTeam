package de.technikteam.api.v1.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RegistrationStartRequest(
		@NotBlank(message = "Device name cannot be blank") @Schema(description = "A user-provided name for the new passkey/device (e.g., 'My Laptop').", example = "My MacBook", required = true) String deviceName) {
}