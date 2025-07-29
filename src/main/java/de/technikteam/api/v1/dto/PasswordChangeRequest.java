package de.technikteam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequest(
		@NotBlank(message = "Current password cannot be blank") @Schema(description = "The user's current password.", required = true) String currentPassword,

		@NotBlank(message = "New password cannot be blank") @Schema(description = "The desired new password. Must meet the password policy.", required = true) String newPassword,

		@NotBlank(message = "Confirmation password cannot be blank") @Schema(description = "Confirmation of the new password.", required = true) String confirmPassword) {
}