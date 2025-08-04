package de.technikteam.api.v1.dto.passkey;

import io.swagger.v3.oas.annotations.media.Schema;

public record FinishLoginRequest(
		@Schema(description = "The PublicKeyCredential object received from the navigator.credentials.get() call, serialized as a JSON string.", required = true) String credential) {
}