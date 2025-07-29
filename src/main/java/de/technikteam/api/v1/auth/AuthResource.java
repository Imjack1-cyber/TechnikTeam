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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @Operation(
        summary = "User Login",
        description = "Authenticates a user with username and password, returning a JWT if successful.",
        requestBody = @RequestBody(
            description = "User credentials for login.",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginRequest.class)
            )
        )
    )
    public ResponseEntity<ApiResponse> login(@org.springframework.web.bind.annotation.RequestBody LoginRequest loginRequest) {
        String username = loginRequest.username();
        String password = loginRequest.password();

        if (loginAttemptService.isLockedOut(username)) {
            logger.warn("Blocked login attempt for locked-out user '{}'", username);
            return new ResponseEntity<>(new ApiResponse(false, "Account is temporarily locked.", null), HttpStatus.FORBIDDEN);
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
            return new ResponseEntity<>(new ApiResponse(false, "Invalid credentials.", null), HttpStatus.UNAUTHORIZED);
        }
    }
}