package de.technikteam.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.AdminLogDAO;
import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.AdminLog;
import de.technikteam.model.Course;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminLogService {
	private static final Logger logger = LogManager.getLogger(AdminLogService.class);
	private final AdminLogDAO logDAO;
	private final AdminUserManagementService adminUserManagementService;
	private final UserService userService;
	private final CourseDAO courseDAO;
	private final NotificationService notificationService;
	private final Gson gson;

	@Autowired
	public AdminLogService(AdminLogDAO logDAO, @Lazy AdminUserManagementService adminUserManagementService,
			@Lazy UserService userService, @Lazy CourseDAO courseDAO, @Lazy NotificationService notificationService) {
		this.logDAO = logDAO;
		this.adminUserManagementService = adminUserManagementService;
		this.userService = userService;
		this.courseDAO = courseDAO;
		this.notificationService = notificationService;
		this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	private String sanitize(String input) {
		if (input == null) {
			return "";
		}
		return input.replace('\n', '_').replace('\r', '_');
	}

	public void log(String adminUsername, String actionType, String details) {
		log(adminUsername, actionType, details, null);
	}

	public void log(String adminUsername, String actionType, String details, Map<String, Object> context) {
		try {
			String saneAdminUsername = sanitize(adminUsername);
			String saneActionType = sanitize(actionType);
			String saneDetails = sanitize(details);
			AdminLog log = new AdminLog();
			log.setAdminUsername(saneAdminUsername);
			log.setActionType(saneActionType);
			log.setDetails(saneDetails);
			if (context != null) {
				log.setContext(gson.toJson(context));
			}
			logger.info("[AUDIT] User: '{}', Action: '{}', Details: {}", saneAdminUsername, saneActionType,
					saneDetails);
			logDAO.createLog(log);
			notificationService.broadcastUIUpdate("ADMIN_LOG", "CREATED", log);
		} catch (Exception e) {
			logger.error(
					"KRITISCH: Fehler beim Schreiben in das Admin-Audit-Log! Daten: [Benutzer: {}, Aktion: {}, Details: {}]",
					sanitize(adminUsername), sanitize(actionType), sanitize(details), e);
		}
	}

	// --- Specialized Logging Methods ---

	public void logUserCreation(String adminUsername, int createdUserId) {
		String details = String.format("User (ID: %d) created.", createdUserId);
		Map<String, Object> context = Map.of("revocable", true, "createdUserId", createdUserId);
		log(adminUsername, "USER_CREATE", details, context);
	}

	public void logUserUpdate(String adminUsername, User before, User after) {
		String details = String.format("User '%s' (ID: %d) updated.", after.getUsername(), after.getId());
		Map<String, Object> context = Map.of("revocable", true, "beforeState", gson.toJson(before), "afterState",
				gson.toJson(after));
		log(adminUsername, "USER_UPDATE", details, context);
	}

	public void logUserDeletion(String adminUsername, User deletedUser) {
		String details = String.format("User '%s' (ID: %d) deleted.", deletedUser.getUsername(), deletedUser.getId());
		Map<String, Object> context = Map.of("revocable", true, "deletedUserId", deletedUser.getId(), "deletedUser",
				gson.toJson(deletedUser));
		log(adminUsername, "USER_DELETE", details, context);
	}

	public void logCourseCreation(String adminUsername, Course createdCourse) {
		String details = String.format("Course '%s' (ID: %d) created.", createdCourse.getName(), createdCourse.getId());
		Map<String, Object> context = Map.of("revocable", true, "createdCourseId", createdCourse.getId());
		log(adminUsername, "COURSE_CREATE", details, context);
	}

	public void logCourseUpdate(String adminUsername, Course before, Course after) {
		String details = String.format("Course '%s' (ID: %d) updated.", after.getName(), after.getId());
		Map<String, Object> context = Map.of("revocable", true, "beforeState", gson.toJson(before), "afterState",
				gson.toJson(after));
		log(adminUsername, "COURSE_UPDATE", details, context);
	}

	public void logCourseDeletion(String adminUsername, Course deletedCourse) {
		String details = String.format("Course '%s' (ID: %d) deleted.", deletedCourse.getName(),
				deletedCourse.getId());
		Map<String, Object> context = Map.of("revocable", true, "deletedCourse", gson.toJson(deletedCourse));
		log(adminUsername, "COURSE_DELETE", details, context);
	}

	// --- Revocation Logic ---

	@Transactional
	public void revokeAction(long logId, User revokingAdmin) {
		AdminLog logToRevoke = logDAO.getLogById(logId);
		if (logToRevoke == null) {
			throw new IllegalArgumentException("Log-Eintrag nicht gefunden.");
		}
		if ("REVOKED".equals(logToRevoke.getStatus())) {
			throw new IllegalStateException("Diese Aktion wurde bereits widerrufen.");
		}

		Map<String, Object> context = parseContext(logToRevoke.getContext());
		if (context == null || !Boolean.TRUE.equals(context.get("revocable"))) {
			throw new UnsupportedOperationException("Diese Aktion kann nicht widerrufen werden.");
		}

		// Perform the reversal action based on the original action type
		String actionType = logToRevoke.getActionType();

		switch (actionType) {
		case "USER_SUSPEND":
			int targetUserId = ((Number) context.get("userId")).intValue();
			adminUserManagementService.unsuspendUser(targetUserId, revokingAdmin);
			break;
		case "USER_CREATE":
			int createdUserId = ((Number) context.get("createdUserId")).intValue();
			userService.deleteUser(createdUserId, revokingAdmin);
			break;
		case "USER_DELETE":
			int deletedUserId = ((Number) context.get("deletedUserId")).intValue();
			userService.undeleteUser(deletedUserId, revokingAdmin);
			break;
		case "USER_UPDATE":
			User beforeUser = gson.fromJson((String) context.get("beforeState"), User.class);
			userService.restoreUserState(beforeUser, revokingAdmin);
			break;
		case "COURSE_CREATE":
			int createdCourseId = ((Number) context.get("createdCourseId")).intValue();
			courseDAO.deleteCourse(createdCourseId);
			break;
		case "COURSE_DELETE":
			Course restoredCourse = gson.fromJson((String) context.get("deletedCourse"), Course.class);
			courseDAO.createCourse(restoredCourse);
			break;
		case "COURSE_UPDATE":
			Course beforeCourse = gson.fromJson((String) context.get("beforeState"), Course.class);
			courseDAO.updateCourse(beforeCourse);
			break;
		default:
			throw new UnsupportedOperationException(
					"Das Widerrufen für den Aktionstyp '" + actionType + "' ist nicht implementiert.");
		}

		// Mark the original log entry as revoked
		logDAO.updateStatus(logId, "REVOKED", revokingAdmin.getId());
		notificationService.broadcastUIUpdate("ADMIN_LOG", "UPDATED", logDAO.getLogById(logId));

		// Log the revocation itself
		log(revokingAdmin.getUsername(), "ACTION_REVOKED",
				"Aktion (Log ID: " + logId + ", Typ: " + logToRevoke.getActionType() + ") widerrufen.");
	}

	private Map<String, Object> parseContext(String contextJson) {
		if (contextJson == null || contextJson.isBlank()) {
			return null;
		}
		try {
			Type type = new TypeToken<Map<String, Object>>() {
			}.getType();
			return gson.fromJson(contextJson, type);
		} catch (Exception e) {
			logger.error("Failed to parse admin log context JSON: {}", contextJson, e);
			return null;
		}
	}
}