package de.technikteam.api.v1.public_api;

import com.google.gson.Gson;
import de.technikteam.api.v1.dto.PasswordChangeRequest;
import de.technikteam.api.v1.dto.ProfileChangeRequestDTO;
import de.technikteam.dao.*;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.ProfileChangeRequest;
import de.technikteam.model.User;
import de.technikteam.service.ProfileRequestService;
import de.technikteam.util.PasswordPolicyValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/public/profile")
@Tag(name = "Public Profile", description = "Endpoints for managing the current user's profile.")
@SecurityRequirement(name = "bearerAuth")
public class PublicProfileResource {

	private final UserDAO userDAO;
	private final EventDAO eventDAO;
	private final UserQualificationsDAO qualificationsDAO;
	private final AchievementDAO achievementDAO;
	private final PasskeyDAO passkeyDAO;
	private final ProfileChangeRequestDAO requestDAO;
	private final ProfileRequestService profileRequestService;
	private final Gson gson;

	@Autowired
	public PublicProfileResource(UserDAO userDAO, EventDAO eventDAO, UserQualificationsDAO qualificationsDAO,
			AchievementDAO achievementDAO, PasskeyDAO passkeyDAO, ProfileChangeRequestDAO requestDAO,
			ProfileRequestService profileRequestService, Gson gson) {
		this.userDAO = userDAO;
		this.eventDAO = eventDAO;
		this.qualificationsDAO = qualificationsDAO;
		this.achievementDAO = achievementDAO;
		this.passkeyDAO = passkeyDAO;
		this.requestDAO = requestDAO;
		this.profileRequestService = profileRequestService;
		this.gson = gson;
	}

	@GetMapping
	@Operation(summary = "Get current user's profile data", description = "Retrieves a comprehensive set of data for the authenticated user's profile page.")
	public ResponseEntity<ApiResponse> getMyProfile(@AuthenticationPrincipal User user) {
		Map<String, Object> profileData = new HashMap<>();
		profileData.put("user", user);
		profileData.put("eventHistory", eventDAO.getEventHistoryForUser(user.getId()));
		profileData.put("qualifications", qualificationsDAO.getQualificationsForUser(user.getId()));
		profileData.put("achievements", achievementDAO.getAchievementsForUser(user.getId()));
		profileData.put("passkeys", passkeyDAO.getCredentialsByUserId(user.getId()));
		profileData.put("hasPendingRequest", requestDAO.hasPendingRequest(user.getId()));

		return ResponseEntity.ok(new ApiResponse(true, "Profile data retrieved.", profileData));
	}

	@PostMapping("/request-change")
	@Operation(summary = "Request a profile data change", description = "Submits a request for an administrator to approve changes to the user's profile data, including an optional profile picture.")
	public ResponseEntity<ApiResponse> requestProfileChange(
			@RequestPart("profileData") @Valid ProfileChangeRequestDTO requestDTO,
			@RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
			@AuthenticationPrincipal User currentUser) {
		try {
			profileRequestService.createChangeRequest(currentUser, requestDTO, profilePicture);
			return ResponseEntity.ok(new ApiResponse(true, "Change request submitted successfully.", null));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Could not save your request: " + e.getMessage(), null));
		}
	}

	@PutMapping("/theme")
	@Operation(summary = "Update user theme", description = "Updates the user's preferred theme (light/dark).")
	public ResponseEntity<ApiResponse> updateUserTheme(@RequestBody Map<String, String> payload,
			@AuthenticationPrincipal User user) {
		String theme = payload.get("theme");
		if (theme != null && (theme.equals("light") || theme.equals("dark"))) {
			if (userDAO.updateUserTheme(user.getId(), theme)) {
				return ResponseEntity.ok(new ApiResponse(true, "Theme updated.", null));
			}
		}
		return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid theme specified.", null));
	}

	@PutMapping("/chat-color")
	@Operation(summary = "Update chat color", description = "Updates the user's preferred color for chat messages.")
	public ResponseEntity<ApiResponse> updateChatColor(@RequestBody Map<String, String> payload,
			@AuthenticationPrincipal User user) {
		String chatColor = payload.get("chatColor");
		if (userDAO.updateUserChatColor(user.getId(), chatColor)) {
			return ResponseEntity.ok(new ApiResponse(true, "Chat color updated.", null));
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Could not save chat color.", null));
		}
	}

	@PutMapping("/password")
	@Operation(summary = "Change password", description = "Allows the authenticated user to change their own password after verifying their current one.")
	public ResponseEntity<ApiResponse> updatePassword(@Valid @RequestBody PasswordChangeRequest request,
			@AuthenticationPrincipal User user) {
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

	@DeleteMapping("/passkeys/{id}")
	@Operation(summary = "Delete a passkey", description = "Removes a registered passkey/credential for the current user.")
	public ResponseEntity<ApiResponse> deletePasskey(
			@Parameter(description = "The database ID of the passkey credential to delete") @PathVariable int id,
			@AuthenticationPrincipal User user) {
		if (passkeyDAO.deleteCredential(id, user.getId())) {
			return ResponseEntity.ok(new ApiResponse(true, "Passkey successfully removed.", null));
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Could not remove passkey.", null));
		}
	}
}