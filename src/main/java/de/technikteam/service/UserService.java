package de.technikteam.service;

import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class UserService {
	private static final Logger logger = LogManager.getLogger(UserService.class);

	private final UserDAO userDAO;
	private final AdminLogService adminLogService;
	private final NotificationService notificationService;

	@Autowired
	public UserService(UserDAO userDAO, AdminLogService adminLogService, NotificationService notificationService) {
		this.userDAO = userDAO;
		this.adminLogService = adminLogService;
		this.notificationService = notificationService;
	}

	@Transactional
	public int createUserWithPermissions(User user, String password, String[] permissionIds, String adminUsername) {
		try {
			int newUserId = userDAO.createUser(user, password);
			if (newUserId > 0) {
				userDAO.updateUserPermissions(newUserId, permissionIds);
				logger.info("Transaction for creating user '{}' committed successfully.", user.getUsername());
				adminLogService.logUserCreation(adminUsername, newUserId);
				User createdUser = userDAO.getUserById(newUserId);
				notificationService.broadcastUIUpdate("USER", "CREATED", createdUser);
				return newUserId;
			} else {
				// This case might occur if there's a non-exception failure in the DAO
				return 0;
			}
		} catch (DuplicateKeyException e) {
			logger.warn("Attempted to create a user with a duplicate username or email: {}", user.getUsername());
			// Re-throw as a runtime exception to ensure the transaction rolls back
			// and the caller knows about the failure.
			throw new IllegalStateException("Benutzername oder E-Mail existiert bereits.", e);
		} catch (Exception e) {
			logger.error("Unexpected error during user creation for '{}'", user.getUsername(), e);
			throw new RuntimeException("Benutzererstellung ist aufgrund eines unerwarteten Fehlers fehlgeschlagen.");
		}
	}

	@Transactional
	public boolean updateUserWithPermissions(User user, String[] permissionIds) {
		boolean profileUpdated = userDAO.updateUser(user);
		boolean permissionsUpdated = userDAO.updateUserPermissions(user.getId(), permissionIds);
		logger.info("Transaction for updating user '{}' committed successfully.", user.getUsername());
		return profileUpdated || permissionsUpdated;
	}

	@Transactional
	public boolean deleteUser(int userId, User adminUser) {
		User userToDelete = userDAO.getUserById(userId);
		if (userToDelete == null) {
			return false;
		}
		boolean success = userDAO.deleteUser(userId);
		if (success) {
			adminLogService.logUserDeletion(adminUser.getUsername(), userToDelete);
			notificationService.broadcastUIUpdate("USER", "DELETED", Map.of("id", userId));
		}
		return success;
	}

	@Transactional
	public boolean undeleteUser(int userId, User adminUser) {
		boolean success = userDAO.undeleteUser(userId);
		if (success) {
            User restoredUser = userDAO.getUserById(userId);
			notificationService.broadcastUIUpdate("USER", "CREATED", restoredUser); // Treat as created for the UI
		}
		return success;
	}

	@Transactional
	public boolean restoreUserState(User userToRestore, User adminUser) {
		// This method assumes the user object `userToRestore` contains the full state
		// to be restored.
		// The `updateUser` method in the DAO is designed to update only the fields
		// provided in the UserUpdateRequest DTO.
		// We'll use the existing `updateUserWithPermissions` for simplicity, assuming
		// the `beforeState` has permissions.
		String[] permissionIds = userToRestore.getPermissions().stream().map(String::valueOf).toArray(String[]::new);
        boolean success = updateUserWithPermissions(userToRestore, permissionIds);
        if(success) {
            notificationService.broadcastUIUpdate("USER", "UPDATED", userToRestore);
        }
        return success;
	}
}