package de.technikteam.api.v1.auth;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.AuthService;
import de.technikteam.service.LoginAttemptService;
import de.technikteam.dao.UserDAO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
			return new ResponseEntity<>(new ApiResponse(false, "Account is temporarily locked.", null),
					HttpStatus.FORBIDDEN);
		}

		User user = userDAO.validateUser(username, password);
		if (user != null) {
			loginAttemptService.clearLoginAttempts(ipAddress);
			authService.addJwtCookie(user, response);
			logger.info("JWT cookie set successfully for user '{}'", username);
			// Return user data but not the token itself
			return ResponseEntity.ok(new ApiResponse(true, "Login successful", user));
		} else {
			loginAttemptService.recordFailedLogin(username, ipAddress);
			logger.warn("Failed API login attempt for user '{}' from IP {}", username, ipAddress);
			return new ResponseEntity<>(new ApiResponse(false, "Invalid credentials.", null), HttpStatus.UNAUTHORIZED);
		}
	}

	@PostMapping("/logout")
	@Operation(summary = "User Logout", description = "Logs out the user by clearing the JWT authentication cookie.")
	public ResponseEntity<ApiResponse> logout(HttpServletResponse response) {
		authService.clearJwtCookie(response);
		return ResponseEntity.ok(new ApiResponse(true, "Logout successful", null));
	}

	private String getClientIp(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader == null || xfHeader.isEmpty()) {
			return request.getRemoteAddr();
		}
		return xfHeader.split(",")[0];
	}
}