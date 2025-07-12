package de.technikteam.servlet.admin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.technikteam.service.AchievementService;
import de.technikteam.service.NotificationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.technikteam.config.AppConfig;
import de.technikteam.config.LocalDateAdapter;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.AttachmentDAO;
import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.EventCustomFieldDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.InventoryKitDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.Attachment;
import de.technikteam.model.Course;
import de.technikteam.model.Event;
import de.technikteam.model.EventCustomField;
import de.technikteam.model.InventoryKit;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/admin/veranstaltungen")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 40, maxRequestSize = 1024 * 1024 * 80)
public class AdminEventServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminEventServlet.class);

	private EventDAO eventDAO;
	private CourseDAO courseDAO;
	private StorageDAO storageDAO;
	private UserDAO userDAO;
	private AttachmentDAO attachmentDAO;
	private EventCustomFieldDAO customFieldDAO;
	private InventoryKitDAO kitDAO;
	private Gson gson;

	@Override
	public void init() {
		eventDAO = new EventDAO();
		courseDAO = new CourseDAO();
		storageDAO = new StorageDAO();
		userDAO = new UserDAO();
		attachmentDAO = new AttachmentDAO();
		customFieldDAO = new EventCustomFieldDAO();
		kitDAO = new InventoryKitDAO();
		gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
				.registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter()).setPrettyPrinting().create();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User user = (User) req.getSession().getAttribute("user");
		if (!user.getPermissions().contains("EVENT_READ") && !user.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String action = req.getParameter("action") == null ? "list" : req.getParameter("action");
		logger.debug("AdminEventServlet received GET with action: {}", action);
		try {
			switch (action) {
			case "getEventData":
				getEventDataAsJson(req, resp);
				break;
			case "getAssignmentData":
				getAssignmentDataAsJson(req, resp);
				break;
			default:
				listEvents(req, resp);
				break;
			}
		} catch (Exception e) {
			logger.error("Error in AdminEventServlet doGet", e);
			req.getSession().setAttribute("errorMessage", "Ein Fehler ist aufgetreten: " + e.getMessage());
			resp.sendRedirect(req.getContextPath() + "/admin/veranstaltungen");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		req.setCharacterEncoding("UTF-8");

		if (!CSRFUtil.isTokenValid(req)) {
			logger.warn("CSRF token validation failed for event action.");
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}

		String action = req.getParameter("action");

		logger.debug("AdminEventServlet received POST with action: {}", action);

		switch (action) {
		case "create":
		case "update":
			handleCreateOrUpdate(req, resp);
			break;
		case "delete":
			handleDelete(req, resp);
			break;
		case "assignUsers":
			handleAssignUsers(req, resp);
			break;
		case "updateStatus":
			handleStatusUpdate(req, resp);
			break;
		case "deleteAttachment":
			handleDeleteAttachment(req, resp);
			break;
		case "inviteUsers":
			handleInviteUsers(req, resp);
			break;
		default:
			logger.warn("Unknown POST action received: {}", action);
			resp.sendRedirect(req.getContextPath() + "/admin/veranstaltungen");
			break;
		}
	}

	private void listEvents(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.info("Listing all events for admin view.");
		List<Event> eventList = eventDAO.getAllEvents();
		List<Course> allCourses = courseDAO.getAllCourses();
		List<StorageItem> allItems = storageDAO.getAllItems();
		List<User> allUsers = userDAO.getAllUsers();
		List<InventoryKit> allKits = kitDAO.getAllKits();

		req.setAttribute("eventList", eventList);
		req.setAttribute("allUsers", allUsers);
		req.setAttribute("allKits", allKits);
		req.setAttribute("allCoursesJson", gson.toJson(allCourses));
		req.setAttribute("allItemsJson", gson.toJson(allItems));
		req.setAttribute("allKitsJson", gson.toJson(allKits));

		req.getRequestDispatcher("/views/admin/admin_events_list.jsp").forward(req, resp);
	}

	private void getEventDataAsJson(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			int eventId = Integer.parseInt(req.getParameter("id"));
			Event event = eventDAO.getEventById(eventId);
			if (event != null) {
				event.setSkillRequirements(eventDAO.getSkillRequirementsForEvent(eventId));
				event.setReservedItems(eventDAO.getReservedItemsForEvent(eventId));
				event.setAttachments(attachmentDAO.getAttachmentsForParent("EVENT", eventId, "ADMIN"));
				event.setCustomFields(customFieldDAO.getCustomFieldsForEvent(eventId));
				String eventJson = gson.toJson(event);
				resp.setContentType("application/json");
				resp.setCharacterEncoding("UTF-8");
				resp.getWriter().write(eventJson);
			} else {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		} catch (NumberFormatException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	private void getAssignmentDataAsJson(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			int eventId = Integer.parseInt(req.getParameter("id"));
			List<User> signedUpUsers = eventDAO.getSignedUpUsersForEvent(eventId);
			Set<Integer> assignedUserIds = eventDAO.getAssignedUsersForEvent(eventId).stream().map(User::getId)
					.collect(Collectors.toSet());

			Map<String, Object> responseData = new HashMap<>();
			responseData.put("signedUpUsers", signedUpUsers);
			responseData.put("assignedUserIds", assignedUserIds);

			resp.setContentType("application/json");
			resp.setCharacterEncoding("UTF-8");
			resp.getWriter().write(gson.toJson(responseData));

		} catch (NumberFormatException e) {
			logger.error("Invalid event ID for assignment data.", e);
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Event-ID.");
		}
	}

	private void handleCreateOrUpdate(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		User adminUser = (User) request.getSession().getAttribute("user");
		String idParam = request.getParameter("id");
		boolean isUpdate = idParam != null && !idParam.isEmpty();

		boolean hasPermission = false;
		if (isUpdate) {
			int eventId = Integer.parseInt(idParam);
			Event event = eventDAO.getEventById(eventId);
			boolean isLeader = event != null && event.getLeaderUserId() == adminUser.getId();
			hasPermission = adminUser.getPermissions().contains("EVENT_UPDATE")
					|| adminUser.getPermissions().contains("ACCESS_ADMIN_PANEL") || isLeader;
		} else {
			hasPermission = adminUser.getPermissions().contains("EVENT_CREATE")
					|| adminUser.getPermissions().contains("ACCESS_ADMIN_PANEL");
		}

		if (!hasPermission) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		Event event = new Event();

		try {
			event.setName(request.getParameter("name"));
			event.setDescription(request.getParameter("description"));
			event.setLocation(request.getParameter("location"));
			event.setEventDateTime(LocalDateTime.parse(request.getParameter("eventDateTime")));

			String endDateTimeParam = request.getParameter("endDateTime");
			if (endDateTimeParam != null && !endDateTimeParam.isEmpty()) {
				event.setEndDateTime(LocalDateTime.parse(endDateTimeParam));
			}

			String leaderIdStr = request.getParameter("leaderUserId");
			if (leaderIdStr != null && !leaderIdStr.isEmpty()) {
				event.setLeaderUserId(Integer.parseInt(leaderIdStr));
			}

			int eventId = 0;
			if (isUpdate) {
				eventId = Integer.parseInt(idParam);
				Event originalEvent = eventDAO.getEventById(eventId);
				event.setId(eventId);
				event.setStatus(originalEvent.getStatus());
				if (eventDAO.updateEvent(event)) {
					AdminLogService.log(adminUser.getUsername(), "UPDATE_EVENT",
							"Event '" + event.getName() + "' (ID: " + eventId + ") aktualisiert.");
				}
			} else {
				eventId = eventDAO.createEvent(event);
				if (eventId > 0) {
					event.setId(eventId);
					AdminLogService.log(adminUser.getUsername(), "CREATE_EVENT",
							"Event '" + event.getName() + "' (ID: " + eventId + ") erstellt.");
				}
			}

			if (eventId > 0) {
				String[] requiredCourseIds = request.getParameterValues("requiredCourseId");
				String[] requiredPersons = request.getParameterValues("requiredPersons");
				eventDAO.saveSkillRequirements(eventId, requiredCourseIds, requiredPersons);

				String[] itemIds = request.getParameterValues("itemId");
				String[] quantities = request.getParameterValues("itemQuantity");
				eventDAO.saveReservations(eventId, itemIds, quantities);

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
					customFieldDAO.saveCustomFieldsForEvent(eventId, customFields);
				}

				Part filePart = request.getPart("attachment");
				if (filePart != null && filePart.getSize() > 0) {
					String requiredRole = request.getParameter("requiredRole");
					handleAttachmentUpload(filePart, eventId, requiredRole, adminUser, request);
				}
				request.getSession().setAttribute("successMessage", "Event erfolgreich gespeichert.");
			} else {
				request.getSession().setAttribute("errorMessage", "Event konnte nicht gespeichert werden.");
			}

		} catch (DateTimeParseException e) {
			logger.error("Invalid date format submitted for event.", e);
			request.getSession().setAttribute("errorMessage",
					"Ungültiges Datumsformat. Bitte das Format 'YYYY-MM-DDTHH:MM' verwenden.");
		} catch (Exception e) {
			logger.error("Error during event creation/update.", e);
			request.getSession().setAttribute("errorMessage",
					"Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage());
		}

		response.sendRedirect(request.getContextPath() + "/admin/veranstaltungen");
	}

	private void handleAttachmentUpload(Part filePart, int eventId, String requiredRole, User adminUser,
			HttpServletRequest req) throws IOException {
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

		if (attachmentDAO.addAttachment(attachment)) {
			AdminLogService.log(adminUser.getUsername(), "ADD_EVENT_ATTACHMENT",
					"Anhang '" + fileName + "' zu Event ID " + eventId + " hinzugefügt.");
		} else {
			req.getSession().setAttribute("errorMessage", "Anhang konnte nicht in DB gespeichert werden.");
		}
	}

	private void handleDeleteAttachment(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User adminUser = (User) req.getSession().getAttribute("user");
		int attachmentId = Integer.parseInt(req.getParameter("id"));
		Attachment attachment = attachmentDAO.getAttachmentById(attachmentId);

		if (attachment == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Anhang nicht gefunden.");
			return;
		}

		Event event = eventDAO.getEventById(attachment.getParentId());
		boolean isLeader = event != null && event.getLeaderUserId() == adminUser.getId();
		boolean hasPermission = adminUser.getPermissions().contains("EVENT_UPDATE")
				|| adminUser.getPermissions().contains("ACCESS_ADMIN_PANEL") || isLeader;

		if (!hasPermission) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		logger.warn("Attempting to delete event attachment ID {}", attachmentId);

		File physicalFile = new File(AppConfig.UPLOAD_DIRECTORY, attachment.getFilepath());
		if (physicalFile.exists())
			physicalFile.delete();

		if (attachmentDAO.deleteAttachment(attachmentId)) {
			AdminLogService.log(adminUser.getUsername(), "DELETE_ATTACHMENT", "Anhang '" + attachment.getFilename()
					+ "' von Event ID " + attachment.getParentId() + " gelöscht.");
			resp.setContentType("application/json");
			resp.getWriter().write("{\"message\":\"Anhang gelöscht\"}");
		} else {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Anhang konnte nicht aus DB gelöscht werden.");
		}
	}

	private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User adminUser = (User) req.getSession().getAttribute("user");
		if (!adminUser.getPermissions().contains("EVENT_DELETE")
				&& !adminUser.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		try {
			int eventId = Integer.parseInt(req.getParameter("id"));
			logger.warn("Attempting to delete event with ID: {}", eventId);
			Event event = eventDAO.getEventById(eventId);
			if (event != null && eventDAO.deleteEvent(eventId)) {
				AdminLogService.log(adminUser.getUsername(), "DELETE_EVENT",
						"Event '" + event.getName() + "' (ID: " + eventId + ") endgültig gelöscht.");
				req.getSession().setAttribute("successMessage", "Event wurde gelöscht.");
			} else {
				req.getSession().setAttribute("errorMessage", "Event konnte nicht gelöscht werden.");
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid event ID format for deletion.", e);
			req.getSession().setAttribute("errorMessage", "Ungültige Event-ID.");
		}
		resp.sendRedirect(req.getContextPath() + "/admin/veranstaltungen");
	}

	private void handleAssignUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User adminUser = (User) req.getSession().getAttribute("user");
		int eventId = Integer.parseInt(req.getParameter("eventId"));
		Event event = eventDAO.getEventById(eventId);
		boolean isLeader = event != null && event.getLeaderUserId() == adminUser.getId();

		if (!adminUser.getPermissions().contains("EVENT_MANAGE_ASSIGNMENTS")
				&& !adminUser.getPermissions().contains("ACCESS_ADMIN_PANEL") && !isLeader) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		try {
			String[] userIds = req.getParameterValues("userIds");
			logger.info("Assigning {} users to event ID {}", (userIds != null ? userIds.length : 0), eventId);
			eventDAO.assignUsersToEvent(eventId, userIds);

			String assignedUserCount = (userIds != null) ? String.valueOf(userIds.length) : "0";
			String logDetails = String.format("Team für Event '%s' (ID: %d) finalisiert. %s Benutzer zugewiesen.",
					event.getName(), eventId, assignedUserCount);
			AdminLogService.log(adminUser.getUsername(), "ASSIGN_TEAM", logDetails);

			req.getSession().setAttribute("successMessage", "Team für das Event wurde erfolgreich zugewiesen.");
		} catch (NumberFormatException e) {
			logger.error("Invalid event ID format for user assignment.", e);
			req.getSession().setAttribute("errorMessage", "Ungültige Event-ID.");
		}
		resp.sendRedirect(req.getContextPath() + "/admin/veranstaltungen");
	}

	private void handleStatusUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User adminUser = (User) req.getSession().getAttribute("user");
		int eventId = Integer.parseInt(req.getParameter("id"));
		Event event = eventDAO.getEventById(eventId);
		boolean isLeader = event != null && event.getLeaderUserId() == adminUser.getId();

		if (!adminUser.getPermissions().contains("EVENT_UPDATE")
				&& !adminUser.getPermissions().contains("ACCESS_ADMIN_PANEL") && !isLeader) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		try {
			String newStatus = req.getParameter("newStatus");
			logger.info("Updating status for event ID {} to '{}'", eventId, newStatus);
			if (event != null && eventDAO.updateEventStatus(eventId, newStatus)) {
				String logDetails = String.format("Status für Event '%s' (ID: %d) von '%s' auf '%s' geändert.",
						event.getName(), eventId, event.getStatus(), newStatus);
				AdminLogService.log(adminUser.getUsername(), "UPDATE_EVENT_STATUS", logDetails);

				if ("ABGESCHLOSSEN".equals(newStatus)) {
					List<User> assignedUsers = eventDAO.getAssignedUsersForEvent(eventId);
					for (User user : assignedUsers) {
						AchievementService.getInstance().checkAndGrantAchievements(user, "EVENT_COMPLETED");
					}
				}

				req.getSession().setAttribute("successMessage", "Event-Status erfolgreich aktualisiert.");
			} else {
				req.getSession().setAttribute("errorMessage", "Fehler beim Aktualisieren des Event-Status.");
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid event ID format for status update.", e);
			req.getSession().setAttribute("errorMessage", "Ungültige Event-ID.");
		}
		resp.sendRedirect(req.getContextPath() + "/admin/veranstaltungen");
	}

	private void handleInviteUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User adminUser = (User) req.getSession().getAttribute("user");
		int eventId = Integer.parseInt(req.getParameter("eventId"));
		String[] userIdsToInvite = req.getParameterValues("userIds");

		Event event = eventDAO.getEventById(eventId);
		if (event == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Event not found.");
			return;
		}

		boolean hasPermission = adminUser.getPermissions().contains("EVENT_MANAGE_ASSIGNMENTS")
				|| adminUser.getPermissions().contains("ACCESS_ADMIN_PANEL")
				|| adminUser.getId() == event.getLeaderUserId();

		if (!hasPermission) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
			return;
		}

		if (userIdsToInvite != null) {
			for (String userIdStr : userIdsToInvite) {
				try {
					int userId = Integer.parseInt(userIdStr);
					NotificationService.getInstance().sendEventInvitation(userId, event.getName(), eventId);
				} catch (NumberFormatException e) {
					logger.warn("Invalid user ID '{}' found when sending invitations.", userIdStr);
				}
			}
			req.getSession().setAttribute("successMessage",
					"Einladungen an " + userIdsToInvite.length + " Benutzer gesendet.");
			AdminLogService.log(adminUser.getUsername(), "INVITE_CREW", "Einladungen für Event '" + event.getName()
					+ "' an " + userIdsToInvite.length + " Benutzer gesendet.");
		} else {
			req.getSession().setAttribute("infoMessage", "Keine Benutzer zum Einladen ausgewählt.");
		}

		resp.sendRedirect(req.getContextPath() + "/admin/veranstaltungen");
	}
}