package de.technikteam.api.v1.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PasskeyAuthenticationFinishRequest(
		@NotBlank @Schema(description = "The Base64Url-encoded credential ID from the WebAuthn API.", required = true) String id,

		@NotBlank @Schema(description = "The Base64Url-encoded raw ID from the WebAuthn API.", required = true) String rawId,

		@NotBlank @Schema(description = "The type of credential ('public-key').", required = true) String type,

		@NotNull @Schema(description = "The client's response object containing authenticatorData, clientDataJSON, and signature.", required = true) Response response) {
	public record Response(
			@NotBlank @Schema(description = "The Base64Url-encoded authenticatorData.", required = true) String authenticatorData,

			@NotBlank @Schema(description = "The Base64Url-encoded clientDataJSON.", required = true) String clientDataJSON,

			@NotBlank @Schema(description = "The Base64Url-encoded signature.", required = true) String signature,

			@Schema(description = "The Base64Url-encoded userHandle (optional).") String userHandle) {
	}
}