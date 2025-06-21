package de.technikteam.servlet.admin;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 * Mapped to /admin/events, this is a complex servlet that manages all aspects
 * of events from an administrative perspective. It handles full CRUD operations
 * (create, read, update, delete) for events, manages skill requirements, and
 * provides the interface for assigning users to events. It forwards to
 * admin_events_list.jsp, admin_event_form.jsp, and admin_event_assign.jsp.
 */

@WebServlet("/admin/events")
public class AdminEventServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminEventServlet.class);

	private EventDAO eventDAO;
	private CourseDAO courseDAO; // Needed for skill requirements in the form

	@Override
	public void init() {
		eventDAO = new EventDAO();
		courseDAO = new CourseDAO(); // Initialize here
	}

	/**
	 * Routes GET requests based on the 'action' parameter. - list (default): Shows
	 * the list of all events. - new/edit: Shows the form to create or edit an
	 * event. - assign: Shows the form to assign users to an event.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action") == null ? "list" : req.getParameter("action");
		switch (action) {
		case "edit":
		case "new":
			showEventForm(req, resp);
			break;
		case "assign":
			showAssignForm(req, resp);
			break;
		default:
			listEvents(req, resp);
			break;
		}
	}

	/**
	 * Routes POST requests based on the 'action' parameter. - create/update:
	 * Handles event creation and updates. - delete: Handles event deletion. -
	 * assignUsers: Handles the final assignment of users.
	 */
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
		default:
			resp.sendRedirect(req.getContextPath() + "/admin/events");
			break;
		}
	}

	/**
	 * Fetches all events and forwards to the list view.
	 */
	private void listEvents(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		List<Event> eventList = eventDAO.getAllEvents();
		req.setAttribute("eventList", eventList);
		req.getRequestDispatcher("/admin/admin_events_list.jsp").forward(req, resp);
	}

	/**
	 * Prepares data for and displays the create/edit event form.
	 */
	private void showEventForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if ("edit".equals(req.getParameter("action"))) {
			int eventId = Integer.parseInt(req.getParameter("id"));
			Event event = eventDAO.getEventById(eventId);
			event.setSkillRequirements(eventDAO.getSkillRequirementsForEvent(eventId)); // Load existing requirements
			req.setAttribute("event", event);
		}
		// Provide all available courses to the form for the dropdown list
		List<Course> allCourses = courseDAO.getAllCourses();
		req.setAttribute("allCourses", allCourses);

		req.getRequestDispatcher("/admin/admin_event_form.jsp").forward(req, resp);
	}

	/**
	 * Prepares data for and displays the user assignment form.
	 */
	private void showAssignForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			int eventId = Integer.parseInt(req.getParameter("id"));
			Event event = eventDAO.getEventById(eventId);
			List<User> signedUpUsers = eventDAO.getSignedUpUsersForEvent(eventId);
			List<User> assignedUsers = eventDAO.getAssignedUsersForEvent(eventId);

			// Convert to a Set for efficient .contains() check in the JSP
			Set<Integer> assignedUserIds = assignedUsers.stream().map(User::getId).collect(Collectors.toSet());

			req.setAttribute("event", event);
			req.setAttribute("signedUpUsers", signedUpUsers);
			req.setAttribute("assignedUserIds", assignedUserIds);

			req.getRequestDispatcher("/admin/admin_event_assign.jsp").forward(req, resp);
		} catch (NumberFormatException e) {
			logger.error("Invalid event ID for assignment form.", e);
			resp.sendRedirect(req.getContextPath() + "/admin/events");
		}
	}

	/**
	 * Processes form submission for creating or updating an event.
	 */

	private void handleCreateOrUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
	    User adminUser = (User) request.getSession().getAttribute("user");
	    String idParam = request.getParameter("id");

	    try {
	        Event event = new Event();
	        event.setName(request.getParameter("name"));
	        event.setDescription(request.getParameter("description"));
	        event.setEventDateTime(LocalDateTime.parse(request.getParameter("eventDateTime")));

	        boolean success = false;

	        if (idParam != null && !idParam.isEmpty()) { // This is an UPDATE
	            int eventId = Integer.parseInt(idParam);
	            Event originalEvent = eventDAO.getEventById(eventId);
	            if (originalEvent == null) { throw new ServletException("Zu aktualisierendes Event nicht gefunden."); }

	            event.setId(eventId);
	            event.setStatus(request.getParameter("status"));

	            List<String> changes = new ArrayList<>();
	            if (!Objects.equals(originalEvent.getName(), event.getName())) changes.add("Name zu '" + event.getName() + "'");
	            if (!Objects.equals(originalEvent.getDescription(), event.getDescription())) changes.add("Beschreibung geändert");
	            if (!originalEvent.getEventDateTime().equals(event.getEventDateTime())) changes.add("Datum zu " + event.getFormattedEventDateTime());
	            if (!Objects.equals(originalEvent.getStatus(), event.getStatus())) changes.add("Status zu '" + event.getStatus() + "'");

	            success = eventDAO.updateEvent(event);
	            if (success) {
	                String logDetails = changes.isEmpty()
	                    ? "Event '" + originalEvent.getName() + "' (ID: " + eventId + ") gespeichert (keine Stammdaten-Änderungen)."
	                    : "Event '" + originalEvent.getName() + "' (ID: " + eventId + ") aktualisiert. Änderungen: " + String.join(", ", changes) + ".";
	                AdminLogService.log(adminUser.getUsername(), "UPDATE_EVENT", logDetails);
	            }

	        } else { // This is a CREATE
	            event.setStatus("GEPLANT"); // Default status for new events
	            int newEventId = eventDAO.createEvent(event);
	            if (newEventId > 0) {
	                success = true;
	                event.setId(newEventId);
	                String logDetails = "Event '" + event.getName() + "' (ID: " + newEventId + ") erstellt.";
	                AdminLogService.log(adminUser.getUsername(), "CREATE_EVENT", logDetails);
	            }
	        }

	        // After creating/updating, save the associated skill requirements
	        if (success) {
	            String[] requiredCourseIds = request.getParameterValues("requiredCourseId");
	            String[] requiredPersons = request.getParameterValues("requiredPersons");
	            // Here you could add more complex logging for skill changes if needed
	            eventDAO.saveSkillRequirements(event.getId(), requiredCourseIds, requiredPersons);
	            request.getSession().setAttribute("successMessage", "Event erfolgreich gespeichert.");
	        } else {
	            request.getSession().setAttribute("errorMessage", "Event konnte nicht gespeichert werden.");
	        }

	    } catch (DateTimeParseException e) {
	        logger.error("Invalid date format submitted for event.", e);
	        request.getSession().setAttribute("errorMessage", "Ungültiges Datumsformat.");
	    } catch (Exception e) {
	        logger.error("Error during event creation/update.", e);
	        request.getSession().setAttribute("errorMessage", "Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage());
	    }

	    response.sendRedirect(request.getContextPath() + "/admin/events");
	}

	/**
	 * Processes request to delete an event.
	 */
	private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		int eventId = Integer.parseInt(req.getParameter("id"));
		Event event = eventDAO.getEventById(eventId); // Get details for logging before deleting

		if (event != null && eventDAO.deleteEvent(eventId)) {
			User adminUser = (User) req.getSession().getAttribute("user");
			AdminLogService.log(adminUser.getUsername(), "DELETE_EVENT",
					"Event '" + event.getName() + "' (ID: " + eventId + ") gelöscht.");
			req.getSession().setAttribute("successMessage", "Event wurde gelöscht.");
		} else {
			req.getSession().setAttribute("errorMessage", "Event konnte nicht gelöscht werden.");
		}
		resp.sendRedirect(req.getContextPath() + "/admin/events");
	}

	/**
	 * Processes form submission for assigning users to an event.
	 */
	private void handleAssignUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	    int eventId = Integer.parseInt(req.getParameter("eventId"));
	    String[] userIds = req.getParameterValues("userIds");
	    User adminUser = (User) req.getSession().getAttribute("user");
	    Event event = eventDAO.getEventById(eventId);

	    // 1. Fetch old assignments for comparison
	    List<String> oldAssignedNames = eventDAO.getAssignedUsersForEvent(eventId)
	                                            .stream().map(User::getUsername)
	                                            .collect(Collectors.toList());

	    // 2. Perform the assignment
	    eventDAO.assignUsersToEvent(eventId, userIds);

	    // 3. Fetch new assignments and log the difference
	    List<String> newAssignedNames = eventDAO.getAssignedUsersForEvent(eventId)
	                                            .stream().map(User::getUsername)
	                                            .collect(Collectors.toList());

	    String logDetails = String.format("Team für Event '%s' (ID: %d) finalisiert. Alt: [%s], Neu: [%s].",
	        event.getName(), eventId, String.join(", ", oldAssignedNames), String.join(", ", newAssignedNames));
	    AdminLogService.log(adminUser.getUsername(), "ASSIGN_TEAM", logDetails);

	    // Also update the event status to "KOMPLETT" as a helpful shortcut
	    if (event != null) {
	        event.setStatus("KOMPLETT");
	        eventDAO.updateEvent(event);
	    }
	    
	    req.getSession().setAttribute("successMessage", "Team für das Event wurde erfolgreich zugewiesen und der Status auf 'Komplett' gesetzt.");
	    resp.sendRedirect(req.getContextPath() + "/admin/events");
	}
}