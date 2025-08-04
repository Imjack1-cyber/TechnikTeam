package de.technikteam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

public record ProfileChangeRequestDTO(
		@Email(message = "Muss ein gültiges E-Mail-Format sein") @Schema(description = "The user's new email address.") String email,

		@Schema(description = "The user's new class year.") Integer classYear,

		@Schema(description = "The user's new class name.") String className,

		@Schema(description = "The user's new profile icon class (e.g., 'fa-user-ninja').") String profileIconClass) {
}