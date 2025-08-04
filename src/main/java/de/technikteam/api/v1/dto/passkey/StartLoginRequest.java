package de.technikteam.api.v1.dto.passkey;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record StartLoginRequest(
		@NotBlank @Schema(description = "The username of the user attempting to log in.", required = true) String username) {
}