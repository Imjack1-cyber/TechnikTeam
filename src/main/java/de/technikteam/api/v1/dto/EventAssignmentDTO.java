package de.technikteam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record EventAssignmentDTO(@NotNull @Schema(description = "The ID of the user being assigned.") Integer userId,

		@Schema(description = "The ID of the event role they are assigned to. Can be null for 'Unassigned'.") Integer roleId) {
}