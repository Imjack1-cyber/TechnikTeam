// src/main/java/de/technikteam/api/v1/EventResource.java
package de.technikteam.api.v1;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.*;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import de.technikteam.model.ApiResponse;
import de.technikteam.service.AchievementService;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.EventService;
import de.technikteam.service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@MultipartConfig(maxFileSize = 41943040, // 40MB
		maxRequestSize = 83886080, // 80MB
		fileSizeThreshold = 1048576 // 1MB
)
public class EventResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(EventResource.class);

	private final EventDAO eventDAO;
	private final EventService eventService;
	private final AdminLogService adminLogService;
	private final AchievementService achievementService;
	private final NotificationService notificationService;
	private final Gson gson;

	@Inject
	public EventResource(EventDAO eventDAO, EventService eventService, AdminLogService adminLogService,
			AchievementService achievementService, NotificationService notificationService, Gson gson) {
		this.eventDAO = eventDAO;
		this.eventService = eventService;
		this.adminLogService = adminLogService;
		this.achievementService = achievementService;
		this.notificationService = notificationService;
		this.gson = gson;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("EVENT_READ")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			// GET /api/v1/events
			List<Event> events = eventDAO.getAllEvents();
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Events retrieved successfully", events));
		} else {
			String[] pathParts = pathInfo.substring(1).split("/");
			Integer eventId = parseId(pathParts[0]);
			if (eventId == null) {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid event ID format.");
				return;
			}

			if (pathParts.length == 1) {
				// GET /api/v1/events/{id}
				Event event = eventDAO.getEventById(eventId);
				if (event != null) {
					sendJsonResponse(resp, HttpServletResponse.SC_OK,
							new ApiResponse(true, "Event retrieved successfully", event));
				} else {
					sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Event not found");
				}
			} else if (pathParts.length == 2 && "potential-crew".equals(pathParts[1])) {
				// GET /api/v1/events/{id}/potential-crew
				List<User> users = eventDAO.getQualifiedAndAvailableUsersForEvent(eventId);
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Potential crew retrieved", users));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		String pathInfo = req.getPathInfo();
		boolean isUpdate = (pathInfo != null && !pathInfo.equals("/"));

		String requiredPermission = isUpdate ? "EVENT_UPDATE" : "EVENT_CREATE";
		if (adminUser == null || !adminUser.getPermissions().contains(requiredPermission)) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		// POST /api/v1/events/{id}/invitations
		if (isUpdate) {
			String[] pathParts = pathInfo.substring(1).split("/");
			if (pathParts.length == 2 && "invitations".equals(pathParts[1])) {
				handleInviteUsers(req, resp, adminUser, parseId(pathParts[0]));
				return;
			}
		}

		// POST /api/v1/events OR POST /api/v1/events/{id} (for update)
		handleCreateOrUpdate(req, resp, adminUser, isUpdate);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("EVENT_UPDATE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing event ID.");
			return;
		}

		String[] pathParts = pathInfo.substring(1).split("/");
		Integer eventId = parseId(pathParts[0]);
		if (eventId == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid event ID format.");
			return;
		}

		if (pathParts.length == 2 && "status".equals(pathParts[1])) {
			handleStatusUpdate(req, resp, adminUser, eventId);
		} else if (pathParts.length == 2 && "assignments".equals(pathParts[1])) {
			handleAssignUsers(req, resp, adminUser, eventId);
		} else {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("EVENT_DELETE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		Integer eventId = parseIdFromPath(req.getPathInfo());
		if (eventId == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid event ID in URL.");
			return;
		}

		Event event = eventDAO.getEventById(eventId);
		if (event == null) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Event to delete not found.");
			return;
		}

		if (eventDAO.deleteEvent(eventId)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_EVENT_API",
					"Event '" + event.getName() + "' (ID: " + eventId + ") deleted via API.");
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Event deleted successfully", Map.of("deletedId", eventId)));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete event.");
		}
	}

	private void handleCreateOrUpdate(HttpServletRequest request, HttpServletResponse response, User adminUser,
			boolean isUpdate) throws IOException, ServletException {
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
				int eventId = parseIdFromPath(request.getPathInfo());
				Event originalEvent = eventDAO.getEventById(eventId);
				event.setId(eventId);
				event.setStatus(originalEvent.getStatus()); // Preserve status
			}

			int eventId = eventService.createOrUpdateEvent(event, isUpdate, adminUser, request);
			if (eventId > 0) {
				Event resultEvent = eventDAO.getEventById(eventId);
				sendJsonResponse(response, isUpdate ? HttpServletResponse.SC_OK : HttpServletResponse.SC_CREATED,
						new ApiResponse(true, "Event saved successfully", resultEvent));
			} else {
				sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Event could not be saved.");
			}
		} catch (DateTimeParseException | NumberFormatException e) {
			sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid data format for date or number.");
		} catch (Exception e) {
			logger.error("Error during event create/update via API", e);
			sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"An internal error occurred: " + e.getMessage());
		}
	}

	private void handleStatusUpdate(HttpServletRequest req, HttpServletResponse resp, User adminUser, int eventId)
			throws IOException {
		String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		Map<String, String> payload = gson.fromJson(jsonPayload, new TypeToken<Map<String, String>>() {
		}.getType());
		String newStatus = payload.get("status");

		if (newStatus == null || newStatus.isEmpty()) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing 'status' in request body.");
			return;
		}

		Event event = eventDAO.getEventById(eventId);
		if (event != null && eventDAO.updateEventStatus(eventId, newStatus)) {
			adminLogService.log(adminUser.getUsername(), "UPDATE_EVENT_STATUS_API",
					"Status for Event '" + event.getName() + "' set to '" + newStatus + "' via API.");
			if ("ABGESCHLOSSEN".equals(newStatus)) {
				List<User> assignedUsers = eventDAO.getAssignedUsersForEvent(eventId);
				assignedUsers.forEach(user -> achievementService.checkAndGrantAchievements(user, "EVENT_COMPLETED"));
			}
			notificationService.broadcastUIUpdate("event_status_updated",
					Map.of("eventId", eventId, "newStatus", newStatus));
			sendJsonResponse(resp, HttpServletResponse.SC_OK, new ApiResponse(true, "Event status updated.", null));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update event status.");
		}
	}

	private void handleAssignUsers(HttpServletRequest req, HttpServletResponse resp, User adminUser, int eventId)
			throws IOException {
		String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		Map<String, List<String>> payload = gson.fromJson(jsonPayload, new TypeToken<Map<String, List<String>>>() {
		}.getType());
		String[] userIds = payload.get("userIds").toArray(new String[0]);

		eventDAO.assignUsersToEvent(eventId, userIds);
		adminLogService.log(adminUser.getUsername(), "ASSIGN_TEAM_API",
				"Team for event ID " + eventId + " updated via API.");
		sendJsonResponse(resp, HttpServletResponse.SC_OK, new ApiResponse(true, "Team assigned successfully.", null));
	}

	private void handleInviteUsers(HttpServletRequest req, HttpServletResponse resp, User adminUser, int eventId)
			throws IOException {
		Event event = eventDAO.getEventById(eventId);
		if (event == null) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Event not found.");
			return;
		}

		String[] userIdsToInvite = req.getParameterValues("userIds");
		if (userIdsToInvite != null) {
			Arrays.stream(userIdsToInvite).forEach(userIdStr -> {
				try {
					notificationService.sendEventInvitation(Integer.parseInt(userIdStr), event.getName(), eventId);
				} catch (NumberFormatException e) {
					logger.warn("Invalid user ID '{}' found when sending invitations.", userIdStr);
				}
			});
			adminLogService.log(adminUser.getUsername(), "INVITE_CREW_API",
					"Sent " + userIdsToInvite.length + " invitations for event '" + event.getName() + "' via API.");
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, userIdsToInvite.length + " invitations sent.", null));
		} else {
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "No users selected to invite.", null));
		}
	}

	private Integer parseId(String pathSegment) {
		try {
			return Integer.parseInt(pathSegment);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Integer parseIdFromPath(String pathInfo) {
		if (pathInfo == null || pathInfo.length() <= 1)
			return null;
		return parseId(pathInfo.substring(1));
	}

	private void sendJsonResponse(HttpServletResponse resp, int statusCode, ApiResponse apiResponse)
			throws IOException {
		resp.setStatus(statusCode);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		try (PrintWriter out = resp.getWriter()) {
			out.print(gson.toJson(apiResponse));
			out.flush();
		}
	}

	private void sendJsonError(HttpServletResponse resp, int statusCode, String message) throws IOException {
		sendJsonResponse(resp, statusCode, new ApiResponse(false, message, null));
	}
}