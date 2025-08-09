package de.technikteam.api.v1.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * A Data Transfer Object (DTO) representing the credentials for a login
 * request. Using a dedicated DTO provides type safety and allows for
 * declarative validation.
 */
public record LoginRequest(
		@NotBlank(message = "Username cannot be blank") @Schema(description = "The user's unique username.", example = "admin", required = true) String username,

		@NotBlank(message = "Password cannot be blank") @Schema(description = "The user's password.", example = "admin123", required = true) String password) {
}