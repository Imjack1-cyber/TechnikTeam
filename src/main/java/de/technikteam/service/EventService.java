// src/main/java/de/technikteam/service/EventService.java
package de.technikteam.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.*;
import de.technikteam.model.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class EventService {
	private static final Logger logger = LogManager.getLogger(EventService.class);

	private final EventDAO eventDAO;
	private final AttachmentDAO attachmentDAO;
	private final EventCustomFieldDAO customFieldDAO;
	private final DatabaseManager dbManager;
	private final ConfigurationService configService;
	private final AdminLogService adminLogService;
	private final NotificationService notificationService;

	@Inject
	public EventService(EventDAO eventDAO, AttachmentDAO attachmentDAO, EventCustomFieldDAO customFieldDAO,
			DatabaseManager dbManager, ConfigurationService configService, AdminLogService adminLogService,
			NotificationService notificationService) {
		this.eventDAO = eventDAO;
		this.attachmentDAO = attachmentDAO;
		this.customFieldDAO = customFieldDAO;
		this.dbManager = dbManager;
		this.configService = configService;
		this.adminLogService = adminLogService;
		this.notificationService = notificationService;
	}

	public int createOrUpdateEvent(Event event, boolean isUpdate, User adminUser, HttpServletRequest request) {
		try (Connection conn = dbManager.getConnection()) {
			conn.setAutoCommit(false);
			try {
				int eventId;
				if (isUpdate) {
					if (eventDAO.updateEvent(event, conn)) {
						eventId = event.getId();
						adminLogService.log(adminUser.getUsername(), "UPDATE_EVENT",
								"Event '" + event.getName() + "' (ID: " + eventId + ") aktualisiert.");
					} else {
						throw new SQLException("Failed to update the core event record.");
					}
				} else {
					eventId = eventDAO.createEvent(event, conn);
					if (eventId > 0) {
						event.setId(eventId);
						adminLogService.log(adminUser.getUsername(), "CREATE_EVENT",
								"Event '" + event.getName() + "' (ID: " + eventId + ") erstellt.");
					} else {
						throw new SQLException("Failed to create the core event record.");
					}
				}

				String[] requiredCourseIds = request.getParameterValues("requiredCourseId");
				String[] requiredPersons = request.getParameterValues("requiredPersons");
				eventDAO.saveSkillRequirements(eventId, requiredCourseIds, requiredPersons, conn);

				String[] itemIds = request.getParameterValues("itemId");
				String[] quantities = request.getParameterValues("itemQuantity");
				eventDAO.saveReservations(eventId, itemIds, quantities, conn);

				String[] customFieldNames = request.getParameterValues("customFieldName");
				String[] customFieldTypes = request.getParameterValues("customFieldType");
				if (customFieldNames != null) {
					List<EventCustomField> customFields = new ArrayList<>();
					for (int i = 0; i < customFieldNames.length; i++) {
						if (customFieldNames[i] != null && !customFieldNames[i].trim().isEmpty()) {
							EventCustomField cf = new EventCustomField();
							cf.setFieldName(customFieldNames[i]);
							cf.setFieldType(customFieldTypes[i]);
							cf.setRequired(true);
							customFields.add(cf);
						}
					}
					customFieldDAO.saveCustomFieldsForEvent(eventId, customFields, conn);
				}

				Part filePart = request.getPart("attachment");
				if (filePart != null && filePart.getSize() > 0) {
					String requiredRole = request.getParameter("requiredRole");
					handleAttachmentUpload(filePart, eventId, requiredRole, adminUser, conn);
				}

				conn.commit();
				logger.info("Transaction for event ID {} committed successfully.", eventId);
				return eventId;

			} catch (Exception e) {
				conn.rollback();
				logger.error("Error in event service transaction. Rolling back.", e);
				return 0;
			}
		} catch (SQLException e) {
			logger.error("Failed to get DB connection for event service transaction.", e);
			return 0;
		}
	}

	public void signOffUserFromRunningEvent(int userId, String username, int eventId, String reason) {
		eventDAO.signOffFromEvent(userId, eventId);

		Event event = eventDAO.getEventById(eventId);
		if (event != null && event.getLeaderUserId() > 0) {
			String notificationMessage = String.format("%s hat sich vom laufenden Event '%s' abgemeldet. Grund: %s",
					username, event.getName(), reason);

			Map<String, Object> payload = Map.of("type", "alert", "payload",
					Map.of("message", notificationMessage, "url", "/veranstaltungen/details?id=" + eventId));

			notificationService.sendNotificationToUser(event.getLeaderUserId(), payload);
			logger.info("Sent sign-off notification to event leader (ID: {}) for event '{}'", event.getLeaderUserId(),
					event.getName());
		}
	}

	private void handleAttachmentUpload(Part filePart, int eventId, String requiredRole, User adminUser,
			Connection conn) throws IOException, SQLException {
		String uploadDir = configService.getProperty("upload.directory") + File.separator + "events";
		new File(uploadDir).mkdirs();

		String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
		File targetFile = new File(uploadDir, fileName);
		filePart.write(targetFile.getAbsolutePath());

		Attachment attachment = new Attachment();
		attachment.setParentId(eventId);
		attachment.setParentType("EVENT");
		attachment.setFilename(fileName);
		attachment.setFilepath("events/" + fileName);
		attachment.setRequiredRole(requiredRole);

		if (attachmentDAO.addAttachment(attachment, conn)) {
			adminLogService.log(adminUser.getUsername(), "ADD_EVENT_ATTACHMENT",
					"Anhang '" + fileName + "' zu Event ID " + eventId + " hinzugefügt.");
		} else {
			throw new SQLException("Failed to save attachment to database.");
		}
	}
}