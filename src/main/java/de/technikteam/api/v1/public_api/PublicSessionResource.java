package de.technikteam.api.v1.public_api;

import de.technikteam.dao.AuthenticationLogDAO;
import de.technikteam.dao.TwoFactorAuthDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.AuthenticationLog;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AuthService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/sessions")
@Tag(name = "Public Sessions", description = "Endpoints for managing user sessions.")
public class PublicSessionResource {

    private final AuthenticationLogDAO authLogDAO;
    private final TwoFactorAuthDAO twoFactorAuthDAO;
    private final AuthService authService;

    @Autowired
    public PublicSessionResource(AuthenticationLogDAO authLogDAO, TwoFactorAuthDAO twoFactorAuthDAO, AuthService authService) {
        this.authLogDAO = authLogDAO;
        this.twoFactorAuthDAO = twoFactorAuthDAO;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Get all active sessions for the current user")
    public ResponseEntity<ApiResponse> getActiveSessions(@AuthenticationPrincipal SecurityUser securityUser) {
        List<AuthenticationLog> activeSessions = authLogDAO.getActiveSessionsForUser(securityUser.getUser().getId());
        return ResponseEntity.ok(new ApiResponse(true, "Active sessions retrieved.", activeSessions));
    }

    @PostMapping("/{logId}/name")
    @Operation(summary = "Name a device/session")
    public ResponseEntity<ApiResponse> nameDevice(@PathVariable long logId, @RequestBody Map<String, String> payload, @AuthenticationPrincipal SecurityUser securityUser) {
        AuthenticationLog log = authLogDAO.getLogById(logId);
        if (log == null || !log.getUserId().equals(securityUser.getUser().getId())) {
            return ResponseEntity.status(403).body(new ApiResponse(false, "Permission denied.", null));
        }

        String deviceName = payload.get("deviceName");
        if (deviceName == null || deviceName.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Device name cannot be empty.", null));
        }

        twoFactorAuthDAO.nameKnownIp(securityUser.getUser().getId(), log.getIpAddress(), deviceName);
        return ResponseEntity.ok(new ApiResponse(true, "Device named successfully.", null));
    }

    @PostMapping("/{jti}/revoke")
    @Operation(summary = "Revoke a specific session")
    public ResponseEntity<ApiResponse> revokeSession(@PathVariable String jti, @AuthenticationPrincipal SecurityUser securityUser) {
        AuthenticationLog logToRevoke = authLogDAO.getLogByJti(jti);
        if (logToRevoke == null || !logToRevoke.getUserId().equals(securityUser.getUser().getId())) {
            return ResponseEntity.status(403).body(new ApiResponse(false, "Permission denied to revoke this session.", null));
        }
        
        if (authService.isTokenRevoked(jti)) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Diese Sitzung wurde bereits widerrufen.", null));
        }

        authService.revokeToken(jti);
        return ResponseEntity.ok(new ApiResponse(true, "Session revoked successfully.", null));
    }

    @PostMapping("/revoke-all")
    @Operation(summary = "Revoke all other sessions for the current user")
    public ResponseEntity<ApiResponse> revokeAllOtherSessions(@AuthenticationPrincipal SecurityUser securityUser) {
        String currentTokenJti = securityUser.getUser().getJti(); // Assuming JTI is added to SecurityUser
        List<AuthenticationLog> activeSessions = authLogDAO.getActiveSessionsForUser(securityUser.getUser().getId());
        
        int revokedCount = 0;
        for (AuthenticationLog session : activeSessions) {
            if (!session.getJti().equals(currentTokenJti)) {
                authService.revokeToken(session.getJti());
                revokedCount++;
            }
        }
        
        return ResponseEntity.ok(new ApiResponse(true, revokedCount + " other sessions have been revoked.", null));
    }
}