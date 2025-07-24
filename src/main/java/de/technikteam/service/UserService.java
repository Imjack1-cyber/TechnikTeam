package de.technikteam.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.DatabaseManager;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

@Singleton
public class UserService {
	private static final Logger logger = LogManager.getLogger(UserService.class);

	private final DatabaseManager dbManager;
	private final UserDAO userDAO;
	private final AdminLogService adminLogService;

	@Inject
	public UserService(DatabaseManager dbManager, UserDAO userDAO, AdminLogService adminLogService) {
		this.dbManager = dbManager;
		this.userDAO = userDAO;
		this.adminLogService = adminLogService;
	}

	public int createUserWithPermissions(User user, String password, String[] permissionIds, String adminUsername) {
		try (Connection conn = dbManager.getConnection()) {
			conn.setAutoCommit(false);
			try {
				int newUserId = userDAO.createUser(user, password, conn);
				if (newUserId > 0) {
					userDAO.updateUserPermissions(newUserId, permissionIds, conn);
					conn.commit();
					logger.info("Transaction for creating user '{}' committed successfully.", user.getUsername());

					String logDetails = String.format(
							"Benutzer '%s' (ID: %d, Rolle-ID: %d, Klasse: %s) erstellt und Berechtigungen zugewiesen.",
							user.getUsername(), newUserId, user.getRoleId(), user.getClassName());
					adminLogService.log(adminUsername, "CREATE_USER", logDetails);

					return newUserId;
				} else {
					throw new SQLException("User creation returned an invalid ID.");
				}
			} catch (Exception e) {
				conn.rollback();
				logger.error("Transaction rolled back for createUserWithPermissions for username '{}'.",
						user.getUsername(), e);
				return 0;
			}
		} catch (SQLException e) {
			logger.error("Failed to get connection for createUserWithPermissions.", e);
			return 0;
		}
	}

	public boolean updateUserWithPermissions(User user, String[] permissionIds) {
		try (Connection conn = dbManager.getConnection()) {
			conn.setAutoCommit(false);
			try {
				boolean profileUpdated = userDAO.updateUser(user, conn);
				boolean permissionsUpdated = userDAO.updateUserPermissions(user.getId(), permissionIds, conn);
				conn.commit();
				logger.info("Transaction for updating user '{}' committed successfully.", user.getUsername());
				return profileUpdated || permissionsUpdated;
			} catch (Exception e) {
				conn.rollback();
				logger.error("Transaction rolled back for updateUserWithPermissions for user '{}'.", user.getUsername(),
						e);
				return false;
			}
		} catch (SQLException e) {
			logger.error("Failed to get connection for updateUserWithPermissions.", e);
			return false;
		}
	}
}