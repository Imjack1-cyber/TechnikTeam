package de.technikteam.servlet.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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
import de.technikteam.service.AchievementService;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.EventService;
import de.technikteam.service.NotificationService;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@MultipartConfig(maxFileSize = 41943040, // 40MB
		maxRequestSize = 83886080, // 80MB
		fileSizeThreshold = 1048576 // 1MB
)
public class AdminEventServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminEventServlet.class);

	private final EventDAO eventDAO;
	private final CourseDAO courseDAO;
	private final StorageDAO storageDAO;
	private final UserDAO userDAO;
	private final AttachmentDAO attachmentDAO;
	private final EventCustomFieldDAO customFieldDAO;
	private final InventoryKitDAO kitDAO;
	private final EventService eventService;
	private final AdminLogService adminLogService;
	private final AchievementService achievementService;
	private final Gson gson;

	@Inject
	public AdminEventServlet(EventDAO eventDAO, CourseDAO courseDAO, StorageDAO storageDAO, UserDAO userDAO,
			AttachmentDAO attachmentDAO, EventCustomFieldDAO customFieldDAO, InventoryKitDAO kitDAO,
			EventService eventService, AdminLogService adminLogService, AchievementService achievementService) {
		this.eventDAO = eventDAO;
		this.courseDAO = courseDAO;
		this.storageDAO = storageDAO;
		this.userDAO = userDAO;
		this.attachmentDAO = attachmentDAO;
		this.customFieldDAO = customFieldDAO;
		this.kitDAO = kitDAO;
		this.eventService = eventService;
		this.adminLogService = adminLogService;
		this.achievementService = achievementService;
		this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
				.registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter()).setPrettyPrinting().create();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User user = (User) req.getSession().getAttribute("user");
		if (!user.getPermissions().contains("EVENT_READ") && !user.hasAdminAccess()) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String action = req.getParameter("action") == null ? "list" : req.getParameter("action");
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
		String action = req.getParameter("action");
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
			resp.sendRedirect(req.getContextPath() + "/admin/veranstaltungen");
			break;
		}
	}

	private void listEvents(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Event-ID.");
		}
	}

	private void handleCreateOrUpdate(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if (!CSRFUtil.isTokenValid(request)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}

		User adminUser = (User) request.getSession().getAttribute("user");
		String idParam = request.getParameter("id");
		boolean isUpdate = idParam != null && !idParam.isEmpty();
		boolean hasPermission = isUpdate
				? (adminUser.getPermissions().contains("EVENT_UPDATE") || adminUser.hasAdminAccess())
				: (adminUser.getPermissions().contains("EVENT_CREATE") || adminUser.hasAdminAccess());
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
			if (isUpdate) {
				int eventId = Integer.parseInt(idParam);
				Event originalEvent = eventDAO.getEventById(eventId);
				event.setId(eventId);
				event.setStatus(originalEvent.getStatus());
			}

			int eventId = eventService.createOrUpdateEvent(event, isUpdate, adminUser, request);
			if (eventId > 0) {
				request.getSession().setAttribute("successMessage", "Event erfolgreich gespeichert.");
			} else {
				request.getSession().setAttribute("errorMessage", "Event konnte nicht gespeichert werden.");
			}
		} catch (DateTimeParseException e) {
			request.getSession().setAttribute("errorMessage", "Ungültiges Datumsformat.");
		} catch (Exception e) {
			request.getSession().setAttribute("errorMessage",
					"Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage());
		}
		response.sendRedirect(request.getContextPath() + "/admin/veranstaltungen");
	}

	private void handleDeleteAttachment(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (!CSRFUtil.isTokenValid(req)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}
		User adminUser = (User) req.getSession().getAttribute("user");
		int attachmentId = Integer.parseInt(req.getParameter("id"));
		Attachment attachment = attachmentDAO.getAttachmentById(attachmentId);
		if (attachment == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Anhang nicht gefunden.");
			return;
		}
		if (!adminUser.hasAdminAccess()) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		if (attachmentDAO.deleteAttachment(attachmentId)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_ATTACHMENT", "Anhang '" + attachment.getFilename()
					+ "' von Event ID " + attachment.getParentId() + " gelöscht.");
			resp.setContentType("application/json");
			resp.getWriter().write("{\"message\":\"Anhang gelöscht\"}");
		} else {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Anhang konnte nicht aus DB gelöscht werden.");
		}
	}

	private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (!CSRFUtil.isTokenValid(req)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}
		User adminUser = (User) req.getSession().getAttribute("user");
		if (!adminUser.getPermissions().contains("EVENT_DELETE") && !adminUser.hasAdminAccess()) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}
		try {
			int eventId = Integer.parseInt(req.getParameter("id"));
			Event event = eventDAO.getEventById(eventId);
			if (event != null && eventDAO.deleteEvent(eventId)) {
				adminLogService.log(adminUser.getUsername(), "DELETE_EVENT",
						"Event '" + event.getName() + "' (ID: " + eventId + ") endgültig gelöscht.");
				req.getSession().setAttribute("successMessage", "Event wurde gelöscht.");
			} else {
				req.getSession().setAttribute("errorMessage", "Event konnte nicht gelöscht werden.");
			}
		} catch (NumberFormatException e) {
			req.getSession().setAttribute("errorMessage", "Ungültige Event-ID.");
		}
		resp.sendRedirect(req.getContextPath() + "/admin/veranstaltungen");
	}

	private void handleAssignUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (!CSRFUtil.isTokenValid(req)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}
		User adminUser = (User) req.getSession().getAttribute("user");
		int eventId = Integer.parseInt(req.getParameter("eventId"));
		Event event = eventDAO.getEventById(eventId);
		if (!adminUser.getPermissions().contains("EVENT_MANAGE_ASSIGNMENTS") && !adminUser.hasAdminAccess()) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}
		try {
			String[] userIds = req.getParameterValues("userIds");
			eventDAO.assignUsersToEvent(eventId, userIds);
			String assignedUserCount = (userIds != null) ? String.valueOf(userIds.length) : "0";
			String logDetails = String.format("Team für Event '%s' (ID: %d) finalisiert. %s Benutzer zugewiesen.",
					event.getName(), eventId, assignedUserCount);
			adminLogService.log(adminUser.getUsername(), "ASSIGN_TEAM", logDetails);
			req.getSession().setAttribute("successMessage", "Team für das Event wurde erfolgreich zugewiesen.");
		} catch (NumberFormatException e) {
			req.getSession().setAttribute("errorMessage", "Ungültige Event-ID.");
		}
		resp.sendRedirect(req.getContextPath() + "/admin/veranstaltungen");
	}

	private void handleStatusUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (!CSRFUtil.isTokenValid(req)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}
		User adminUser = (User) req.getSession().getAttribute("user");
		int eventId = Integer.parseInt(req.getParameter("id"));
		Event event = eventDAO.getEventById(eventId);
		if (!adminUser.getPermissions().contains("EVENT_UPDATE") && !adminUser.hasAdminAccess()) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}
		try {
			String newStatus = req.getParameter("newStatus");
			if (event != null && eventDAO.updateEventStatus(eventId, newStatus)) {
				String logDetails = String.format("Status für Event '%s' (ID: %d) von '%s' auf '%s' geändert.",
						event.getName(), eventId, event.getStatus(), newStatus);
				adminLogService.log(adminUser.getUsername(), "UPDATE_EVENT_STATUS", logDetails);
				if ("ABGESCHLOSSEN".equals(newStatus)) {
					List<User> assignedUsers = eventDAO.getAssignedUsersForEvent(eventId);
					for (User user : assignedUsers) {
						achievementService.checkAndGrantAchievements(user, "EVENT_COMPLETED");
					}
				}
				NotificationService.getInstance().broadcastUIUpdate("event_status_updated",
						Map.of("eventId", eventId, "newStatus", newStatus));
				req.getSession().setAttribute("successMessage", "Event-Status erfolgreich aktualisiert.");
			} else {
				req.getSession().setAttribute("errorMessage", "Fehler beim Aktualisieren des Event-Status.");
			}
		} catch (NumberFormatException e) {
			req.getSession().setAttribute("errorMessage", "Ungültige Event-ID.");
		}
		resp.sendRedirect(req.getContextPath() + "/admin/veranstaltungen");
	}

	private void handleInviteUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (!CSRFUtil.isTokenValid(req)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}
		User adminUser = (User) req.getSession().getAttribute("user");
		int eventId = Integer.parseInt(req.getParameter("eventId"));
		String[] userIdsToInvite = req.getParameterValues("userIds");
		Event event = eventDAO.getEventById(eventId);
		if (event == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Event not found.");
			return;
		}
		if (!adminUser.hasAdminAccess()) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
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
			adminLogService.log(adminUser.getUsername(), "INVITE_CREW", "Einladungen für Event '" + event.getName()
					+ "' an " + userIdsToInvite.length + " Benutzer gesendet.");
		} else {
			req.getSession().setAttribute("infoMessage", "Keine Benutzer zum Einladen ausgewählt.");
		}
		resp.sendRedirect(req.getContextPath() + "/admin/veranstaltungen");
	}
}