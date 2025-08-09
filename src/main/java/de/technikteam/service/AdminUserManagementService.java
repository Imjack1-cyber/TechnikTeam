package de.technikteam.service;

import de.technikteam.dao.UserDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import de.technikteam.model.User;

import java.time.LocalDateTime;

@Service
public class AdminUserManagementService {
	private static final Logger logger = LogManager.getLogger(AdminUserManagementService.class);

	private final UserDAO userDAO;
	private final AdminLogService adminLogService;
	private final LoginAttemptService loginAttemptService;

	@Autowired
	public AdminUserManagementService(UserDAO userDAO, AdminLogService adminLogService,
			LoginAttemptService loginAttemptService) {
		this.userDAO = userDAO;
		this.adminLogService = adminLogService;
		this.loginAttemptService = loginAttemptService;
	}

	@Transactional
	public boolean suspendUser(int userId, String durationStr, String reason, User adminUser) {
		User userToSuspend = userDAO.getUserById(userId);
		if (userToSuspend == null) {
			throw new IllegalArgumentException("Benutzer nicht gefunden.");
		}
		if ("ADMIN".equals(userToSuspend.getRoleName())) {
			throw new AccessDeniedException("Administratoren können nicht gesperrt werden.");
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
			adminLogService.log(adminUser.getUsername(), "USER_SUSPEND", logDetails);
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

			if (result) {
				String logDetails = String.format("User '%s' (ID: %d) unsuspended and unlocked.",
						unsuspendedUser.getUsername(), userId);
				adminLogService.log(adminUser.getUsername(), "USER_UNSUSPEND", logDetails);
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