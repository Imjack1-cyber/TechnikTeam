package de.technikteam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequest(
		@NotBlank(message = "Aktuelles Passwort darf nicht leer sein") @Schema(description = "The user's current password.", required = true) String currentPassword,

		@NotBlank(message = "Neues Passwort darf nicht leer sein") @Schema(description = "The desired new password. Must meet the password policy.", required = true) String newPassword,

		@NotBlank(message = "Bestätigungspasswort darf nicht leer sein") @Schema(description = "Confirmation of the new password.", required = true) String confirmPassword) {
}