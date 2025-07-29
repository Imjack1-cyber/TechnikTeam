package de.technikteam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record WikiUpdateRequest(
		@NotNull(message = "Content cannot be null") @Schema(description = "The full Markdown content of the wiki page.", required = true) String content) {
}