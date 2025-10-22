package de.technikteam.api.v1.auth;

import de.technikteam.api.v1.dto.PasswordResetFinalizeRequest;
import de.technikteam.api.v1.dto.PasswordResetInitiateRequest;
import de.technikteam.dao.IdentityVerificationDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.IdentityVerificationRequest;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.IdentityVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/identity-verification")
@Tag(name = "Identity Verification", description = "Endpoints for app-based identity verification flows.")
public class IdentityVerificationResource {

    private final IdentityVerificationService verificationService;
    private final UserDAO userDAO;
    private final IdentityVerificationDAO identityVerificationDAO;

    @Autowired
    public IdentityVerificationResource(IdentityVerificationService verificationService, UserDAO userDAO, IdentityVerificationDAO identityVerificationDAO) {
        this.verificationService = verificationService;
        this.userDAO = userDAO;
        this.identityVerificationDAO = identityVerificationDAO;
    }

    @PostMapping("/initiate-password-reset")
    @Operation(summary = "Initiate a password reset via push notification")
    public ResponseEntity<ApiResponse> initiatePasswordReset(@Valid @RequestBody PasswordResetInitiateRequest request) {
        User user = userDAO.getUserByUsername(request.usernameOrEmail());
        if (user == null) {
            // Also check by email
            user = userDAO.getAllUsers().stream().filter(u -> request.usernameOrEmail().equalsIgnoreCase(u.getEmail())).findFirst().orElse(null);
        }

        if (user == null) {
            // Fail silently to prevent user enumeration ONLY IF USER DOES NOT EXIST.
            return ResponseEntity.ok(new ApiResponse(true, "If an account with this email/username exists, instructions have been sent.", null));
        }

        // If the user exists, always create the request. The service will handle whether to send a push notification.
        try {
            String token = verificationService.initiateRequest(user, "PASSWORD_RESET", null);
            return ResponseEntity.ok(new ApiResponse(true, "If an account with a registered device exists, a notification has been sent. Otherwise, an administrator has been notified.", Map.of("challengeToken", token)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/status/{token}")
    @Operation(summary = "Check the status of a verification request")
    public ResponseEntity<ApiResponse> checkVerificationStatus(@PathVariable String token) {
        String status = verificationService.checkStatus(token);
        return ResponseEntity.ok(new ApiResponse(true, "Status retrieved.", status));
    }

    @GetMapping("/details/{token}")
    @Operation(summary = "Get details of a verification request")
    public ResponseEntity<ApiResponse> getRequestDetails(@PathVariable String token) {
        IdentityVerificationRequest request = verificationService.getRequestDetails(token);
        if(request == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ApiResponse(true, "Details retrieved", request));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Finalize password reset with an approved token")
    public ResponseEntity<ApiResponse> resetPasswordWithToken(@Valid @RequestBody PasswordResetFinalizeRequest request) {
        try {
            verificationService.finalizePasswordReset(request.token(), request.newPassword());
            return ResponseEntity.ok(new ApiResponse(true, "Password has been successfully reset.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/pending-resets")
    @Operation(summary = "Get all pending password reset requests (Admin)")
    @PreAuthorize("hasAuthority('USER_PASSWORD_RESET')")
    public ResponseEntity<ApiResponse> getPendingPasswordResets() {
        List<IdentityVerificationRequest> requests = verificationService.getPendingPasswordResets();
        return ResponseEntity.ok(new ApiResponse(true, "Pending password resets retrieved.", requests));
    }

    @PostMapping("/{token}/complete")
    @Operation(summary = "Mark a password reset request as completed by an admin")
    @PreAuthorize("hasAuthority('USER_PASSWORD_RESET')")
    public ResponseEntity<ApiResponse> markAsCompleted(@PathVariable String token, @AuthenticationPrincipal SecurityUser securityUser) {
        try {
            verificationService.markAsCompleted(token, securityUser.getUser());
            return ResponseEntity.ok(new ApiResponse(true, "Request marked as completed.", null));
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}