package de.technikteam.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.EventAttachmentDAO;
import de.technikteam.dao.EventChatDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventTaskDAO;
import de.technikteam.dao.InventoryKitDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet("/veranstaltungen/details")
public class EventDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(EventDetailsServlet.class);
	private EventDAO eventDAO;
	private EventTaskDAO taskDAO;
	private EventChatDAO chatDAO;
	private EventAttachmentDAO attachmentDAO;
	private StorageDAO storageDAO;
	private InventoryKitDAO kitDAO;
	private Gson gson;

	@Override
	public void init() {
		eventDAO = new EventDAO();
		taskDAO = new EventTaskDAO();
		chatDAO = new EventChatDAO();
		attachmentDAO = new EventAttachmentDAO();
		storageDAO = new StorageDAO();
		kitDAO = new InventoryKitDAO();
		gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		try {
			int eventId = Integer.parseInt(request.getParameter("id"));
			logger.info("Event details requested for ID: {} by user '{}'", eventId, user.getUsername());
			Event event = eventDAO.getEventById(eventId);

			if (event == null) {
				logger.warn("Event with ID {} not found. Redirecting to 404.", eventId);
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Event nicht gefunden.");
				return;
			}

			boolean isGlobalAdmin = user.getPermissions().contains("EVENT_MANAGE_TASKS")
					|| user.getPermissions().contains("ACCESS_ADMIN_PANEL");
			boolean isEventLeader = user.getId() == event.getLeaderUserId();
			boolean hasTaskManagementPermission = isGlobalAdmin || isEventLeader;
			request.setAttribute("hasTaskManagementPermission", hasTaskManagementPermission);

			String userRoleForAttachments = (hasTaskManagementPermission) ? "ADMIN" : "NUTZER";

			List<User> assignedUsers = eventDAO.getAssignedUsersForEvent(eventId);
			Set<Integer> assignedUserIds = assignedUsers.stream().map(User::getId).collect(Collectors.toSet());
			boolean isUserAssigned = assignedUserIds.contains(user.getId());
			request.setAttribute("isUserAssigned", isUserAssigned);

			List<User> signedUpUsers = eventDAO.getSignedUpUsersForEvent(eventId);
			boolean isUserParticipant = signedUpUsers.stream().anyMatch(u -> u.getId() == user.getId());
			request.setAttribute("isUserParticipant", isUserParticipant);

			event.setAttachments(attachmentDAO.getAttachmentsForEvent(eventId, userRoleForAttachments));
			event.setSkillRequirements(eventDAO.getSkillRequirementsForEvent(eventId));
			event.setReservedItems(eventDAO.getReservedItemsForEvent(eventId));
			event.setAssignedAttendees(assignedUsers);
			event.setEventTasks(taskDAO.getTasksForEvent(eventId));

			if ("LAUFEND".equalsIgnoreCase(event.getStatus())) {
				event.setChatMessages(chatDAO.getMessagesForEvent(eventId));
			} else {
				event.setChatMessages(new ArrayList<>());
			}

			request.setAttribute("event", event);

			if (hasTaskManagementPermission) {
				request.setAttribute("assignedUsersJson", gson.toJson(assignedUsers));
				request.setAttribute("allItemsJson", gson.toJson(storageDAO.getAllItems()));
				request.setAttribute("allKitsJson", gson.toJson(kitDAO.getAllKits()));
				request.setAttribute("tasksJson", gson.toJson(event.getEventTasks()));
			} else {
				request.setAttribute("assignedUsersJson", "[]");
				request.setAttribute("allItemsJson", "[]");
				request.setAttribute("allKitsJson", "[]");
				request.setAttribute("tasksJson", "[]");
			}

			logger.debug("Forwarding to eventDetails.jsp for event '{}'", event.getName());
			request.getRequestDispatcher("/views/public/eventDetails.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			logger.error("Invalid event ID format in request.", e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Event-ID.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while fetching event details.", e);
			response.sendRedirect(request.getContextPath() + "/error500");
		}
	}
}