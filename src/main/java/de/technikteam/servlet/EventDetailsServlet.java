package de.technikteam.servlet;

import de.technikteam.dao.EventAttachmentDAO;
import de.technikteam.dao.EventChatDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventTaskDAO;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.stream.Collectors;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mapped to `/eventDetails`, this servlet is responsible for displaying the
 * detailed view of a single event. It fetches all relevant data for the event,
 * including its description, skill requirements, and assigned team. If the
 * event is currently 'LAUFEND' (running), it also fetches associated tasks and
 * chat history. It forwards all this data to `eventDetails.jsp`.
 */
@WebServlet("/eventDetails")
public class EventDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(EventDetailsServlet.class);
	private EventDAO eventDAO;
	private EventTaskDAO taskDAO;
	private EventChatDAO chatDAO;
	private EventAttachmentDAO attachmentDAO;

	@Override
	public void init() {
		eventDAO = new EventDAO();
		taskDAO = new EventTaskDAO();
		chatDAO = new EventChatDAO();
		attachmentDAO = new EventAttachmentDAO();
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

			// Fetch base data applicable to all event statuses
			event.setSkillRequirements(eventDAO.getSkillRequirementsForEvent(eventId));
			event.setReservedItems(eventDAO.getReservedItemsForEvent(eventId));

			String userRoleForAttachments = (user.getRole().equals("ADMIN") || user.getId() == event.getLeaderUserId())
					? "ADMIN"
					: "NUTZER";
			event.setAttachments(attachmentDAO.getAttachmentsForEvent(eventId, userRoleForAttachments));

			List<User> assignedUsers = eventDAO.getAssignedUsersForEvent(eventId);
			event.setAssignedAttendees(assignedUsers);

			// Fetch data specific to running events (tasks, chat)
			if ("LAUFEND".equalsIgnoreCase(event.getStatus())) {
				logger.debug("Event {} is running. Fetching tasks and chat messages.", eventId);
				event.setEventTasks(taskDAO.getTasksForEvent(eventId));
				event.setChatMessages(chatDAO.getMessagesForEvent(eventId));
			}

			// For Admins and Users, provide the list of assigned users for the task assignment
			// dropdown
			if ("ADMIN".equalsIgnoreCase(user.getRole()) || "NUTZER".equalsIgnoreCase(user.getRole()) {
				request.setAttribute("assignedUsers", assignedUsers);
			}

			// For regular users, determine if they are part of the assigned team to show
			// relevant UI
			Set<Integer> assignedUserIds = assignedUsers.stream().map(User::getId).collect(Collectors.toSet());
			boolean isUserAssigned = assignedUserIds.contains(user.getId());
			request.setAttribute("isUserAssigned", isUserAssigned);

			request.setAttribute("event", event);
			logger.debug("Forwarding to eventDetails.jsp for event '{}'", event.getName());
			request.getRequestDispatcher("/eventDetails.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			logger.error("Invalid event ID format in request.", e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Event-ID.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while fetching event details.", e);
			response.sendRedirect(request.getContextPath() + "/error500.jsp");
		}
	}
}
