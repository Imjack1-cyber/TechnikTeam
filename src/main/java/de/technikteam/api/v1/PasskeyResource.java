package de.technikteam.api.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.data.*;
import de.technikteam.api.v1.dto.PasskeyRegistrationFinishRequest;
import de.technikteam.dao.PasskeyDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.NavigationItem;
import de.technikteam.model.PasskeyCredential;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AuthService;
import de.technikteam.service.PasskeyService;
import de.technikteam.util.NavigationRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/passkeys")
@Tag(name = "Passkeys", description = "Endpoints for WebAuthn/Passkey authentication.")
public class PasskeyResource {

    private final PasskeyService passkeyService;
    private final PasskeyDAO passkeyDAO;
    private final AuthService authService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public PasskeyResource(PasskeyService passkeyService, PasskeyDAO passkeyDAO, AuthService authService) {
        this.passkeyService = passkeyService;
        this.passkeyDAO = passkeyDAO;
        this.authService = authService;
    }

    @PostMapping("/registration/start")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Start Passkey Registration")
    public ResponseEntity<ApiResponse> startRegistration(@AuthenticationPrincipal SecurityUser securityUser, HttpServletRequest request) {
        try {
            PublicKeyCredentialCreationOptions options = passkeyService.startRegistration(securityUser.getUser(), request.getSession());
            return ResponseEntity.ok(new ApiResponse(true, "Registration options generated.", options));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PostMapping("/registration/finish")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Finish Passkey Registration")
    public ResponseEntity<ApiResponse> finishRegistration(@Valid @RequestBody PasskeyRegistrationFinishRequest finishRequest, @AuthenticationPrincipal SecurityUser securityUser, HttpServletRequest request) {
        try {
            String credentialJson = objectMapper.writeValueAsString(finishRequest.credential());
            PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential = PublicKeyCredential.parseRegistrationResponseJson(credentialJson);
            
            passkeyService.finishRegistration(credential, request.getSession(), securityUser.getUser(), finishRequest.deviceName());
            
            return ResponseEntity.ok(new ApiResponse(true, "Passkey registered successfully.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Registration failed: " + e.getMessage(), null));
        }
    }

    @PostMapping("/authentication/start")
    @Operation(summary = "Start Passkey Authentication")
    public ResponseEntity<ApiResponse> startAuthentication(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String username = payload.get("username");
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Username is required.", null));
        }
        try {
            PublicKeyCredentialRequestOptions options = passkeyService.startAuthentication(username, request.getSession());
            return ResponseEntity.ok(new ApiResponse(true, "Authentication options generated.", options));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PostMapping("/authentication/finish")
    @Operation(summary = "Finish Passkey Authentication")
    public ResponseEntity<ApiResponse> finishAuthentication(@RequestBody String credentialJson, HttpServletRequest request, HttpServletResponse response) {
        try {
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential = PublicKeyCredential.parseAssertionResponseJson(credentialJson);
            User user = passkeyService.finishAuthentication(credential, request.getSession());

            if (user != null) {
                String token = authService.generateToken(user);
                authService.addJwtCookie(user, response);

                List<NavigationItem> navigationItems = NavigationRegistry.getNavigationItemsForUser(user);
                Map<String, Object> sessionData = Map.of("user", user, "navigation", navigationItems);
                Map<String, Object> responseData = Map.of("session", sessionData, "token", token);

                return ResponseEntity.ok(new ApiResponse(true, "Authentication successful.", responseData));
            } else {
                return new ResponseEntity<>(new ApiResponse(false, "Authentication failed.", null), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Authentication failed: " + e.getMessage(), null));
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get registered passkeys for the current user")
    public ResponseEntity<ApiResponse> getPasskeys(@AuthenticationPrincipal SecurityUser securityUser) {
        List<PasskeyCredential> credentials = passkeyDAO.getCredentialsByUserId(securityUser.getUser().getId());
        return ResponseEntity.ok(new ApiResponse(true, "Passkeys retrieved.", credentials));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a registered passkey")
    public ResponseEntity<ApiResponse> deletePasskey(@PathVariable int id, @AuthenticationPrincipal SecurityUser securityUser) {
        if (passkeyDAO.deleteCredential(id, securityUser.getUser().getId())) {
            return ResponseEntity.ok(new ApiResponse(true, "Passkey deleted.", null));
        }
        return new ResponseEntity<>(new ApiResponse(false, "Passkey not found or you do not have permission to delete it.", null), HttpStatus.NOT_FOUND);
    }
}