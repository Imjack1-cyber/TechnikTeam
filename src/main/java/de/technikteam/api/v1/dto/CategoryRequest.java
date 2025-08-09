package de.technikteam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
		@NotBlank(message = "Kategoriename darf nicht leer sein") @Size(min = 2, max = 100, message = "Kategoriename muss zwischen 2 und 100 Zeichen lang sein") @Schema(description = "The name for the new file category.", required = true) String name) {
}