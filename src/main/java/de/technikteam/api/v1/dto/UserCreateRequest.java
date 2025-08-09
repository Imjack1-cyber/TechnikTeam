package de.technikteam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserCreateRequest(
		@NotBlank(message = "Benutzername darf nicht leer sein") @Size(min = 3, max = 50, message = "Benutzername muss zwischen 3 und 50 Zeichen lang sein") @Schema(description = "The user's unique username.", required = true) String username,

		@NotBlank(message = "Passwort darf nicht leer sein") @Schema(description = "The user's initial password. Must meet the password policy.", required = true) String password,

		@NotNull(message = "Rollen-ID darf nicht null sein") @Schema(description = "The ID of the user's role.", required = true) Integer roleId,

		@Email(message = "Muss ein gültiges E-Mail-Format sein") @Schema(description = "The user's email address.") String email,

		@Schema(description = "The user's class year.") Integer classYear,

		@Schema(description = "The user's class name.") String className,

		@NotNull(message = "Berechtigungsliste darf nicht null sein") @Schema(description = "A list of IDs for the user's individual permissions.") List<Integer> permissionIds) {
}