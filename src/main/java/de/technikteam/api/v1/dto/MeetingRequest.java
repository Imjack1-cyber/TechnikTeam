package de.technikteam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record MeetingRequest(
		@NotNull(message = "Kurs-ID darf nicht null sein") @Schema(description = "The ID of the parent course for this meeting.", required = true) Integer courseId,

		@NotBlank(message = "Meeting-Name darf nicht leer sein") @Schema(description = "The name of the meeting.", required = true, example = "Teil 1: Grundlagen") String name,

		@NotNull(message = "Datum und Uhrzeit des Meetings dürfen nicht null sein") @Schema(description = "The start date and time of the meeting.", required = true) LocalDateTime meetingDateTime,

		@Schema(description = "The optional end date and time of the meeting.") LocalDateTime endDateTime,

		@Schema(description = "The ID of the user leading the meeting.") Integer leaderUserId,

		@Schema(description = "A description of the meeting's content.") String description,

		@Schema(description = "The location of the meeting.") String location,

		@Min(value = 1, message = "Maximale Teilnehmerzahl muss mindestens 1 sein") @Schema(description = "The maximum number of participants for the meeting. Null for unlimited.") Integer maxParticipants,

		@Schema(description = "The date and time after which signups are closed.") LocalDateTime signupDeadline) {
}