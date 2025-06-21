package de.technikteam.service;

import de.technikteam.dao.AdminLogDAO;
import de.technikteam.model.AdminLog;

/**
 * A simple service that acts as a middleman for logging. It provides a static
 * log method that other parts of the application can call to create an
 * administrative audit log entry without having to interact directly with the
 * AdminLogDAO.
 */
public class AdminLogService {
	private static final AdminLogDAO logDAO = new AdminLogDAO();

	/**
	 * Logs an administrative action to the database.
	 * 
	 * @param adminUsername The username of the admin performing the action.
	 * @param actionType    A description of the action (e.g., "CREATE_USER").
	 * @param details       More details about the action (e.g., "Created user
	 *                      'testuser'").
	 */
	public static void log(String adminUsername, String actionType, String details) {
		AdminLog log = new AdminLog();
		log.setAdminUsername(adminUsername);
		log.setActionType(actionType);
		log.setDetails(details);
		logDAO.createLog(log); // Diese Methode müssen wir im DAO erstellen
	}
}