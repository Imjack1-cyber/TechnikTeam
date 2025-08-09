package de.technikteam.api.v1.auth;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.NavigationItem;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AuthService;
import de.technikteam.service.LoginAttemptService;
import de.technikteam.dao.UserDAO;
import de.technikteam.util.NavigationRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication.")
public class AuthResource {
	private static final Logger logger = LogManager.getLogger(AuthResource.class);

	private final UserDAO userDAO;
	private final AuthService authService;
	private final LoginAttemptService loginAttemptService;

	@Autowired
	public AuthResource(UserDAO userDAO, AuthService authService, LoginAttemptService loginAttemptService) {
		this.userDAO = userDAO;
		this.authService = authService;
		this.loginAttemptService = loginAttemptService;
	}

	@PostMapping("/login")
	@Operation(summary = "User Login", description = "Authenticates a user with username and password. On success, it sets an HttpOnly cookie with the JWT and returns user session data.", requestBody = @RequestBody(description = "User credentials for login.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginRequest.class))))
	public ResponseEntity<ApiResponse> login(
			@org.springframework.web.bind.annotation.RequestBody LoginRequest loginRequest, HttpServletRequest request,
			HttpServletResponse response) {
		String username = loginRequest.username();
		String password = loginRequest.password();
		String ipAddress = getClientIp(request);

		if (loginAttemptService.isLockedOut(username, ipAddress)) {
			logger.warn("Blocked login attempt for locked-out user '{}' from IP {}", username, ipAddress);
			return new ResponseEntity<>(new ApiResponse(false, "Konto ist vorübergehend gesperrt.", null),
					HttpStatus.FORBIDDEN);
		}

		User user = userDAO.validateUser(username, password);
		if (user != null) {
			loginAttemptService.clearLoginAttempts(username);
			authService.addJwtCookie(user, response);
			logger.info("JWT cookie set successfully for user '{}'", username);
			// Return user data but not the token itself
			return ResponseEntity.ok(new ApiResponse(true, "Anmeldung erfolgreich", user));
		} else {
			loginAttemptService.recordFailedLogin(username, ipAddress);
			logger.warn("Failed API login attempt for user '{}' from IP {}", username, ipAddress);
			return new ResponseEntity<>(new ApiResponse(false, "Falscher Benutzername oder Passwort.", null),
					HttpStatus.UNAUTHORIZED);
		}
	}

	@GetMapping("/csrf-token")
	@Operation(summary = "Get CSRF Token", description = "An endpoint that does nothing but allows the client to make a GET request to receive the initial XSRF-TOKEN cookie from the server.")
	public ResponseEntity<ApiResponse> getCsrfToken(HttpServletRequest request) {
		// By accessing the token, we ensure it's generated and added to the response.
		CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
		if (csrfToken != null) {
			logger.info("CSRF token explicitly requested and provided.");
		}
		return ResponseEntity.ok(new ApiResponse(true, "CSRF token provided in cookie.", null));
	}

	@GetMapping("/me")
	@Operation(summary = "Get current user session", description = "Retrieves the user object and navigation items for the currently authenticated user.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<ApiResponse> getCurrentUser(@AuthenticationPrincipal SecurityUser securityUser,
			HttpServletRequest request) {
		// Explicitly load the CSRF token to ensure the XSRF-TOKEN cookie is set on the
		// response
		// for the very first authenticated GET request the client makes.
		CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
		logger.info("CSRF token loaded during /me request: {}", csrfToken != null ? "OK" : "NULL");

		if (securityUser == null) {
			return new ResponseEntity<>(new ApiResponse(false, "Keine aktive Sitzung gefunden.", null),
					HttpStatus.UNAUTHORIZED);
		}

		User authenticatedUser = userDAO.getUserById(securityUser.getUser().getId()); // Re-fetch to get latest data
																						// including count
		List<NavigationItem> navigationItems = NavigationRegistry.getNavigationItemsForUser(authenticatedUser);
		Map<String, Object> responseData = Map.of("user", authenticatedUser, "navigation", navigationItems);
		return ResponseEntity.ok(new ApiResponse(true, "Current user session retrieved.", responseData));
	}

	@PostMapping("/logout")
	@Operation(summary = "User Logout", description = "Logs out the user by clearing the JWT authentication cookie.")
	public ResponseEntity<ApiResponse> logout(HttpServletResponse response) {
		authService.clearJwtCookie(response);
		return ResponseEntity.ok(new ApiResponse(true, "Abmeldung erfolgreich", null));
	}

	private String getClientIp(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader == null || xfHeader.isEmpty()) {
			return request.getRemoteAddr();
		}
		return xfHeader.split(",")[0];
	}
}