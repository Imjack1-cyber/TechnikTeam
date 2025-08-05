package de.technikteam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record EventDebriefingDTO(@NotNull @Schema(description = "Summary of what went well.") String whatWentWell,
		@NotNull @Schema(description = "Summary of what could be improved.") String whatToImprove,
		@Schema(description = "Notes about specific equipment performance.") String equipmentNotes,
		@Schema(description = "List of user IDs for crew members who performed exceptionally.") List<Integer> standoutCrewMemberIds) {
}