package de.technikteam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GeneralFeedbackRequest(
		@NotBlank(message = "Betreff darf nicht leer sein") @Size(max = 255, message = "Betreff darf nicht länger als 255 Zeichen sein") @Schema(description = "The subject line of the feedback.", required = true) String subject,

		@NotBlank(message = "Inhalt darf nicht leer sein") @Schema(description = "The detailed content of the feedback.", required = true) String content) {
}