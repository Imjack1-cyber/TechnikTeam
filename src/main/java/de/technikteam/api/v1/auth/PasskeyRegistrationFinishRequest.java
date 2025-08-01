package de.technikteam.api.v1.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PasskeyRegistrationFinishRequest(
		@NotBlank @Schema(description = "The Base64Url-encoded credential ID from the WebAuthn API.", required = true) String id,

		@NotBlank @Schema(description = "The Base64Url-encoded raw ID from the WebAuthn API.", required = true) String rawId,

		@NotBlank @Schema(description = "The type of credential ('public-key').", required = true) String type,

		@NotNull @Schema(description = "The client's response object containing clientDataJSON and attestationObject.", required = true) Response response) {
	public record Response(
			@NotBlank @Schema(description = "The Base64Url-encoded clientDataJSON.", required = true) String clientDataJSON,

			@NotBlank @Schema(description = "The Base64Url-encoded attestationObject.", required = true) String attestationObject) {
	}
}