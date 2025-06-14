package de.technikteam.servlet.admin;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.Course;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import de.technikteam.service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/events")
public class AdminEventServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
			action = "list";
		}

		try {
			switch (action) {
			case "new":
				showForm(request, response); // Ruft das leere Formular auf
				break;
			case "edit":
				// ID wird nur für "edit" benötigt
				int editId = Integer.parseInt(request.getParameter("id"));
				showForm(request, response, editId);
				break;
			case "assign":
				// ID wird auch für "assign" benötigt
				int assignId = Integer.parseInt(request.getParameter("id"));
				showAssignForm(request, response, assignId);
				break;
			case "list":
			default:
				listEvents(request, response);
				break;
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid or missing ID parameter for action: {}", action, e);
			request.getSession().setAttribute("errorMessage", "Ungültige oder fehlende ID für die ausgewählte Aktion.");
			response.sendRedirect(request.getContextPath() + "/admin/events");
		} catch (Exception e) {
			logger.error("Error in doGet of AdminEventServlet", e);
			request.getSession().setAttribute("errorMessage", "Ein unerwarteter Fehler ist aufgetreten.");
			response.sendRedirect(request.getContextPath() + "/admin/events");
		}
	}

	// Überladen Sie showForm, um beide Fälle (neu und bearbeiten) zu behandeln
	private void showForm(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<Course> allCourses = courseDAO.getAllCourses();
		request.setAttribute("allCourses", allCourses);
		request.getRequestDispatcher("/admin/admin_event_form.jsp").forward(request, response);
	}

	private void showForm(HttpServletRequest request, HttpServletResponse response, int eventId)
			throws ServletException, IOException {
		List<Course> allCourses = courseDAO.getAllCourses();
		request.setAttribute("allCourses", allCourses);
		Event event = eventDAO.getEventById(eventId);
		if (event != null) {
			event.setSkillRequirements(eventDAO.getSkillRequirementsForEvent(eventId));
		}
		request.setAttribute("event", event);
		request.getRequestDispatcher("/admin/admin_event_form.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");
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
			case "delete": // <-- DIESER FALL HAT WAHRSCHEINLICH GEFEHLT
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

	private void showAssignForm(HttpServletRequest request, HttpServletResponse response, int eventId)
			throws ServletException, IOException {
		eventId = Integer.parseInt(request.getParameter("id"));
		Event event = eventDAO.getEventById(eventId);
		List<User> signedUpUsers = eventDAO.getSignedUpUsersForEvent(eventId);
		List<User> assignedUsers = eventDAO.getAssignedUsersForEvent(eventId);

		request.setAttribute("event", event);
		request.setAttribute("signedUpUsers", signedUpUsers);
		request.setAttribute("assignedUserIds", assignedUsers.stream().map(User::getId).collect(Collectors.toSet()));

		request.getRequestDispatcher("/admin/admin_event_assign.jsp").forward(request, response);
	}

	// Ersetzen Sie die bestehende handleCreateOrUpdate-Methode im AdminEventServlet

	private void handleCreateOrUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Event event = new Event();
		event.setName(request.getParameter("name"));
		event.setDescription(request.getParameter("description"));
		try {
			event.setEventDateTime(LocalDateTime.parse(request.getParameter("eventDateTime")));
		} catch (DateTimeParseException e) {
			/* ... Fehlerbehandlung ... */ }

		String idParam = request.getParameter("id");
		int savedEventId = 0;

		if (idParam != null && !idParam.isEmpty()) {
			// UPDATE
			event.setId(Integer.parseInt(idParam));
			event.setStatus(request.getParameter("status"));
			if (eventDAO.updateEvent(event)) {
				savedEventId = event.getId();
				request.getSession().setAttribute("successMessage", "Event erfolgreich aktualisiert.");
			} else {
				request.getSession().setAttribute("errorMessage", "Event konnte nicht aktualisiert werden.");
			}
		} else {
			// CREATE
			savedEventId = eventDAO.createEvent(event);
			if (savedEventId > 0) {
				request.getSession().setAttribute("successMessage", "Event erfolgreich erstellt.");
			} else {
				request.getSession().setAttribute("errorMessage", "Event konnte nicht erstellt werden.");
			}
		}

		// Jetzt die Anforderungen speichern, falls das Event erfolgreich gespeichert
		// wurde
		if (savedEventId > 0) {
			String[] requiredCourseIds = request.getParameterValues("requiredCourseId");
			String[] requiredPersons = request.getParameterValues("requiredPersons");
			eventDAO.saveSkillRequirements(savedEventId, requiredCourseIds, requiredPersons);
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