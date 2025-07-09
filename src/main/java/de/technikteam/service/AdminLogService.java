package de.technikteam.service;

import de.technikteam.dao.AdminLogDAO;
import de.technikteam.model.AdminLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A simple service layer that acts as a middleman for logging administrative
 * actions. It provides a static `log` method that other parts of the
 * application can call to create an audit log entry. This decouples the
 * application logic from the direct use of the AdminLogDAO.
 */
public class AdminLogService {
	private static final Logger logger = LogManager.getLogger(AdminLogService.class);
	private static final AdminLogDAO logDAO = new AdminLogDAO();

	/**
	 * Removes characters that could be used for log injection attacks.
	 * 
	 * @param input The string to sanitize.
	 * @return The sanitized string.
	 */
	private static String sanitize(String input) {
		if (input == null) {
			return "";
		}
		return input.replace('\n', '_').replace('\r', '_');
	}

	/**
	 * Creates and persists an administrative audit log entry. This is the central
	 * point for all audit logging.
	 * 
	 * @param adminUsername The username of the admin performing the action.
	 * @param actionType    A high-level category for the action (e.g.,
	 *                      "UPDATE_USER", "DELETE_EVENT").
	 * @param details       A detailed, human-readable description of the action and
	 *                      its context.
	 */
	public static void log(String adminUsername, String actionType, String details) {
		try {
			String saneAdminUsername = sanitize(adminUsername);
			String saneActionType = sanitize(actionType);
			String saneDetails = sanitize(details);

			AdminLog log = new AdminLog();
			log.setAdminUsername(saneAdminUsername);
			log.setActionType(saneActionType);
			log.setDetails(saneDetails);

			logger.info("[AUDIT] User: '{}', Action: '{}', Details: {}", saneAdminUsername, saneActionType,
					saneDetails);

			logDAO.createLog(log);
		} catch (Exception e) {
			logger.error("CRITICAL: Failed to write to admin audit log! Data: [User: {}, Action: {}, Details: {}]",
					sanitize(adminUsername), sanitize(actionType), sanitize(details), e);
		}
	}
}