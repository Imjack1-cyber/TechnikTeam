package de.technikteam.config;

import de.technikteam.dao.PermissionDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import de.technikteam.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class InitialAdminCreator implements CommandLineRunner {

	private static final Logger logger = LogManager.getLogger(InitialAdminCreator.class);

	private final UserDAO userDAO;
	private final UserService userService;
	private final PermissionDAO permissionDAO;

	@Autowired
	public InitialAdminCreator(UserDAO userDAO, UserService userService, PermissionDAO permissionDAO) {
		this.userDAO = userDAO;
		this.userService = userService;
		this.permissionDAO = permissionDAO;
	}

	@Override
	public void run(String... args) throws Exception {
		if (userDAO.getUserByUsername("admin") == null) {
			logger.warn("############################################################");
			logger.warn("##              FIRST TIME SETUP DETECTED                 ##");
			logger.warn("##     Creating default 'admin' user with a random pass   ##");
			logger.warn("############################################################");

			User adminUser = new User();
			adminUser.setUsername("admin");
			adminUser.setRoleId(1); // Assuming 1 is the ADMIN role ID

			String randomPassword = generateRandomPassword(16);

			// Fetch the ID for the master admin permission
			Integer adminPermissionId = permissionDAO.getPermissionIdByKey(Permissions.ACCESS_ADMIN_PANEL);
			if (adminPermissionId == null) {
				logger.error(
						"FATAL: Could not find the essential ACCESS_ADMIN_PANEL permission in the database. Admin user cannot be created with full rights.");
				throw new RuntimeException("ACCESS_ADMIN_PANEL permission not found.");
			}
			String[] adminPermissionIds = { String.valueOf(adminPermissionId) };

			userService.createUserWithPermissions(adminUser, randomPassword, adminPermissionIds, "SYSTEM");

			logger.warn("############################################################");
			logger.warn("##                ADMIN USER CREATED                      ##");
			logger.warn("##                                                        ##");
			logger.warn("##    Username: admin                                     ##");
			logger.warn("##    Password: " + randomPassword + "                     ##");
			logger.warn("##                                                        ##");
			logger.warn("##   !!! PLEASE COPY THIS PASSWORD AND STORE IT SAFELY.   ##");
			logger.warn("##             IT WILL NOT BE SHOWN AGAIN.                ##");
			logger.warn("############################################################");
		}
	}

	private String generateRandomPassword(int length) {
		final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
		SecureRandom random = new SecureRandom();
		return random.ints(length, 0, chars.length()).mapToObj(chars::charAt)
				.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
	}
}