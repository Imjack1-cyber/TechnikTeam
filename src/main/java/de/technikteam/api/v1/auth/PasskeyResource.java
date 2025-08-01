package de.technikteam.api.v1.auth;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AuthService;
import de.technikteam.service.ChallengeRepository;
import de.technikteam.service.PasskeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/passkey")
@Tag(name = "Passkey Authentication", description = "Endpoints for WebAuthn/Passkey registration and authentication (passwordless login).")
public class PasskeyResource {
	private static final Logger logger = LogManager.getLogger(PasskeyResource.class);

	private final PasskeyService passkeyService;
	private final AuthService authService;
	private final ChallengeRepository challengeRepository;

	@Autowired
	public PasskeyResource(PasskeyService passkeyService, AuthService authService,
			ChallengeRepository challengeRepository) {
		this.passkeyService = passkeyService;
		this.authService = authService;
		this.challengeRepository = challengeRepository;
	}

	@PostMapping("/register/start")
	@Operation(summary = "Start passkey registration", description = "Initiates the WebAuthn registration ceremony to create a new passkey.")
	public ResponseEntity<ApiResponse> startRegistration(@Valid @RequestBody RegistrationStartRequest request,
			@AuthenticationPrincipal SecurityUser securityUser) {
		try {
			// Temporarily store the device name with the challenge
			challengeRepository.addDeviceName(securityUser.getUsername(), request.deviceName());
			String registrationOptionsJson = passkeyService.startRegistration(securityUser.getUser());
			return ResponseEntity.ok(new ApiResponse(true, "Registration ceremony started.", registrationOptionsJson));
		} catch (Exception e) {
			logger.error("Failed to start passkey registration for user {}: {}", securityUser.getUsername(),
					e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Failed to start passkey registration: " + e.getMessage(), null));
		}
	}

	@PostMapping("/register/finish")
	@Operation(summary = "Finish passkey registration", description = "Completes the WebAuthn registration ceremony and saves the new passkey credential.")
	public ResponseEntity<ApiResponse> finishRegistration(@RequestBody String credentialJson,
			@AuthenticationPrincipal SecurityUser securityUser) {
		try {
			String deviceName = challengeRepository.getDeviceName(securityUser.getUsername()).orElse("New Device"); // Fallback
																													// device
																													// name

			if (passkeyService.finishRegistration(securityUser.getUser().getId(), securityUser.getUsername(),
					deviceName, credentialJson)) {
				return ResponseEntity.ok(new ApiResponse(true, "Passkey registered successfully.", null));
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new ApiResponse(false, "Failed to save passkey credential.", null));
			}
		} catch (Exception e) {
			logger.error("Failed to finish passkey registration for user {}: {}", securityUser.getUsername(),
					e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Failed to finish passkey registration: " + e.getMessage(), null));
		} finally {
			challengeRepository.removeDeviceName(securityUser.getUsername());
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
	@Operation(summary = "Finish passkey authentication", description = "Completes the WebAuthn authentication ceremony and logs the user in if successful by setting an HttpOnly cookie.")
	public ResponseEntity<ApiResponse> finishAuthentication(@RequestBody String credentialJson,
			HttpServletResponse response) {
		try {
			User authenticatedUser = passkeyService.finishAuthentication(credentialJson);
			if (authenticatedUser != null) {
				authService.addJwtCookie(authenticatedUser, response);
				return ResponseEntity.ok(new ApiResponse(true, "Login successful", authenticatedUser));
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