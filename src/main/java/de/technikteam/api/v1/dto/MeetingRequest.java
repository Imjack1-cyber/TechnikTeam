package de.technikteam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record MeetingRequest(
		@NotNull(message = "Course ID cannot be null") @Schema(description = "The ID of the parent course for this meeting.", required = true) Integer courseId,

		@NotBlank(message = "Meeting name cannot be blank") @Schema(description = "The name of the meeting.", required = true, example = "Teil 1: Grundlagen") String name,

		@NotNull(message = "Meeting date and time cannot be null") @FutureOrPresent(message = "Meeting date must be in the present or future") @Schema(description = "The start date and time of the meeting.", required = true) LocalDateTime meetingDateTime,

		@Schema(description = "The optional end date and time of the meeting.") LocalDateTime endDateTime,

		@Schema(description = "The ID of the user leading the meeting.") Integer leaderUserId,

		@Schema(description = "A description of the meeting's content.") String description,

		@Schema(description = "The location of the meeting.") String location) {
}