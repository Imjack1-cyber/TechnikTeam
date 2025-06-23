package de.technikteam.servlet.admin;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.technikteam.config.LocalDateAdapter;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.Course;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 * Mapped to /admin/events, this is a complex servlet that manages all aspects
 * 
 * of events from an administrative perspective. It handles full CRUD operations
 * 
 * (create, read, update, delete) for events, manages skill requirements,
 * 
 * updates event statuses, and provides the interface for assigning users to an
 * 
 * event's final team. All create/edit operations are handled via modals on the
 * 
 * list page.
 */
@WebServlet("/admin/events")
public class AdminEventServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminEventServlet.class);

	private EventDAO eventDAO;
	private CourseDAO courseDAO;
	private Gson gson;

	@Override
	public void init() {
		eventDAO = new EventDAO();
		courseDAO = new CourseDAO();
		// Configure Gson to handle LocalDateTime and LocalDate, which it doesn't by
		// default
		gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
				.registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter()).create();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action") == null ? "list" : req.getParameter("action");
		logger.debug("AdminEventServlet received GET request with action: {}", action);
		try {
			switch (action) {
			case "assign":
				showAssignForm(req, resp);
				break;
			case "getEventData":
				getEventDataAsJson(req, resp);
				break;
			default:
				listEvents(req, resp);
				break;
			}
		} catch (Exception e) {
			logger.error("Error in AdminEventServlet doGet", e);
			req.getSession().setAttribute("errorMessage", "Ein Fehler ist aufgetreten: " + e.getMessage());
			resp.sendRedirect(req.getContextPath() + "/admin/events");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");
		logger.debug("AdminEventServlet received POST request with action: {}", action);
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
		default:
			logger.warn("Unknown POST action received: {}", action);
			resp.sendRedirect(req.getContextPath() + "/admin/events");
			break;
		}
	}

	private void listEvents(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.info("Listing all events for admin view.");
		List<Event> eventList = eventDAO.getAllEvents();
		List<Course> allCourses = courseDAO.getAllCourses();
		req.setAttribute("eventList", eventList);
		req.setAttribute("allCourses", allCourses);
		req.getRequestDispatcher("/admin/admin_events_list.jsp").forward(req, resp);
	}

	private void getEventDataAsJson(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			int eventId = Integer.parseInt(req.getParameter("id"));
			Event event = eventDAO.getEventById(eventId);
			if (event != null) {
				event.setSkillRequirements(eventDAO.getSkillRequirementsForEvent(eventId));
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

	private void showAssignForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			int eventId = Integer.parseInt(req.getParameter("id"));
			logger.info("Showing user assignment form for event ID: {}", eventId);
			Event event = eventDAO.getEventById(eventId);
			List<User> signedUpUsers = eventDAO.getSignedUpUsersForEvent(eventId);
			List<User> assignedUsers = eventDAO.getAssignedUsersForEvent(eventId);
			Set<Integer> assignedUserIds = assignedUsers.stream().map(User::getId).collect(Collectors.toSet());

			req.setAttribute("event", event);
			req.setAttribute("signedUpUsers", signedUpUsers);
			req.setAttribute("assignedUserIds", assignedUserIds);

			req.getRequestDispatcher("/admin/admin_event_assign.jsp").forward(req, resp);
		} catch (NumberFormatException e) {
			logger.error("Invalid event ID for assignment form.", e);
			req.getSession().setAttribute("errorMessage", "Ungültige Event-ID.");
			resp.sendRedirect(req.getContextPath() + "/admin/events");
		}
	}

	private void handleCreateOrUpdate(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		User adminUser = (User) request.getSession().getAttribute("user");
		String idParam = request.getParameter("id");
		boolean isUpdate = idParam != null && !idParam.isEmpty();
		try {
			Event event = new Event();
			event.setName(request.getParameter("name"));
			event.setDescription(request.getParameter("description"));
			event.setEventDateTime(LocalDateTime.parse(request.getParameter("eventDateTime")));

			String endDateTimeParam = request.getParameter("endDateTime");
			if (endDateTimeParam != null && !endDateTimeParam.isEmpty()) {
				event.setEndDateTime(LocalDateTime.parse(endDateTimeParam));
			}

			boolean success = false;
			String actionType;
			Event originalEvent = null;

			if (isUpdate) {
				actionType = "UPDATE_EVENT";
				int eventId = Integer.parseInt(idParam);
				originalEvent = eventDAO.getEventById(eventId);
				event.setId(eventId);
				event.setStatus(originalEvent.getStatus()); // Status is updated via its own action
				logger.info("Attempting to update event ID: {}", eventId);
				success = eventDAO.updateEvent(event);
				if (success) {
					AdminLogService.log(adminUser.getUsername(), actionType,
							"Event '" + event.getName() + "' (ID: " + eventId + ") aktualisiert.");
				}
			} else { // CREATE
				actionType = "CREATE_EVENT";
				logger.info("Attempting to create new event: {}", event.getName());
				int newEventId = eventDAO.createEvent(event);
				if (newEventId > 0) {
					success = true;
					event.setId(newEventId);
					AdminLogService.log(adminUser.getUsername(), actionType,
							"Event '" + event.getName() + "' (ID: " + newEventId + ") erstellt.");
				}
			}

			if (success) {
				String[] requiredCourseIds = request.getParameterValues("requiredCourseId");
				String[] requiredPersons = request.getParameterValues("requiredPersons");
				eventDAO.saveSkillRequirements(event.getId(), requiredCourseIds, requiredPersons);
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

		response.sendRedirect(request.getContextPath() + "/admin/events");
	}

	private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User adminUser = (User) req.getSession().getAttribute("user");
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
		resp.sendRedirect(req.getContextPath() + "/admin/events");
	}

	private void handleAssignUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User adminUser = (User) req.getSession().getAttribute("user");
		try {
			int eventId = Integer.parseInt(req.getParameter("eventId"));
			String[] userIds = req.getParameterValues("userIds");
			logger.info("Assigning {} users to event ID {}", (userIds != null ? userIds.length : 0), eventId);
			Event event = eventDAO.getEventById(eventId);
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
		resp.sendRedirect(req.getContextPath() + "/admin/events");
	}

	private void handleStatusUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User adminUser = (User) req.getSession().getAttribute("user");
		try {
			int eventId = Integer.parseInt(req.getParameter("id"));
			String newStatus = req.getParameter("newStatus");
			logger.info("Updating status for event ID {} to '{}'", eventId, newStatus);
			Event event = eventDAO.getEventById(eventId);
			if (event != null && eventDAO.updateEventStatus(eventId, newStatus)) {
				String logDetails = String.format("Status für Event '%s' (ID: %d) von '%s' auf '%s' geändert.",
						event.getName(), eventId, event.getStatus(), newStatus);
				AdminLogService.log(adminUser.getUsername(), "UPDATE_EVENT_STATUS", logDetails);
				req.getSession().setAttribute("successMessage", "Event-Status erfolgreich aktualisiert.");
			} else {
				req.getSession().setAttribute("errorMessage", "Fehler beim Aktualisieren des Event-Status.");
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid event ID format for status update.", e);
			req.getSession().setAttribute("errorMessage", "Ungültige Event-ID.");
		}
		resp.sendRedirect(req.getContextPath() + "/admin/events");
	}

}