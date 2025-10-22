package de.technikteam.service;

import com.google.gson.Gson;
import de.technikteam.api.v1.dto.NotificationPayload;
import de.technikteam.config.Permissions;
import de.technikteam.dao.IdentityVerificationDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.IdentityVerificationRequest;
import de.technikteam.model.User;
import de.technikteam.util.PasswordPolicyValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class IdentityVerificationService {
    private static final Logger logger = LogManager.getLogger(IdentityVerificationService.class);

    private final IdentityVerificationDAO verificationDAO;
    private final NotificationService notificationService;
    private final UserDAO userDAO;
    private final AdminLogService adminLogService;

    @Autowired
    public IdentityVerificationService(IdentityVerificationDAO verificationDAO, NotificationService notificationService, UserDAO userDAO, AdminLogService adminLogService) {
        this.verificationDAO = verificationDAO;
        this.notificationService = notificationService;
        this.userDAO = userDAO;
        this.adminLogService = adminLogService;
    }

    @Transactional
    public String initiateRequest(User user, String requestType, Map<String, Object> context) {
        String token = generateSecureToken();
        IdentityVerificationRequest request = new IdentityVerificationRequest();
        request.setUserId(user.getId());
        request.setChallengeToken(token);
        request.setRequestType(requestType);
        if (context != null) {
            request.setContext(new Gson().toJson(context));
        }
        request.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        verificationDAO.create(request);
        logger.info("Created identity verification request of type {} for user '{}' with token {}", requestType, user.getUsername(), token);

        // Fetch the full request to include username in the broadcast
        IdentityVerificationRequest createdRequest = verificationDAO.findByToken(token).orElse(null);
        if(createdRequest != null) {
            notificationService.broadcastUIUpdate("IDENTITY_VERIFICATION", "CREATED", createdRequest);
        }

        if (user.getFcmToken() != null) {
            sendPushNotification(user, requestType, token);
        }

        if ("PASSWORD_RESET".equals(requestType)) {
            notifyAdminsOfPasswordResetRequest(user);
        }
        return token;
    }

    @Transactional
    public void verifyRequest(String token, String decision, User approvingUser) {
        IdentityVerificationRequest request = verificationDAO.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Ungültiger oder abgelaufener Token."));

        if (request.getUserId() != approvingUser.getId()) {
            throw new AccessDeniedException("Sie sind nicht berechtigt, diese Anfrage zu bearbeiten.");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Diese Anfrage wurde bereits bearbeitet.");
        }
        if (request.getExpiresAt().isBefore(LocalDateTime.now())) {
            verificationDAO.updateStatus(token, "EXPIRED");
            throw new IllegalStateException("Diese Anfrage ist abgelaufen.");
        }

        String newStatus = "approve".equalsIgnoreCase(decision) ? "APPROVED" : "DENIED";
        verificationDAO.updateStatus(token, newStatus);
        logger.info("Identity verification request for token {} was {} by user '{}'", token, newStatus, approvingUser.getUsername());
    }

    public String checkStatus(String token) {
        return verificationDAO.findByToken(token)
                .map(IdentityVerificationRequest::getStatus)
                .orElse("INVALID");
    }

    public IdentityVerificationRequest getRequestDetails(String token) {
        return verificationDAO.findByToken(token).orElse(null);
    }

    @Transactional
    public void finalizePasswordReset(String token, String newPassword) {
        IdentityVerificationRequest request = verificationDAO.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Ungültiger oder abgelaufener Token."));

        if (!"APPROVED".equals(request.getStatus()) || !"PASSWORD_RESET".equals(request.getRequestType())) {
            throw new IllegalStateException("Anfrage nicht genehmigt oder falscher Anfragetyp.");
        }

        PasswordPolicyValidator.ValidationResult validationResult = PasswordPolicyValidator.validate(newPassword);
        if (!validationResult.isValid()) {
            throw new IllegalArgumentException("Passwort entspricht nicht den Richtlinien: " + validationResult.getMessage());
        }

        userDAO.changePassword(request.getUserId(), newPassword);
        verificationDAO.updateStatus(token, "COMPLETED");
        notificationService.broadcastUIUpdate("IDENTITY_VERIFICATION", "DELETED", Map.of("id", request.getId()));
        logger.info("Password reset finalized for user ID {} using token {}", request.getUserId(), token);
    }

    public List<IdentityVerificationRequest> getPendingPasswordResets() {
        return verificationDAO.findPendingPasswordResets();
    }

    @Transactional
    public void markAsCompleted(String token, User adminUser) {
        IdentityVerificationRequest request = verificationDAO.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Request not found."));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Request has already been processed.");
        }

        if (verificationDAO.updateStatus(token, "COMPLETED")) {
            adminLogService.log(adminUser.getUsername(), "PASSWORD_RESET_COMPLETED", "Admin manually reset password for request token: " + token);
            notificationService.broadcastUIUpdate("IDENTITY_VERIFICATION", "DELETED", Map.of("id", request.getId()));
        }
    }


    private void sendPushNotification(User user, String requestType, String token) {
        NotificationPayload payload = new NotificationPayload();
        payload.setLevel("Important");
        payload.setAndroidImportance("HIGH");
        payload.setUrl("/verify-identity/" + token);

        if ("PASSWORD_RESET".equals(requestType)) {
            payload.setTitle("Passwort zurücksetzen");
            payload.setDescription("Eine Anfrage zum Zurücksetzen Ihres Passworts wurde gestellt. Bitte bestätigen.");
        } else if ("MFA_LOGIN".equals(requestType)) {
            payload.setTitle("Anmeldung bestätigen");
            payload.setDescription("Eine neue Anmeldung bei Ihrem Konto erfordert Ihre Bestätigung.");
        } else {
            return; // Unknown type
        }

        notificationService.sendNotificationToUser(user.getId(), payload);
    }

    private void notifyAdminsOfPasswordResetRequest(User user) {
        List<Integer> adminIds = userDAO.findUserIdsByPermission(Permissions.USER_PASSWORD_RESET);
        if (adminIds.isEmpty()) return;

        NotificationPayload payload = new NotificationPayload();
        payload.setTitle("Passwort-Reset angefordert");
        payload.setDescription(String.format("Benutzer '%s' hat das Zurücksetzen seines Passworts angefordert.", user.getUsername()));
        payload.setLevel("Important");
        payload.setUrl("/admin/users/password-resets");

        adminIds.forEach(adminId -> notificationService.sendNotificationToUser(adminId, payload));
        logger.info("Notified {} admins about password reset request for user '{}'", adminIds.size(), user.getUsername());
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cleanupExpired() {
        int deletedCount = verificationDAO.deleteExpired();
        if (deletedCount > 0) {
            logger.info("Cleaned up {} expired identity verification requests.", deletedCount);
        }
    }
}