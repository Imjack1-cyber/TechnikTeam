package de.technikteam.service;

import de.technikteam.dao.TwoFactorAuthDAO;
import de.technikteam.dao.UserDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import de.technikteam.model.User;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AdminUserManagementService {
	private static final Logger logger = LogManager.getLogger(AdminUserManagementService.class);

	private final UserDAO userDAO;
	private final AdminLogService adminLogService;
	private final LoginAttemptService loginAttemptService;
	private final TwoFactorAuthDAO twoFactorAuthDAO;
	private final NotificationService notificationService;

	@Autowired
	public AdminUserManagementService(UserDAO userDAO, AdminLogService adminLogService,
			LoginAttemptService loginAttemptService, TwoFactorAuthDAO twoFactorAuthDAO, NotificationService notificationService) {
		this.userDAO = userDAO;
		this.adminLogService = adminLogService;
		this.loginAttemptService = loginAttemptService;
		this.twoFactorAuthDAO = twoFactorAuthDAO;
		this.notificationService = notificationService;
	}

	@Transactional
	public boolean suspendUser(int userId, String durationStr, String reason, User adminUser) {
		User userToSuspend = userDAO.getUserById(userId);
		if (userToSuspend == null) {
			throw new IllegalArgumentException("Benutzer nicht gefunden.");
		}
		
		// The root admin (ID 1) cannot be suspended.
		if (userToSuspend.getId() == 1) {
			throw new AccessDeniedException("The root admin account cannot be suspended.");
		}
		// An admin cannot suspend themselves.
        if (userToSuspend.getId() == adminUser.getId()) {
            throw new AccessDeniedException("Sie können sich nicht selbst sperren.");
        }
		// A non-root admin cannot suspend another admin. Only the root admin (ID 1) can.
		if (userToSuspend.hasAdminAccess() && adminUser.getId() != 1) {
			throw new AccessDeniedException("Only the root administrator can suspend other admins.");
		}


		LocalDateTime suspendedUntil = null;

		if (durationStr != null && !durationStr.isBlank() && !durationStr.equalsIgnoreCase("indefinite")) {
			try {
				suspendedUntil = parseDurationToLocalDateTime(durationStr);
			} catch (IllegalArgumentException e) {
				logger.warn("Invalid duration string '{}' provided for suspending user {}", durationStr, userId);
				throw e;
			}
		}

		boolean result = userDAO.suspendUser(userId, suspendedUntil, reason);
		if (result) {
			User suspendedUser = userDAO.getUserById(userId);
			String logDetails = String.format("User '%s' (ID: %d) suspended until %s. Reason: %s",
					suspendedUser.getUsername(), userId,
					suspendedUntil != null ? suspendedUntil.toString() : "indefinite", reason);
			Map<String, Object> context = Map.of("userId", userId, "revocable", true);
			adminLogService.log(adminUser.getUsername(), "USER_SUSPEND", logDetails, context);
			notificationService.broadcastUIUpdate("USER", "UPDATED", suspendedUser);
		} else {
			logger.warn("Failed to suspend user id {}", userId);
		}
		return result;
	}

	@Transactional
	public boolean unsuspendUser(int userId, User adminUser) {
		boolean result = userDAO.unsuspendUser(userId);
		User unsuspendedUser = userDAO.getUserById(userId);

		if (unsuspendedUser != null) {
			// Also clear any login attempt lockouts, regardless of whether the unsuspend
			// operation changed the row
			loginAttemptService.clearLoginAttempts(unsuspendedUser.getUsername());
			twoFactorAuthDAO.clearKnownIpsForUser(userId);

			if (result) {
				String logDetails = String.format("User '%s' (ID: %d) unsuspended, unlocked, and known IPs cleared.",
						unsuspendedUser.getUsername(), userId);
				// Un-suspending is not easily reversible, so mark as not revocable
				Map<String, Object> context = Map.of("userId", userId, "revocable", false);
				adminLogService.log(adminUser.getUsername(), "USER_UNSUSPEND", logDetails, context);
				notificationService.broadcastUIUpdate("USER", "UPDATED", unsuspendedUser);
			}
			return true; // Return true if the user is now in an unsuspended state, even if they already
							// were
		} else {
			logger.warn("Failed to unsuspend user id {}", userId);
			return false;
		}
	}

	public User getUser(int userId) {
		return userDAO.getUserById(userId);
	}

	private LocalDateTime parseDurationToLocalDateTime(String durationStr) {
		durationStr = durationStr.trim().toLowerCase();
		if (durationStr.length() < 2) {
			throw new IllegalArgumentException("Invalid duration: " + durationStr);
		}
		char unit = durationStr.charAt(durationStr.length() - 1);
		String numberPart = durationStr.substring(0, durationStr.length() - 1);
		long amount;
		try {
			amount = Long.parseLong(numberPart);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid numeric part in duration: " + durationStr, e);
		}

		LocalDateTime now = LocalDateTime.now();
		switch (unit) {
		case 's':
			return now.plusSeconds(amount);
		case 'm':
			return now.plusMinutes(amount);
		case 'h':
			return now.plusHours(amount);
		case 'd':
			return now.plusDays(amount);
		case 'w':
			return now.plusWeeks(amount);
		default:
			throw new IllegalArgumentException("Unsupported duration unit: " + unit + ". Use s, m, h, d, w.");
		}
	}
}