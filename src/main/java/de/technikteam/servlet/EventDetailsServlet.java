package de.technikteam.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.*;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
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

@Singleton
public class EventDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(EventDetailsServlet.class);
	private final EventDAO eventDAO;
	private final EventTaskDAO taskDAO;
	private final EventChatDAO chatDAO;
	private final AttachmentDAO attachmentDAO;
	private final StorageDAO storageDAO;
	private final InventoryKitDAO kitDAO;
	private final Gson gson;

	@Inject
	public EventDetailsServlet(EventDAO eventDAO, EventTaskDAO taskDAO, EventChatDAO chatDAO,
			AttachmentDAO attachmentDAO, StorageDAO storageDAO, InventoryKitDAO kitDAO) {
		this.eventDAO = eventDAO;
		this.taskDAO = taskDAO;
		this.chatDAO = chatDAO;
		this.attachmentDAO = attachmentDAO;
		this.storageDAO = storageDAO;
		this.kitDAO = kitDAO;
		this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		try {
			int eventId = Integer.parseInt(request.getParameter("id"));
			Event event = eventDAO.getEventById(eventId);

			if (event == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Event nicht gefunden.");
				return;
			}

			boolean isGlobalAdmin = user.getPermissions().contains("EVENT_MANAGE_TASKS") || user.hasAdminAccess();
			boolean isEventLeader = user.getId() == event.getLeaderUserId();
			boolean hasTaskManagementPermission = isGlobalAdmin || isEventLeader;
			request.setAttribute("hasTaskManagementPermission", hasTaskManagementPermission);

			String userRoleForAttachments = (hasTaskManagementPermission) ? "ADMIN" : "NUTZER";
			List<User> assignedUsers = eventDAO.getAssignedUsersForEvent(eventId);
			boolean isUserAssigned = assignedUsers.stream().anyMatch(u -> u.getId() == user.getId());
			request.setAttribute("isUserAssigned", isUserAssigned);

			List<User> signedUpUsers = eventDAO.getSignedUpUsersForEvent(eventId);
			boolean isUserParticipant = signedUpUsers.stream().anyMatch(u -> u.getId() == user.getId());
			request.setAttribute("isUserParticipant", isUserParticipant);

			event.setAttachments(attachmentDAO.getAttachmentsForParent("EVENT", eventId, userRoleForAttachments));
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
			}

			request.getRequestDispatcher("/views/public/eventDetails.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Event-ID.");
		}
	}
}