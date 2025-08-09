package de.technikteam.service;

import de.technikteam.dao.AdminLogDAO;
import de.technikteam.model.AdminLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminLogService {
	private static final Logger logger = LogManager.getLogger(AdminLogService.class);
	private final AdminLogDAO logDAO;

	@Autowired
	public AdminLogService(AdminLogDAO logDAO) {
		this.logDAO = logDAO;
	}

	private String sanitize(String input) {
		if (input == null) {
			return "";
		}
		// Replace newlines and carriage returns to prevent log injection
		return input.replace('\n', '_').replace('\r', '_');
	}

	public void log(String adminUsername, String actionType, String details) {
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
			logger.error(
					"KRITISCH: Fehler beim Schreiben in das Admin-Audit-Log! Daten: [Benutzer: {}, Aktion: {}, Details: {}]",
					sanitize(adminUsername), sanitize(actionType), sanitize(details), e);
		}
	}
}