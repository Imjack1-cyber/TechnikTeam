package de.technikteam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GeneralFeedbackRequest(
		@NotBlank(message = "Subject cannot be blank") @Size(max = 255, message = "Subject cannot be longer than 255 characters") @Schema(description = "The subject line of the feedback.", required = true) String subject,

		@NotBlank(message = "Content cannot be blank") @Schema(description = "The detailed content of the feedback.", required = true) String content) {
}