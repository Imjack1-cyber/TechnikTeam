package de.technikteam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserCreateRequest(
		@NotBlank(message = "Username cannot be blank") @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") @Schema(description = "The user's unique username.", required = true) String username,

		@NotBlank(message = "Password cannot be blank") @Schema(description = "The user's initial password. Must meet the password policy.", required = true) String password,

		@NotNull(message = "Role ID cannot be null") @Schema(description = "The ID of the user's role.", required = true) Integer roleId,

		@Email(message = "Must be a valid email format") @Schema(description = "The user's email address.") String email,

		@Schema(description = "The user's class year.") Integer classYear,

		@Schema(description = "The user's class name.") String className,

		@NotNull(message = "Permission list cannot be null") @Schema(description = "A list of IDs for the user's individual permissions.") List<Integer> permissionIds) {
}