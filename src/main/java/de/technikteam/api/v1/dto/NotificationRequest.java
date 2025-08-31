package de.technikteam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@SuppressWarnings("deprecation") // Suppress warning for Schema on record components, which is a known issue
public record NotificationRequest(
		@NotBlank(message = "Titel darf nicht leer sein") @Size(max = 100, message = "Titel darf 100 Zeichen nicht überschreiten") @Schema(description = "The title of the notification.", required = true) String title,

		@NotBlank(message = "Beschreibung darf nicht leer sein") @Schema(description = "The main content of the notification.", required = true) String description,

		@NotBlank(message = "Stufe darf nicht leer sein") @Schema(description = "The severity level of the notification.", required = true, allowableValues = {
				"Informational", "Important", "Warning" }) String level,

		@NotBlank(message = "Zieltyp darf nicht leer sein") @Schema(description = "The target audience type.", required = true, allowableValues = {
				"ALL", "EVENT", "MEETING" }) String targetType,

		@Schema(description = "The ID of the event or meeting if targetType is EVENT or MEETING.") Integer targetId){
}