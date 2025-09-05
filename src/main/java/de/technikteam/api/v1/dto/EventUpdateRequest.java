package de.technikteam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record EventUpdateRequest(@NotBlank @Schema(description = "Name of the event") String name,
		@NotNull @Schema(description = "Start date and time of the event") LocalDateTime eventDateTime,
		@Schema(description = "End date and time of the event") LocalDateTime endDateTime,
		@Schema(description = "Detailed description of the event") String description,
		@Schema(description = "ID of the venue for the event") Integer venueId,
		@Schema(description = "ID of the pre-flight checklist template") Integer preflightTemplateId,
		@Schema(description = "Current status of the event (e.g., GEPLANT, LAUFEND)") String status,
		@Schema(description = "ID of the user leading the event") Integer leaderUserId,
		@Schema(description = "Required role for viewing attachments") String requiredRole,
		@Schema(description = "Reminder time in minutes before the event starts") Integer reminderMinutes,
		@Schema(description = "Array of course IDs for skill requirements") List<String> requiredCourseIds,
		@Schema(description = "Array of required person counts for skills") List<String> requiredPersons,
		@Schema(description = "Array of item IDs for reservations") List<String> itemIds,
		@Schema(description = "Array of quantities for reserved items") List<String> quantities) {
}