package de.technikteam.api.v1;

import de.technikteam.dao.AuthenticationLogDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.AuthenticationLog;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.AuthService;
import de.technikteam.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/auth-log")
@Tag(name = "Admin Authentication Log", description = "Endpoints for viewing authentication history.")
@SecurityRequirement(name = "bearerAuth")
public class AdminAuthLogResource {

    private static final Logger logger = LogManager.getLogger(AdminAuthLogResource.class);
    private final AuthenticationLogDAO authLogDAO;
    private final AuthService authService;
    private final AdminLogService adminLogService;
    private final NotificationService notificationService;

    @Autowired
    public AdminAuthLogResource(AuthenticationLogDAO authLogDAO, AuthService authService, AdminLogService adminLogService, NotificationService notificationService) {
        this.authLogDAO = authLogDAO;
        this.authService = authService;
        this.adminLogService = adminLogService;
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get recent authentication logs")
    public ResponseEntity<ApiResponse> getAuthLogs(@RequestParam(defaultValue = "100") int limit) {
        logger.debug("Request received to get auth logs with limit: {}", limit);
        List<AuthenticationLog> logs = authLogDAO.getLogs(limit);
        logger.debug("Returning {} auth log entries.", logs.size());
        return ResponseEntity.ok(new ApiResponse(true, "Authentication logs retrieved successfully.", logs));
    }

    @PostMapping("/revoke-session")
    @Operation(summary = "Revoke a user session via JWT ID")
    public ResponseEntity<ApiResponse> revokeSession(@RequestBody Map<String, String> payload,
            @AuthenticationPrincipal SecurityUser securityUser) {
        String jti = payload.get("jti");
        logger.info("Request received from admin '{}' to revoke session with JTI: {}", securityUser.getUsername(), jti);
        if (jti == null || jti.isBlank()) {
            logger.warn("Revoke session request failed: JTI is missing.");
            return ResponseEntity.badRequest().body(new ApiResponse(false, "JWT ID (jti) is required.", null));
        }

        if (authService.isTokenRevoked(jti)) {
            logger.warn("Attempted to revoke an already-revoked session with JTI: {}", jti);
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Diese Sitzung wurde bereits widerrufen.", null));
        }

        AuthenticationLog logToRevoke = authLogDAO.getLogByJti(jti);
        authService.revokeToken(jti);

        if (logToRevoke != null) {
            String details = String.format("Session for user '%s' (IP: %s, Login: %s) revoked.",
                    logToRevoke.getUsername(), logToRevoke.getIpAddress(), logToRevoke.getTimestamp());
            adminLogService.log(securityUser.getUsername(), "SESSION_REVOKE", details);
            notificationService.broadcastUIUpdate("AUTH_LOG", "UPDATED", logToRevoke);
        }

        logger.info("Session with JTI '{}' was successfully added to the blocklist.", jti);
        return ResponseEntity.ok(new ApiResponse(true, "Session successfully revoked. The user will be logged out on their next action.", null));
    }
}