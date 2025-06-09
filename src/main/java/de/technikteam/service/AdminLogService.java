package de.technikteam.service;

import de.technikteam.dao.AdminLogDAO; // Müssen wir noch erstellen
import de.technikteam.model.User;

public class AdminLogService {
	private static AdminLogDAO logDAO = new AdminLogDAO();

	public static void log(User admin, String action, String target) {
		log(admin, action, target, null);
	}

	public static void log(User admin, String action, String target, String details) {
		if (admin == null || !"ADMIN".equalsIgnoreCase(admin.getRole())) {
			return; // Nur Admins loggen
		}
		logDAO.createLog(admin.getUsername(), action, target, details);
	}
}