// src/main/java/de/technikteam/api/v1/public_api/PublicEventResource.java
package de.technikteam.api.v1.public_api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.*;
import de.technikteam.model.*;
import de.technikteam.service.EventService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class PublicEventResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(PublicEventResource.class);

	private final EventDAO eventDAO;
	private final EventTaskDAO taskDAO;
	private final EventChatDAO chatDAO;
	private final AttachmentDAO attachmentDAO;
	private final EventCustomFieldDAO customFieldDAO;
	private final EventService eventService;
	private final Gson gson;

	@Inject
	public PublicEventResource(EventDAO eventDAO, EventTaskDAO taskDAO, EventChatDAO chatDAO,
			AttachmentDAO attachmentDAO, EventCustomFieldDAO customFieldDAO, EventService eventService, Gson gson) {
		this.eventDAO = eventDAO;
		this.taskDAO = taskDAO;
		this.chatDAO = chatDAO;
		this.attachmentDAO = attachmentDAO;
		this.customFieldDAO = customFieldDAO;
		this.eventService = eventService;
		this.gson = gson;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User user = (User) req.getAttribute("user");
		if (user == null) {
			sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
			return;
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			handleGetEventList(req, resp, user);
		} else {
			handleGetEventDetails(req, resp, user, pathInfo);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User user = (User) req.getAttribute("user");
		if (user == null) {
			sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
			return;
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
			return;
		}

		String[] pathParts = pathInfo.substring(1).split("/");

		try {
			int eventId = Integer.parseInt(pathParts[0]);
			String action = pathParts.length > 1 ? pathParts[1] : "";

			if ("signup".equals(action)) {
				handleSignup(req, resp, user, eventId);
			} else if ("signoff".equals(action)) {
				handleSignoff(req, resp, user, eventId);
			} else if ("tasks".equals(action) && pathParts.length == 4) {
				int taskId = Integer.parseInt(pathParts[2]);
				String taskAction = pathParts[3];
				handleTaskAction(req, resp, user, eventId, taskId, taskAction);
			} else {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid action or URL format.");
			}
		} catch (NumberFormatException e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid event or task ID format.");
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User user = (User) req.getAttribute("user");
		if (user == null) {
			sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
			return;
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
			return;
		}

		String[] pathParts = pathInfo.substring(1).split("/");
		if (pathParts.length != 3 || !pathParts[1].equals("tasks") || !pathParts[2].endsWith("status")) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST,
					"Invalid URL format for PUT. Expected /api/v1/public/events/{eventId}/tasks/{taskId}/status");
			return;
		}

		try {
			int eventId = Integer.parseInt(pathParts[0]);
			int taskId = Integer.parseInt(pathParts[2].replace("/status", ""));
			handleTaskStatusUpdate(req, resp, user, eventId, taskId);
		} catch (NumberFormatException e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid event or task ID.");
		}
	}

	private void handleTaskAction(HttpServletRequest req, HttpServletResponse resp, User user, int eventId, int taskId,
			String action) throws IOException {
		Event event = eventDAO.getEventById(eventId);
		if (event == null || !eventDAO.isUserAssociatedWithEvent(eventId, user.getId())) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
			return;
		}

		boolean success = false;
		if ("claim".equals(action)) {
			success = taskDAO.claimTask(taskId, user.getId());
		} else if ("unclaim".equals(action)) {
			success = taskDAO.unclaimTask(taskId, user.getId());
		}

		if (success) {
			sendJsonResponse(resp, HttpServletResponse.SC_OK, new ApiResponse(true, "Task action successful.", null));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to perform task action.");
		}
	}

	private void handleTaskStatusUpdate(HttpServletRequest req, HttpServletResponse resp, User user, int eventId,
			int taskId) throws IOException {
		Event event = eventDAO.getEventById(eventId);
		boolean isLeader = event != null && event.getLeaderUserId() == user.getId();
		boolean isTaskAssignee = taskDAO.isUserAssignedToTask(taskId, user.getId());

		if (event == null || (!user.hasAdminAccess() && !isLeader && !isTaskAssignee)) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
			return;
		}

		String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		Map<String, String> payload = gson.fromJson(jsonPayload, new TypeToken<Map<String, String>>() {
		}.getType());
		String status = payload.get("status");

		if (taskDAO.updateTaskStatus(taskId, status)) {
			sendJsonResponse(resp, HttpServletResponse.SC_OK, new ApiResponse(true, "Task status updated.", null));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update task status.");
		}
	}

	private void handleSignup(HttpServletRequest req, HttpServletResponse resp, User user, int eventId)
			throws IOException {
		eventDAO.signUpForEvent(user.getId(), eventId);

		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			Type type = new TypeToken<Map<String, String>>() {
			}.getType();
			Map<String, String> customFieldResponses = gson.fromJson(jsonPayload, type);

			if (customFieldResponses != null) {
				for (Map.Entry<String, String> entry : customFieldResponses.entrySet()) {
					String key = entry.getKey();
					if (key.startsWith("customfield_")) {
						int fieldId = Integer.parseInt(key.substring("customfield_".length()));
						EventCustomFieldResponse customResponse = new EventCustomFieldResponse();
						customResponse.setFieldId(fieldId);
						customResponse.setUserId(user.getId());
						customResponse.setResponseValue(entry.getValue());
						customFieldDAO.saveResponse(customResponse);
					}
				}
			}
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Successfully signed up for event.", null));
		} catch (Exception e) {
			logger.error("Error processing signup custom fields for user {} event {}", user.getId(), eventId, e);
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Successfully signed up, but there was an issue with custom fields.", null));
		}
	}

	private void handleSignoff(HttpServletRequest req, HttpServletResponse resp, User user, int eventId)
			throws IOException {
		Event event = eventDAO.getEventById(eventId);
		if ("LAUFEND".equals(event.getStatus())) {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			Map<String, String> payload = gson.fromJson(jsonPayload, new TypeToken<Map<String, String>>() {
			}.getType());
			String reason = payload.get("reason");
			if (reason == null || reason.trim().isEmpty()) {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST,
						"A reason is required to sign off from a running event.");
				return;
			}
			eventService.signOffUserFromRunningEvent(user.getId(), user.getUsername(), eventId, reason);
		} else {
			eventDAO.signOffFromEvent(user.getId(), eventId);
		}
		sendJsonResponse(resp, HttpServletResponse.SC_OK,
				new ApiResponse(true, "Successfully signed off from event.", null));
	}

	private void handleGetEventList(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
		try {
			List<Event> allUpcomingEvents = eventDAO.getAllActiveAndUpcomingEvents();
			List<Event> qualifiedEvents = eventDAO.getUpcomingEventsForUser(user, 0);
			List<Integer> qualifiedEventIds = qualifiedEvents.stream().map(Event::getId).collect(Collectors.toList());

			for (Event event : allUpcomingEvents) {
				event.setUserQualified(qualifiedEventIds.contains(event.getId()));
				qualifiedEvents.stream().filter(qe -> qe.getId() == event.getId()).findFirst()
						.ifPresent(qe -> event.setUserAttendanceStatus(qe.getUserAttendanceStatus()));
				if (event.getUserAttendanceStatus() == null) {
					event.setUserAttendanceStatus("OFFEN");
				}
			}
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Events retrieved.", allUpcomingEvents));
		} catch (Exception e) {
			logger.error("Error fetching public event list", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not retrieve event list.");
		}
	}

	private void handleGetEventDetails(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo)
			throws IOException {
		try {
			int eventId = Integer.parseInt(pathInfo.substring(1));
			Event event = eventDAO.getEventById(eventId);

			if (event == null) {
				sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Event not found.");
				return;
			}

			boolean isGlobalAdmin = user.hasAdminAccess();
			boolean isEventLeader = user.getId() == event.getLeaderUserId();
			boolean isAssociated = eventDAO.isUserAssociatedWithEvent(eventId, user.getId());

			if (!isGlobalAdmin && !isEventLeader && !isAssociated) {
				sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "You are not authorized to view this event.");
				return;
			}

			String userRoleForAttachments = (isGlobalAdmin || isEventLeader) ? "ADMIN" : "NUTZER";
			event.setAttachments(attachmentDAO.getAttachmentsForParent("EVENT", eventId, userRoleForAttachments));
			event.setSkillRequirements(eventDAO.getSkillRequirementsForEvent(eventId));
			event.setReservedItems(eventDAO.getReservedItemsForEvent(eventId));
			event.setAssignedAttendees(eventDAO.getAssignedUsersForEvent(eventId));
			event.setEventTasks(taskDAO.getTasksForEvent(eventId));
			if ("LAUFEND".equalsIgnoreCase(event.getStatus())) {
				event.setChatMessages(chatDAO.getMessagesForEvent(eventId));
			} else {
				event.setChatMessages(new ArrayList<>());
			}
			sendJsonResponse(resp, HttpServletResponse.SC_OK, new ApiResponse(true, "Event details retrieved.", event));
		} catch (NumberFormatException e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid event ID format.");
		}
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