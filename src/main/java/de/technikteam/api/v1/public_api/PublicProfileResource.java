package de.technikteam.api.v1.public_api;

import com.google.gson.Gson;
import de.technikteam.api.v1.dto.PasswordChangeRequest;
import de.technikteam.api.v1.dto.ProfileChangeRequestDTO;
import de.technikteam.api.v1.dto.TwoFactorSetupDTO;
import de.technikteam.dao.*;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.model.dto.LoginIpInfo;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.ProfileRequestService;
import de.technikteam.service.TwoFactorAuthService;
import de.technikteam.util.PasswordPolicyValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/profile")
@Tag(name = "Public Profile", description = "Endpoints for managing the current user's profile.")
@SecurityRequirement(name = "bearerAuth")
public class PublicProfileResource {
    private static final Logger logger = LogManager.getLogger(PublicProfileResource.class);

	private final UserDAO userDAO;
	private final EventDAO eventDAO;
	private final UserQualificationsDAO qualificationsDAO;
	private final AchievementDAO achievementDAO;
	private final ProfileChangeRequestDAO requestDAO;
	private final ProfileRequestService profileRequestService;
	private final TwoFactorAuthService twoFactorAuthService;
    private final AuthenticationLogDAO authLogDAO;
    private final TwoFactorAuthDAO twoFactorAuthDAO;
	private final PasskeyDAO passkeyDAO;

	@Autowired
	public PublicProfileResource(UserDAO userDAO, EventDAO eventDAO, UserQualificationsDAO qualificationsDAO,
			AchievementDAO achievementDAO, ProfileChangeRequestDAO requestDAO,
			ProfileRequestService profileRequestService, TwoFactorAuthService twoFactorAuthService,
            AuthenticationLogDAO authLogDAO, TwoFactorAuthDAO twoFactorAuthDAO, PasskeyDAO passkeyDAO) {
		this.userDAO = userDAO;
		this.eventDAO = eventDAO;
		this.qualificationsDAO = qualificationsDAO;
		this.achievementDAO = achievementDAO;
		this.requestDAO = requestDAO;
		this.profileRequestService = profileRequestService;
		this.twoFactorAuthService = twoFactorAuthService;
        this.authLogDAO = authLogDAO;
        this.twoFactorAuthDAO = twoFactorAuthDAO;
		this.passkeyDAO = passkeyDAO;
	}

	@GetMapping
	@Operation(summary = "Get current user's profile data", description = "Retrieves a comprehensive set of data for the authenticated user's profile page.")
	public ResponseEntity<ApiResponse> getMyProfile(@AuthenticationPrincipal SecurityUser securityUser) {
	    logger.debug("Fetching full profile data for user '{}'", securityUser.getUsername());
		User user = userDAO.getUserById(securityUser.getUser().getId()); // Fetch latest user data
		Map<String, Object> profileData = new HashMap<>();
		profileData.put("user", user);
		profileData.put("eventHistory", eventDAO.getEventHistoryForUser(user.getId()));
		profileData.put("qualifications", qualificationsDAO.getQualificationsForUser(user.getId()));
		profileData.put("achievements", achievementDAO.getAchievementsForUser(user.getId()));
		profileData.put("passkeys", passkeyDAO.getCredentialsByUserId(user.getId()));
		profileData.put("hasPendingRequest", requestDAO.hasPendingRequest(user.getId()));

		return ResponseEntity.ok(new ApiResponse(true, "Profildaten erfolgreich abgerufen.", profileData));
	}

	@GetMapping("/{userId}")
	@Operation(summary = "Get another user's public profile data", description = "Retrieves a public-safe subset of another user's profile data.")
	public ResponseEntity<ApiResponse> getUserProfile(@PathVariable int userId) {
        logger.debug("Fetching public profile data for user ID {}", userId);
		User user = userDAO.getUserById(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse(false, "Benutzer nicht gefunden.", null));
		}

		Map<String, Object> profileData = new HashMap<>();
		profileData.put("user", user);
		profileData.put("qualifications", qualificationsDAO.getQualificationsForUser(userId));
		profileData.put("achievements", achievementDAO.getAchievementsForUser(userId));
		profileData.put("eventHistory", eventDAO.getEventHistoryForUser(userId));

		return ResponseEntity.ok(new ApiResponse(true, "Profildaten erfolgreich abgerufen.", profileData));
	}
    
    @GetMapping("/known-ips")
    @Operation(summary = "Get known IP addresses for the current user")
    public ResponseEntity<ApiResponse> getKnownIps(@AuthenticationPrincipal SecurityUser securityUser) {
        List<LoginIpInfo> knownIps = twoFactorAuthDAO.getKnownIpsForUser(securityUser.getUser().getId());
        return ResponseEntity.ok(new ApiResponse(true, "Bekannte Standorte abgerufen.", knownIps));
    }

	@PostMapping("/request-change")
	@Operation(summary = "Request a profile data change", description = "Submits a request for an administrator to approve changes to the user's profile data.")
	public ResponseEntity<ApiResponse> requestProfileChange(@Valid @RequestBody ProfileChangeRequestDTO requestDTO,
			@AuthenticationPrincipal SecurityUser securityUser) {
        logger.info("Received profile change request from user '{}' with data: {}", securityUser.getUsername(), requestDTO);
		try {
			profileRequestService.createChangeRequest(securityUser.getUser(), requestDTO);
			return ResponseEntity.ok(new ApiResponse(true, "Änderungsantrag erfolgreich eingereicht.", null));
		} catch (Exception e) {
		    logger.error("Error creating profile change request for user '{}'", securityUser.getUsername(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
					new ApiResponse(false, "Ihr Antrag konnte nicht gespeichert werden: " + e.getMessage(), null));
		}
	}

	@PostMapping("/register-device")
	@Operation(summary = "Register a device token for push notifications")
	public ResponseEntity<ApiResponse> registerDevice(@RequestBody Map<String, String> payload,
			@AuthenticationPrincipal SecurityUser securityUser) {
		String token = payload.get("token");
		logger.debug("Received request to register FCM token for user '{}'", securityUser.getUsername());
		if (token == null || token.isBlank()) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "Token is required.", null));
		}
		if (userDAO.updateFcmToken(securityUser.getUser().getId(), token)) {
			return ResponseEntity.ok(new ApiResponse(true, "Device token registered successfully.", null));
		}
		return ResponseEntity.internalServerError().body(new ApiResponse(false, "Failed to register token.", null));
	}

	@PutMapping("/theme")
	@Operation(summary = "Update user theme", description = "Updates the user's preferred theme (light/dark).")
	public ResponseEntity<ApiResponse> updateUserTheme(@RequestBody Map<String, String> payload,
			@AuthenticationPrincipal SecurityUser securityUser) {
		User user = securityUser.getUser();
		String theme = payload.get("theme");
        logger.debug("User '{}' requested theme change to '{}'", user.getUsername(), theme);
		if (theme != null && (theme.equals("light") || theme.equals("dark"))) {
			if (userDAO.updateUserTheme(user.getId(), theme)) {
				User updatedUser = userDAO.getUserById(user.getId());
				return ResponseEntity.ok(new ApiResponse(true, "Theme aktualisiert.", updatedUser));
			}
		}
		return ResponseEntity.badRequest().body(new ApiResponse(false, "Ungültiges Theme angegeben.", null));
	}

	@PutMapping("/chat-color")
	@Operation(summary = "Update chat color", description = "Updates the user's preferred color for chat messages.")
	public ResponseEntity<ApiResponse> updateChatColor(@RequestBody Map<String, String> payload,
			@AuthenticationPrincipal SecurityUser securityUser) {
		User user = securityUser.getUser();
		String chatColor = payload.get("chatColor");
        logger.debug("User '{}' requested chat color change to '{}'", user.getUsername(), chatColor);
		if (userDAO.updateUserChatColor(user.getId(), chatColor)) {
			return ResponseEntity.ok(new ApiResponse(true, "Chatfarbe aktualisiert.", null));
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Chatfarbe konnte nicht gespeichert werden.", null));
		}
	}

	@PutMapping("/password")
	@Operation(summary = "Change password", description = "Allows the authenticated user to change their own password after verifying their current one.")
	public ResponseEntity<ApiResponse> updatePassword(@Valid @RequestBody PasswordChangeRequest request,
			@AuthenticationPrincipal SecurityUser securityUser) {
        logger.info("Processing password change request for user '{}'", securityUser.getUsername());
		User user = securityUser.getUser();
		if (userDAO.validateUser(user.getUsername(), request.currentPassword()) == null) {
		    logger.warn("Password change failed for user '{}': Incorrect current password provided.", user.getUsername());
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "Das aktuelle Passwort ist nicht korrekt.", null));
		}
		if (!request.newPassword().equals(request.confirmPassword())) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "Die neuen Passwörter stimmen nicht überein.", null));
		}
		PasswordPolicyValidator.ValidationResult validationResult = PasswordPolicyValidator
				.validate(request.newPassword());
		if (!validationResult.isValid()) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, validationResult.getMessage(), null));
		}
		if (userDAO.changePassword(user.getId(), request.newPassword())) {
		    logger.info("Successfully changed password for user '{}'", user.getUsername());
			return ResponseEntity.ok(new ApiResponse(true, "Ihr Passwort wurde erfolgreich geändert.", null));
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Passwort konnte nicht geändert werden.", null));
		}
	}

	@PutMapping("/layout")
	@Operation(summary = "Update layout preferences", description = "Saves the user's custom layout and navigation preferences.")
	public ResponseEntity<ApiResponse> updateDashboardLayout(@RequestBody Object layout,
			@AuthenticationPrincipal SecurityUser securityUser) {
		User user = securityUser.getUser();
		String layoutJson = new Gson().toJson(layout);
        logger.debug("Updating layout for user '{}' with JSON: {}", user.getUsername(), layoutJson);
		if (userDAO.updateDashboardLayout(user.getId(), layoutJson)) {
			User updatedUser = userDAO.getUserById(user.getId());
			return ResponseEntity.ok(new ApiResponse(true, "Layout-Einstellungen gespeichert.", updatedUser));
		}
		return ResponseEntity.internalServerError()
				.body(new ApiResponse(false, "Layout konnte nicht gespeichert werden.", null));
	}

	@PostMapping("/2fa/setup")
	@Operation(summary = "Start 2FA setup", description = "Generates a new TOTP secret and QR code for the user.")
	public ResponseEntity<ApiResponse> setup2FA(@AuthenticationPrincipal SecurityUser securityUser) {
	    logger.info("Initiating 2FA setup for user '{}'", securityUser.getUsername());
		try {
			TwoFactorSetupDTO setupData = twoFactorAuthService.generateNewSecretAndQrCode(securityUser.getUser());
			return ResponseEntity.ok(new ApiResponse(true, "2FA setup data generated.", setupData));
		} catch (Exception e) {
		    logger.error("Error during 2FA setup for user '{}'", securityUser.getUsername(), e);
			return ResponseEntity.internalServerError().body(new ApiResponse(false, e.getMessage(), null));
		}
	}

	@PostMapping("/2fa/enable")
	@Operation(summary = "Verify and enable 2FA", description = "Verifies a TOTP token and enables 2FA for the user, returning backup codes.")
	public ResponseEntity<ApiResponse> enable2FA(@RequestBody Map<String, String> payload,
			@AuthenticationPrincipal SecurityUser securityUser) {
		String secret = payload.get("secret");
		String token = payload.get("token");
        logger.info("Attempting to enable 2FA for user '{}'", securityUser.getUsername());
		if (secret == null || token == null) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "Secret and token are required.", null));
		}
		try {
			List<String> backupCodes = twoFactorAuthService.enableTotpForUser(securityUser.getUser().getId(), secret, token);
			return ResponseEntity.ok(new ApiResponse(true, "2FA enabled successfully.", Map.of("backupCodes", backupCodes)));
		} catch (Exception e) {
		    logger.error("Failed to enable 2FA for user '{}': {}", securityUser.getUsername(), e.getMessage());
			return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
		}
	}

	@PostMapping("/2fa/disable")
	@Operation(summary = "Disable 2FA", description = "Disables 2FA for the user after verifying a current TOTP token.")
	public ResponseEntity<ApiResponse> disable2FA(@RequestBody Map<String, String> payload,
			@AuthenticationPrincipal SecurityUser securityUser) {
		String token = payload.get("token");
        logger.info("Attempting to disable 2FA for user '{}'", securityUser.getUsername());
		if (token == null) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "Token is required.", null));
		}
		try {
			twoFactorAuthService.disableTotpForUser(securityUser.getUser().getId(), token);
			return ResponseEntity.ok(new ApiResponse(true, "2FA disabled successfully.", null));
		} catch (Exception e) {
            logger.error("Failed to disable 2FA for user '{}': {}", securityUser.getUsername(), e.getMessage());
			return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
		}
	}

    @PostMapping("/known-ips/forget")
    @Operation(summary = "Forget a known IP address", description = "Removes an IP address from the user's list of known IPs, forcing a 2FA challenge on the next login from that location.")
    public ResponseEntity<ApiResponse> forgetIp(@RequestBody Map<String, String> payload, @AuthenticationPrincipal SecurityUser securityUser) {
        String ipAddress = payload.get("ipAddress");
        if (ipAddress == null || ipAddress.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "IP address is required.", null));
        }
        if (twoFactorAuthDAO.forgetIp(securityUser.getUser().getId(), ipAddress)) {
            return ResponseEntity.ok(new ApiResponse(true, "IP address forgotten. 2FA will be required on next login from this location.", null));
        }
        return ResponseEntity.internalServerError().body(new ApiResponse(false, "Could not forget IP address.", null));
    }

    @PostMapping("/known-ips/forget-all")
    @Operation(summary = "Forget all known IP addresses", description = "Removes all of the user's known IPs, forcing 2FA challenges on all subsequent logins.")
    public ResponseEntity<ApiResponse> forgetAllIps(@AuthenticationPrincipal SecurityUser securityUser) {
        twoFactorAuthDAO.clearKnownIpsForUser(securityUser.getUser().getId());
        return ResponseEntity.ok(new ApiResponse(true, "All known locations have been forgotten. 2FA will be required on your next login.", null));
    }
}