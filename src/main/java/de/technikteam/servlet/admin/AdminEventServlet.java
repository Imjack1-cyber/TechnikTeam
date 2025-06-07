package de.technikteam.servlet.admin;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.Course;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import de.technikteam.service.NotificationService;

@WebServlet("/admin/events")
public class AdminEventServlet extends HttpServlet {
	private static final Logger logger = LogManager.getLogger(AdminEventServlet.class);
	private EventDAO eventDAO;
	private CourseDAO courseDAO;

	public void init() {
		eventDAO = new EventDAO();
		courseDAO = new CourseDAO(); // Initialisieren!
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action");
		if (action == null) {
			action = "list"; // Default action
		}

		try {
			switch (action) {
			case "new":
			case "edit":
				showEditForm(request, response);
				break;
			case "assign":
				showAssignForm(request, response);
				break;
			case "list":
			default:
				listEvents(request, response);
				break;
			}
		} catch (Exception e) {
			logger.error("Error in doGet of AdminEventServlet", e);
			request.getSession().setAttribute("errorMessage", "Ein unerwarteter Fehler ist aufgetreten.");
			response.sendRedirect(request.getContextPath() + "/admin/events");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String action = request.getParameter("action");
		if (action == null) {
			response.sendRedirect(request.getContextPath() + "/admin/events");
			return;
		}

		try {
			switch (action) {
			case "create":
			case "update":
				handleCreateOrUpdate(request, response);
				break;
			case "delete":
				handleDelete(request, response);
				break;
			case "saveAssignments":
				handleSaveAssignments(request, response);
				break;
			default:
				response.sendRedirect(request.getContextPath() + "/admin/events");
				break;
			}
		} catch (Exception e) {
			logger.error("Error in doPost of AdminEventServlet", e);
			request.getSession().setAttribute("errorMessage", "Ein unerwarteter Fehler ist aufgetreten.");
			response.sendRedirect(request.getContextPath() + "/admin/events");
		}
	}

	private void listEvents(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<Event> events = eventDAO.getAllEvents();
		request.setAttribute("eventList", events);
		request.getRequestDispatcher("/admin/admin_events_list.jsp").forward(request, response);
	}

	private void showEditForm(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<Course> allCourses = courseDAO.getAllCourses(); // Alle verfügbaren Lehrgänge laden
		request.setAttribute("allCourses", allCourses);

		if ("edit".equals(request.getParameter("action"))) {
			int id = Integer.parseInt(request.getParameter("id"));
			Event event = eventDAO.getEventById(id);
			event.setSkillRequirements(eventDAO.getSkillRequirementsForEvent(id)); // Anforderungen laden
			request.setAttribute("event", event);
		}
		request.getRequestDispatcher("/admin/admin_event_form.jsp").forward(request, response);
	}

	private void showAssignForm(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int eventId = Integer.parseInt(request.getParameter("id"));
		Event event = eventDAO.getEventById(eventId);
		List<User> signedUpUsers = eventDAO.getSignedUpUsersForEvent(eventId);
		List<User> assignedUsers = eventDAO.getAssignedUsersForEvent(eventId);

		request.setAttribute("event", event);
		request.setAttribute("signedUpUsers", signedUpUsers);
		request.setAttribute("assignedUserIds", assignedUsers.stream().map(User::getId).collect(Collectors.toSet()));

		request.getRequestDispatcher("/admin/admin_event_assign.jsp").forward(request, response);
	}

	private void handleCreateOrUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Event event = new Event();
		String[] requiredCourseIds = request.getParameterValues("requiredCourseId");
		String[] requiredPersons = request.getParameterValues("requiredPersons");
		String[] skillNames = request.getParameterValues("skillName");
		event.setName(request.getParameter("name"));
		event.setDescription(request.getParameter("description"));
		try {
			event.setEventDateTime(LocalDateTime.parse(request.getParameter("eventDateTime")));
		} catch (DateTimeParseException e) {
			logger.error("Invalid date format submitted.", e);
			request.getSession().setAttribute("errorMessage", "Ungültiges Datumsformat.");
			response.sendRedirect(request.getContextPath() + "/admin/events");
			return;
		}

		boolean success;
		String action = request.getParameter("id") != null && !request.getParameter("id").isEmpty() ? "update"
				: "create";

		if ("update".equals(action)) {
			event.setId(Integer.parseInt(request.getParameter("id")));
			event.setStatus(request.getParameter("status")); // Allow status update from form
			success = eventDAO.updateEvent(event);
			request.getSession().setAttribute("successMessage",
					"Event '" + event.getName() + "' erfolgreich aktualisiert.");
		} else {
			event.setStatus("GEPLANT"); // Default status
			success = eventDAO.createEvent(event);
			request.getSession().setAttribute("successMessage",
					"Event '" + event.getName() + "' erfolgreich erstellt.");
		}

		if (!success) {
			request.getSession().setAttribute("errorMessage", "Operation am Event fehlgeschlagen.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/events");
	}

	private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		int id = Integer.parseInt(request.getParameter("id"));
		if (eventDAO.deleteEvent(id)) {
			request.getSession().setAttribute("successMessage", "Event erfolgreich gelöscht.");
		} else {
			request.getSession().setAttribute("errorMessage", "Event konnte nicht gelöscht werden.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/events");
	}

	private void handleSaveAssignments(HttpServletRequest request, HttpServletResponse response) throws IOException {
		int eventId = Integer.parseInt(request.getParameter("eventId"));
		String[] userIds = request.getParameterValues("userIds");

		eventDAO.assignUsersToEvent(eventId, userIds);

		Event event = eventDAO.getEventById(eventId);
		if (event != null) {
			event.setStatus("KOMPLETT");
			eventDAO.updateEvent(event);
			request.getSession().setAttribute("successMessage",
					"Teilnehmer zugewiesen und Event als 'Komplett' markiert.");

			// Send notification to all connected clients
			String notificationMessage = "Das Team für das Event '" + event.getName() + "' steht fest!";
			NotificationService.getInstance().sendNotification(notificationMessage);
		}
		response.sendRedirect(request.getContextPath() + "/admin/events");
	}
}