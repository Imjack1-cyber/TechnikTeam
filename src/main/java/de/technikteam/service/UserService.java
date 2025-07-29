package de.technikteam.service;

import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
	private static final Logger logger = LogManager.getLogger(UserService.class);

	private final UserDAO userDAO;
	private final AdminLogService adminLogService;

	@Autowired
	public UserService(UserDAO userDAO, AdminLogService adminLogService) {
		this.userDAO = userDAO;
		this.adminLogService = adminLogService;
	}

	@Transactional
	public int createUserWithPermissions(User user, String password, String[] permissionIds, String adminUsername) {
		int newUserId = userDAO.createUser(user, password);
		if (newUserId > 0) {
			userDAO.updateUserPermissions(newUserId, permissionIds);
			logger.info("Transaction for creating user '{}' committed successfully.", user.getUsername());

			String logDetails = String.format(
					"Benutzer '%s' (ID: %d, Rolle-ID: %d, Klasse: %s) erstellt und Berechtigungen zugewiesen.",
					user.getUsername(), newUserId, user.getRoleId(), user.getClassName());
			adminLogService.log(adminUsername, "CREATE_USER", logDetails);

			return newUserId;
		} else {
			throw new RuntimeException("User creation returned an invalid ID.");
		}
	}

	@Transactional
	public boolean updateUserWithPermissions(User user, String[] permissionIds) {
		boolean profileUpdated = userDAO.updateUser(user);
		boolean permissionsUpdated = userDAO.updateUserPermissions(user.getId(), permissionIds);
		logger.info("Transaction for updating user '{}' committed successfully.", user.getUsername());
		return profileUpdated || permissionsUpdated;
	}
}