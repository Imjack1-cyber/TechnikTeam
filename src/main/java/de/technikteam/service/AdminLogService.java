package de.technikteam.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.AdminLogDAO;
import de.technikteam.model.AdminLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public class AdminLogService {
	private static final Logger logger = LogManager.getLogger(AdminLogService.class);
	private final AdminLogDAO logDAO;

	@Inject
	public AdminLogService(AdminLogDAO logDAO) {
		this.logDAO = logDAO;
	}

	private String sanitize(String input) {
		if (input == null) {
			return "";
		}
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
			logger.error("CRITICAL: Failed to write to admin audit log! Data: [User: {}, Action: {}, Details: {}]",
					sanitize(adminUsername), sanitize(actionType), sanitize(details), e);
		}
	}
}