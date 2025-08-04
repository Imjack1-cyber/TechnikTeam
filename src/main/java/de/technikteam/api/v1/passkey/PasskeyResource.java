package de.technikteam.api.v1.passkey;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import de.technikteam.api.v1.dto.passkey.FinishLoginRequest;
import de.technikteam.api.v1.dto.passkey.FinishRegistrationRequest;
import de.technikteam.api.v1.dto.passkey.StartLoginRequest;
import de.technikteam.dao.PasskeyDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AuthService;
import de.technikteam.service.ChallengeCacheService;
import de.technikteam.service.PasskeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/passkey")
@Tag(name = "Passkey Authentication", description = "Endpoints for WebAuthn/Passkey registration and login.")
public class PasskeyResource {

	private final PasskeyService passkeyService;
	private final PasskeyDAO passkeyDAO;
	private final AuthService authService;
	private final ChallengeCacheService challengeCache;

	@Autowired
	public PasskeyResource(PasskeyService passkeyService, PasskeyDAO passkeyDAO, AuthService authService,
			ChallengeCacheService challengeCache) {
		this.passkeyService = passkeyService;
		this.passkeyDAO = passkeyDAO;
		this.authService = authService;
		this.challengeCache = challengeCache;
	}

	private String getRegistrationCacheKey(int userId) {
		return "reg-" + userId;
	}

	private String getLoginCacheKey(String credentialId) {
		return "login-" + credentialId;
	}

	@PostMapping("/register/start")
	@Operation(summary = "Start passkey registration", description = "Initiates the registration ceremony for a new passkey.")
	public ResponseEntity<ApiResponse> startRegistration(@AuthenticationPrincipal SecurityUser securityUser) {
		PublicKeyCredentialCreationOptions options = passkeyService.startRegistration(securityUser.getUser());
		challengeCache.put(getRegistrationCacheKey(securityUser.getUser().getId()), options);
		return ResponseEntity.ok(new ApiResponse(true, "Registration started.", options));
	}

	@PostMapping("/register/finish")
	@Operation(summary = "Finish passkey registration", description = "Completes the registration ceremony for a new passkey.")
	public ResponseEntity<ApiResponse> finishRegistration(@AuthenticationPrincipal SecurityUser securityUser,
			@Valid @RequestBody FinishRegistrationRequest registrationRequest) {
		String cacheKey = getRegistrationCacheKey(securityUser.getUser().getId());
		PublicKeyCredentialCreationOptions registrationOptions = challengeCache
				.get(cacheKey, PublicKeyCredentialCreationOptions.class).orElse(null);

		if (registrationOptions == null) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "No registration in progress. Please start again.", null));
		}
		challengeCache.remove(cacheKey);

		try {
			boolean success = passkeyService.finishRegistration(registrationRequest.credential(),
					registrationRequest.deviceName(), securityUser.getUser(), registrationOptions);
			if (success) {
				return ResponseEntity.ok(new ApiResponse(true, "Passkey registered successfully.", null));
			} else {
				return ResponseEntity.badRequest().body(new ApiResponse(false, "Passkey registration failed.", null));
			}
		} catch (RegistrationFailedException | IOException e) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "Registration failed: " + e.getMessage(), null));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "An unexpected error occurred.", null));
		}
	}

	@PostMapping("/login/start")
	@Operation(summary = "Start passkey login", description = "Initiates the authentication ceremony for a passkey login.")
	public ResponseEntity<ApiResponse> startLogin(@Valid @RequestBody StartLoginRequest loginRequest) {
		AssertionRequest assertionRequest = passkeyService.startAuthentication(loginRequest.username());
		// For login, we don't know the credential ID yet, so we cache by username for
		// now.
		challengeCache.put("login-" + loginRequest.username(), assertionRequest);
		return ResponseEntity.ok(new ApiResponse(true, "Authentication started.",
				assertionRequest.getPublicKeyCredentialRequestOptions()));
	}

	@PostMapping("/login/finish")
	@Operation(summary = "Finish passkey login", description = "Completes the authentication ceremony for a passkey login.")
	public ResponseEntity<ApiResponse> finishLogin(@Valid @RequestBody FinishLoginRequest loginRequest,
			HttpServletResponse response) {
		String username;
		try {
			username = passkeyService.getUsernameFromLoginCredential(loginRequest.credential());
		} catch (IOException e) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid credential format.", null));
		}

		if (username == null) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "Could not determine username from credential.", null));
		}

		String cacheKey = "login-" + username;
		AssertionRequest assertionRequest = challengeCache.get(cacheKey, AssertionRequest.class).orElse(null);

		if (assertionRequest == null) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "No login in progress or session expired. Please start again.", null));
		}
		challengeCache.remove(cacheKey);

		try {
			User user = passkeyService.finishAuthentication(loginRequest.credential(), assertionRequest);
			if (user != null) {
				authService.addJwtCookie(user, response);
				return ResponseEntity.ok(new ApiResponse(true, "Login successful.", user));
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(new ApiResponse(false, "Passkey authentication failed.", null));
			}
		} catch (AssertionFailedException | IOException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new ApiResponse(false, "Authentication failed: " + e.getMessage(), null));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "An unexpected error occurred.", null));
		}
	}

	@DeleteMapping("/credentials/{id}")
	@Operation(summary = "Delete a passkey credential", description = "Removes a registered passkey from the current user's account.")
	public ResponseEntity<ApiResponse> deleteCredential(@AuthenticationPrincipal SecurityUser securityUser,
			@PathVariable int id) {
		if (passkeyDAO.deleteCredential(id, securityUser.getUser().getId())) {
			return ResponseEntity.ok(new ApiResponse(true, "Passkey deleted successfully.", null));
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
					new ApiResponse(false, "Credential not found or you do not have permission to delete it.", null));
		}
	}
}