package de.technikteam.service;

import de.technikteam.dao.AdminLogDAO;
import de.technikteam.model.AdminLog;

/**
 * A central service for logging administrative actions.
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