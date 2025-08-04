package de.technikteam.api.v1.dto.passkey;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record FinishRegistrationRequest(
		@NotBlank @Schema(description = "A user-friendly name for the device being registered (e.g., 'My Laptop').", required = true) String deviceName,

		@NotBlank @Schema(description = "The PublicKeyCredential object received from the navigator.credentials.create() call, serialized as a JSON string.", required = true) String credential) {
}