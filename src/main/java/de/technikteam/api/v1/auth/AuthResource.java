package de.technikteam.api.v1.auth;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.AuthService;
import de.technikteam.service.LoginAttemptService;
import de.technikteam.dao.UserDAO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
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
    @Operation(
        summary = "User Login",
        description = "Authenticates a user with username and password, returning a JWT if successful.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User credentials for login.",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = "object",
                    requiredProperties = {"username", "password"},
                    properties = {
                        @Schema(name = "username", type = "string", example = "admin"),
                        @Schema(name = "password", type = "string", format = "password", example = "admin123")
                    }
                )
            )
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful, JWT returned."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Account is temporarily locked.")
        }
    )
    public ResponseEntity<ApiResponse> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Username and password are required.", null));
        }

        if (loginAttemptService.isLockedOut(username)) {
            logger.warn("Blocked login attempt for locked-out user '{}'", username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "Account is temporarily locked.", null));
        }

        User user = userDAO.validateUser(username, password);
        if (user != null) {
            loginAttemptService.clearLoginAttempts(username);
            String token = authService.generateToken(user);
            logger.info("JWT generated successfully for user '{}'", username);
            return ResponseEntity.ok(new ApiResponse(true, "Login successful", Map.of("token", token)));
        } else {
            loginAttemptService.recordFailedLogin(username);
            logger.warn("Failed API login attempt for user '{}'", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "Invalid credentials.", null));
        }
    }
}