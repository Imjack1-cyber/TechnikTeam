package de.technikteam.api.v1.auth;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.AuthService;
import de.technikteam.service.PasskeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/passkey")
@Tag(name = "Passkey Authentication", description = "Endpoints for WebAuthn/Passkey registration and authentication (passwordless login).")
public class PasskeyResource {
	private static final Logger logger = LogManager.getLogger(PasskeyResource.class);

	private final PasskeyService passkeyService;
	private final AuthService authService;

	@Autowired
	public PasskeyResource(PasskeyService passkeyService, AuthService authService) {
		this.passkeyService = passkeyService;
		this.authService = authService;
	}

	@PostMapping("/register/start")
	@Operation(summary = "Start passkey registration", description = "Initiates the WebAuthn registration ceremony to create a new passkey.")
	public ResponseEntity<ApiResponse> startRegistration(@Valid @RequestBody RegistrationStartRequest request,
			@AuthenticationPrincipal User currentUser) {
		try {
			String registrationOptionsJson = passkeyService.startRegistration(currentUser);
			return ResponseEntity.ok(new ApiResponse(true, "Registration ceremony started.", registrationOptionsJson));
		} catch (Exception e) {
			logger.error("Failed to start passkey registration for user {}: {}", currentUser.getUsername(),
					e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Failed to start passkey registration: " + e.getMessage(), null));
		}
	}

	@PostMapping("/register/finish")
	@Operation(summary = "Finish passkey registration", description = "Completes the WebAuthn registration ceremony and saves the new passkey credential.")
	public ResponseEntity<ApiResponse> finishRegistration(
			@Parameter(description = "Name for the new device") @RequestParam String deviceName,
			@Valid @RequestBody PasskeyRegistrationFinishRequest request, @AuthenticationPrincipal User currentUser) {
		try {
			if (passkeyService.finishRegistration(currentUser.getId(), request.toString(), deviceName)) {
				return ResponseEntity.ok(new ApiResponse(true, "Passkey registered successfully.", null));
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new ApiResponse(false, "Failed to save passkey credential.", null));
			}
		} catch (Exception e) {
			logger.error("Failed to finish passkey registration for user {}: {}", currentUser.getUsername(),
					e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Failed to finish passkey registration: " + e.getMessage(), null));
		}
	}

	@PostMapping("/login/start")
	@Operation(summary = "Start passkey authentication", description = "Initiates the WebAuthn authentication ceremony to log in with a passkey.")
	public ResponseEntity<ApiResponse> startAuthentication(
			@Parameter(description = "Username for passkey authentication") @RequestParam String username) {
		try {
			String authenticationOptionsJson = passkeyService.startAuthentication(username);
			return ResponseEntity
					.ok(new ApiResponse(true, "Authentication ceremony started.", authenticationOptionsJson));
		} catch (Exception e) {
			logger.error("Failed to start passkey authentication for username {}: {}", username, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Failed to start passkey authentication: " + e.getMessage(), null));
		}
	}

	@PostMapping("/login/finish")
	@Operation(summary = "Finish passkey authentication", description = "Completes the WebAuthn authentication ceremony and logs the user in if successful, returning a JWT.")
	public ResponseEntity<ApiResponse> finishAuthentication(
			@Valid @RequestBody PasskeyAuthenticationFinishRequest request) {
		try {
			User authenticatedUser = passkeyService.finishAuthentication(request.toString());
			if (authenticatedUser != null) {
				String token = authService.generateToken(authenticatedUser);
				return ResponseEntity.ok(new ApiResponse(true, "Login successful", Map.of("token", token)));
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(new ApiResponse(false, "Passkey authentication failed.", null));
			}
		} catch (Exception e) {
			logger.error("Failed to finish passkey authentication: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Failed to finish passkey authentication: " + e.getMessage(), null));
		}
	}
}