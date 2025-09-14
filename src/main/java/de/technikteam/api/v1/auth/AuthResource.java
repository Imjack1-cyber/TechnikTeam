package de.technikteam.api.v1.auth;

import de.technikteam.api.v1.dto.TwoFactorVerificationRequest;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.NavigationItem;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.security.UserSuspendedException;
import de.technikteam.service.*;
import de.technikteam.dao.TwoFactorAuthDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.util.NavigationRegistry;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

import java.time.LocalDateTime;
import java.util.HashMap;
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
	private final AuthenticationLogService authLogService;
	private final SystemSettingsService settingsService;
	private final TwoFactorAuthDAO twoFactorAuthDAO;
	private final TwoFactorAuthService twoFactorAuthService;
	private final GeoIpService geoIpService;
	private final UserAgentService userAgentService;
	private final PrivacyPolicyService privacyPolicyService;

	@Autowired
	public AuthResource(UserDAO userDAO, AuthService authService, LoginAttemptService loginAttemptService,
			AuthenticationLogService authLogService, SystemSettingsService settingsService,
			TwoFactorAuthDAO twoFactorAuthDAO, TwoFactorAuthService twoFactorAuthService,
			GeoIpService geoIpService, UserAgentService userAgentService, PrivacyPolicyService privacyPolicyService) {
		this.userDAO = userDAO;
		this.authService = authService;
		this.loginAttemptService = loginAttemptService;
		this.authLogService = authLogService;
		this.settingsService = settingsService;
		this.twoFactorAuthDAO = twoFactorAuthDAO;
		this.twoFactorAuthService = twoFactorAuthService;
		this.geoIpService = geoIpService;
		this.userAgentService = userAgentService;
		this.privacyPolicyService = privacyPolicyService;
	}

	@PostMapping("/login")
	@Operation(summary = "User Login", description = "Authenticates a user with username and password. On success, it sets an HttpOnly cookie with the JWT and returns user session data.", requestBody = @RequestBody(description = "User credentials for login.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginRequest.class))))
	public ResponseEntity<ApiResponse> login(
			@Valid @org.springframework.web.bind.annotation.RequestBody LoginRequest loginRequest, HttpServletRequest request,
			HttpServletResponse response) {
		String username = loginRequest.username();
		String password = loginRequest.password();
        String clientType = loginRequest.clientType() != null ? loginRequest.clientType() : "web";
		String ipAddress = getClientIp(request);
		String userAgent = request.getHeader("User-Agent");
		logger.info("Login attempt for user '{}' from IP address: {} (Client: {})", username, ipAddress, clientType);

		if (geoIpService.isIpBlocked(ipAddress)) {
			logger.warn("Blocked login attempt for user '{}' from blocked country. IP: {}", username, ipAddress);
			return new ResponseEntity<>(new ApiResponse(false, "Zugriff von Ihrem Standort aus verweigert.", null),
					HttpStatus.FORBIDDEN);
		}

		if (loginAttemptService.isLockedOut(username, ipAddress)) {
			logger.warn("Blocked login attempt for locked-out user '{}' from IP {}", username, ipAddress);
			long remainingSeconds = loginAttemptService.getRemainingLockoutSeconds(username);
			String message = String.format(
					"Konto ist vorübergehend gesperrt. Bitte versuchen Sie es in %d Sekunden erneut.",
					remainingSeconds);
			return new ResponseEntity<>(new ApiResponse(false, message, null), HttpStatus.FORBIDDEN);
		}

		try {
			User user = userDAO.validateUser(username, password);
			if (user != null) {
				loginAttemptService.clearLoginAttempts(username);

				// Risk-Based 2FA Check
				boolean needs2fa = false;
				if (user.isTotpEnabled()) {
					boolean isKnownIp = twoFactorAuthDAO.isIpKnownForUser(user.getId(), ipAddress);
					LocalDateTime lastLoginTime = authLogService.getTimestampOfLastLogin(user.getId());

					if (!isKnownIp) {
						needs2fa = true;
						logger.info("2FA required for user '{}': Unrecognized IP subnet.", username);
					} else if (lastLoginTime != null && lastLoginTime.isBefore(LocalDateTime.now().minusDays(30))) {
						needs2fa = true;
						logger.info("2FA required for user '{}': Inactivity period exceeded.", username);
					}
				}

				if (needs2fa) {
					String preAuthToken = authService.generatePreAuthToken(user.getId());
					return ResponseEntity.ok(new ApiResponse(true, "2FA_REQUIRED", Map.of("token", preAuthToken)));
				}

				// If no 2FA is needed, proceed with full login
				twoFactorAuthDAO.addKnownIpForUser(user.getId(), ipAddress);
                long tokenLifetime = "native".equalsIgnoreCase(clientType) ? (336 * 60 * 60) : (8 * 60 * 60);
				String token = authService.generateToken(user, tokenLifetime);
                
                if ("web".equalsIgnoreCase(clientType)) {
				    authService.addJwtCookie(user, response);
				    logger.info("JWT cookie set successfully for user '{}'", username);
                }

				Claims claims = authService.parseTokenClaims(token);
				Map<String, String> agentDetails = userAgentService.parseUserAgent(userAgent);
				String countryCode = geoIpService.getCountryCode(ipAddress);
				authLogService.logLoginSuccess(user.getId(), username, ipAddress, claims.getId(),
						claims.getExpiration().toInstant(), userAgent, agentDetails.get("deviceType"), countryCode);

				List<NavigationItem> navigationItems = NavigationRegistry.getNavigationItemsForUser(user);
				Map<String, Object> sessionData = new HashMap<>();
				sessionData.put("user", user);
				sessionData.put("navigation", navigationItems);
				sessionData.put("previousLogin", authLogService.getPreviousLoginInfo(user.getId()));
				sessionData.put("maintenanceStatus", settingsService.getMaintenanceStatus());

				Map<String, Object> responseData = new HashMap<>();
				responseData.put("session", sessionData);
				responseData.put("token", token);

				return ResponseEntity.ok(new ApiResponse(true, "Anmeldung erfolgreich", responseData));
			} else {
				loginAttemptService.recordFailedLogin(username, ipAddress);
				authLogService.logLoginFailure(username, ipAddress);
				logger.warn("Failed API login attempt for user '{}' from IP {}", username, ipAddress);
				return new ResponseEntity<>(new ApiResponse(false, "Falscher Benutzername oder Passwort.", null),
						HttpStatus.UNAUTHORIZED);
			}
		} catch (UserSuspendedException e) {
			return new ResponseEntity<>(new ApiResponse(false, e.getMessage(), null), HttpStatus.FORBIDDEN);
		}
	}

	@PostMapping("/verify-2fa")
	@Operation(summary = "Verify 2FA Token", description = "Verifies a TOTP or backup code to complete a login attempt from an unknown location.")
	public ResponseEntity<ApiResponse> verifyTwoFactor(@Valid @org.springframework.web.bind.annotation.RequestBody TwoFactorVerificationRequest verificationRequest,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			User user = authService.validatePreAuthTokenAndGetUser(verificationRequest.preAuthToken());
			if (user == null) {
				return new ResponseEntity<>(new ApiResponse(false, "Invalid or expired pre-authentication token.", null), HttpStatus.FORBIDDEN);
			}
			String ipAddress = getClientIp(request);
			String userAgent = request.getHeader("User-Agent");

			boolean isValid = false;
			if (verificationRequest.backupCode() != null && !verificationRequest.backupCode().isBlank()) {
				isValid = twoFactorAuthService.verifyBackupCode(user.getId(), verificationRequest.backupCode());
			} else if (verificationRequest.token() != null && !verificationRequest.token().isBlank()) {
				String decryptedSecret = twoFactorAuthService.decrypt(user.getTotpSecret());
				isValid = twoFactorAuthService.verifyCode(decryptedSecret, verificationRequest.token());
			}

			if (isValid) {
				twoFactorAuthDAO.addKnownIpForUser(user.getId(), ipAddress);
				// Generate final, full-privilege token
				String finalToken = authService.generateToken(user);
				authService.addJwtCookie(user, response);

				// Log successful login with all details
				Claims claims = authService.parseTokenClaims(finalToken);
				Map<String, String> agentDetails = userAgentService.parseUserAgent(userAgent);
				String countryCode = geoIpService.getCountryCode(ipAddress);
				authLogService.logLoginSuccess(user.getId(), user.getUsername(), ipAddress, claims.getId(),
						claims.getExpiration().toInstant(), userAgent, agentDetails.get("deviceType"), countryCode);
                
                // Return full session data, same as non-2FA login path
                List<NavigationItem> navigationItems = NavigationRegistry.getNavigationItemsForUser(user);
				Map<String, Object> sessionData = new HashMap<>();
				sessionData.put("user", user);
				sessionData.put("navigation", navigationItems);
				sessionData.put("previousLogin", authLogService.getPreviousLoginInfo(user.getId()));
				sessionData.put("maintenanceStatus", settingsService.getMaintenanceStatus());

				Map<String, Object> responseData = new HashMap<>();
				responseData.put("session", sessionData);
				responseData.put("token", finalToken);

				return ResponseEntity.ok(new ApiResponse(true, "2FA verification successful.", responseData));
			} else {
				return new ResponseEntity<>(new ApiResponse(false, "Invalid code.", null), HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(new ApiResponse(false, "Verification failed: " + e.getMessage(), null), HttpStatus.FORBIDDEN);
		}
	}

	@GetMapping("/csrf-token")
	@Operation(summary = "Get CSRF Token", description = "An endpoint that does nothing but allows the client to make a GET request to receive the initial XSRF-TOKEN cookie from the server.")
	public ResponseEntity<ApiResponse> getCsrfToken(HttpServletRequest request) {
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
		CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
		logger.info("CSRF token loaded during /me request: {}", csrfToken != null ? "OK" : "NULL");

		if (securityUser == null) {
			return new ResponseEntity<>(new ApiResponse(false, "Keine aktive Sitzung gefunden.", null),
					HttpStatus.UNAUTHORIZED);
		}

		User authenticatedUser = userDAO.getUserById(securityUser.getUser().getId());
		List<NavigationItem> navigationItems = NavigationRegistry.getNavigationItemsForUser(authenticatedUser);
		Map<String, Object> responseData = new HashMap<>();
		responseData.put("user", authenticatedUser);
		responseData.put("navigation", navigationItems);
		responseData.put("maintenanceStatus", settingsService.getMaintenanceStatus());
		responseData.put("currentPolicyVersion", privacyPolicyService.getCurrentVersion());

		return ResponseEntity.ok(new ApiResponse(true, "Current user session retrieved.", responseData));
	}

	@PostMapping("/logout")
	@Operation(summary = "User Logout", description = "Logs out the user by clearing the JWT authentication cookie.")
	public ResponseEntity<ApiResponse> logout(@AuthenticationPrincipal SecurityUser securityUser,
			HttpServletRequest request, HttpServletResponse response) {
		if (securityUser != null) {
			authLogService.logLogout(securityUser.getUser().getId(), securityUser.getUsername(), getClientIp(request));
		}
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