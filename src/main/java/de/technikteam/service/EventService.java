package de.technikteam.service;

import de.technikteam.config.AppConfig;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for handling complex business logic related to Events,
 * including transactional operations that span multiple DAOs.
 */
public class EventService {
    private static final Logger logger = LogManager.getLogger(EventService.class);

    private final EventDAO eventDAO;
    private final AttachmentDAO attachmentDAO;
    private final EventCustomFieldDAO customFieldDAO;

    public EventService() {
        this.eventDAO = new EventDAO();
        this.attachmentDAO = new AttachmentDAO();
        this.customFieldDAO = new EventCustomFieldDAO();
    }

    /**
     * Creates or updates an event and all its related entities within a single database transaction.
     *
     * @param event The event object to save.
     * @param isUpdate A flag indicating if this is a new event or an update.
     * @param adminUser The admin performing the action.
     * @param request The servlet request containing all parameters and parts.
     * @return The ID of the saved event, or 0 on failure.
     */
    public int createOrUpdateEvent(Event event, boolean isUpdate, User adminUser, HttpServletRequest request) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            int eventId;
            if (isUpdate) {
                if (eventDAO.updateEvent(event, conn)) {
                    eventId = event.getId();
                    AdminLogService.log(adminUser.getUsername(), "UPDATE_EVENT",
                            "Event '" + event.getName() + "' (ID: " + eventId + ") aktualisiert.");
                } else {
                    throw new SQLException("Failed to update the core event record.");
                }
            } else {
                eventId = eventDAO.createEvent(event, conn);
                if (eventId > 0) {
                    event.setId(eventId);
                    AdminLogService.log(adminUser.getUsername(), "CREATE_EVENT",
                            "Event '" + event.getName() + "' (ID: " + eventId + ") erstellt.");
                } else {
                    throw new SQLException("Failed to create the core event record.");
                }
            }

            // Handle associated data within the same transaction
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
                handleAttachmentUpload(filePart, eventId, requiredRole, adminUser, request, conn);
            }

            conn.commit();
            logger.info("Transaction for event ID {} committed successfully.", eventId);
            return eventId;

        } catch (Exception e) {
            logger.error("Error in event service transaction. Rolling back.", e);
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.info("Transaction rolled back successfully.");
                } catch (SQLException ex) {
                    logger.error("Failed to rollback transaction.", ex);
                }
            }
            return 0;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    logger.error("Failed to close connection.", ex);
                }
            }
        }
    }

    private void handleAttachmentUpload(Part filePart, int eventId, String requiredRole, User adminUser, HttpServletRequest req, Connection conn) throws IOException, SQLException {
        String uploadDir = AppConfig.UPLOAD_DIRECTORY + File.separator + "events";
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
            AdminLogService.log(adminUser.getUsername(), "ADD_EVENT_ATTACHMENT",
                    "Anhang '" + fileName + "' zu Event ID " + eventId + " hinzugefügt.");
        } else {
            req.getSession().setAttribute("errorMessage", "Anhang konnte nicht in DB gespeichert werden.");
            throw new SQLException("Failed to save attachment to database.");
        }
    }
}