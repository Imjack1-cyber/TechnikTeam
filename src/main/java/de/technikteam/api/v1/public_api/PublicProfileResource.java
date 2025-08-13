package de.technikteam.api.v1.public_api;

import com.google.gson.Gson;
import de.technikteam.api.v1.dto.PasswordChangeRequest;
import de.technikteam.api.v1.dto.ProfileChangeRequestDTO;
import de.technikteam.dao.*;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.ProfileRequestService;
import de.technikteam.util.PasswordPolicyValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/profile")
@Tag(name = "Public Profile", description = "Endpoints for managing the current user's profile.")
@SecurityRequirement(name = "bearerAuth")
public class PublicProfileResource {

	private final UserDAO userDAO;
	private final EventDAO eventDAO;
	private final UserQualificationsDAO qualificationsDAO;
	private final AchievementDAO achievementDAO;
	private final ProfileChangeRequestDAO requestDAO;
	private final ProfileRequestService profileRequestService;

	@Autowired
	public PublicProfileResource(UserDAO userDAO, EventDAO eventDAO, UserQualificationsDAO qualificationsDAO,
			AchievementDAO achievementDAO, ProfileChangeRequestDAO requestDAO,
			ProfileRequestService profileRequestService) {
		this.userDAO = userDAO;
		this.eventDAO = eventDAO;
		this.qualificationsDAO = qualificationsDAO;
		this.achievementDAO = achievementDAO;
		this.requestDAO = requestDAO;
		this.profileRequestService = profileRequestService;
	}

	@GetMapping
	@Operation(summary = "Get current user's profile data", description = "Retrieves a comprehensive set of data for the authenticated user's profile page.")
	public ResponseEntity<ApiResponse> getMyProfile(@AuthenticationPrincipal SecurityUser securityUser) {
		User user = securityUser.getUser();
		Map<String, Object> profileData = new HashMap<>();
		profileData.put("user", user);
		profileData.put("eventHistory", eventDAO.getEventHistoryForUser(user.getId()));
		profileData.put("qualifications", qualificationsDAO.getQualificationsForUser(user.getId()));
		profileData.put("achievements", achievementDAO.getAchievementsForUser(user.getId()));
		profileData.put("passkeys", Collections.emptyList()); 
		profileData.put("hasPendingRequest", requestDAO.hasPendingRequest(user.getId()));

		return ResponseEntity.ok(new ApiResponse(true, "Profildaten erfolgreich abgerufen.", profileData));
	}

	@GetMapping("/{userId}")
	@Operation(summary = "Get another user's public profile data", description = "Retrieves a public-safe subset of another user's profile data.")
	public ResponseEntity<ApiResponse> getUserProfile(@PathVariable int userId) {
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

	@PostMapping("/request-change")
	@Operation(summary = "Request a profile data change", description = "Submits a request for an administrator to approve changes to the user's profile data.")
	public ResponseEntity<ApiResponse> requestProfileChange(@Valid @RequestBody ProfileChangeRequestDTO requestDTO,
			@AuthenticationPrincipal SecurityUser securityUser) {
		try {
			profileRequestService.createChangeRequest(securityUser.getUser(), requestDTO);
			return ResponseEntity.ok(new ApiResponse(true, "Änderungsantrag erfolgreich eingereicht.", null));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
					new ApiResponse(false, "Ihr Antrag konnte nicht gespeichert werden: " + e.getMessage(), null));
		}
	}

	@PostMapping("/register-device")
	@Operation(summary = "Register a device token for push notifications")
	public ResponseEntity<ApiResponse> registerDevice(@RequestBody Map<String, String> payload,
			@AuthenticationPrincipal SecurityUser securityUser) {
		String token = payload.get("token");
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
		User user = securityUser.getUser();
		if (userDAO.validateUser(user.getUsername(), request.currentPassword()) == null) {
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
		if (userDAO.updateDashboardLayout(user.getId(), layoutJson)) {
			User updatedUser = userDAO.getUserById(user.getId());
			return ResponseEntity.ok(new ApiResponse(true, "Layout-Einstellungen gespeichert.", updatedUser));
		}
		return ResponseEntity.internalServerError()
				.body(new ApiResponse(false, "Layout konnte nicht gespeichert werden.", null));
	}
}