package de.technikteam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

public record ProfileChangeRequestDTO(
		@Email(message = "Must be a valid email format") @Schema(description = "The user's new email address.") String email,

		@Schema(description = "The user's new class year.") Integer classYear,

		@Schema(description = "The user's new class name.") String className) {
}