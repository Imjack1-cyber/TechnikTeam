package de.technikteam.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FileRenameRequest(
		@NotBlank(message = "Der neue Name darf nicht leer sein.") @Size(max = 255, message = "Der Dateiname darf maximal 255 Zeichen lang sein.") String newName) {
}