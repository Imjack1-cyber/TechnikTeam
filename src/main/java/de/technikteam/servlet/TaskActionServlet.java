package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventTaskDAO;
import de.technikteam.model.Event;
import de.technikteam.model.EventTask;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

@Singleton
public class TaskActionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(TaskActionServlet.class);
	private final EventTaskDAO taskDAO;
	private final EventDAO eventDAO;
	private final AdminLogService adminLogService;

	@Inject
	public TaskActionServlet(EventTaskDAO taskDAO, EventDAO eventDAO, AdminLogService adminLogService) {
		this.taskDAO = taskDAO;
		this.eventDAO = eventDAO;
		this.adminLogService = adminLogService;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		User user = (User) request.getSession().getAttribute("user");
		String action = request.getParameter("action");

		if (user == null || action == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		if (!CSRFUtil.isTokenValid(request)) {
			logger.warn("CSRF token validation failed for task action by user '{}'", user.getUsername());
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}

		try {
			switch (action) {
			case "save":
				handleSaveTask(request, response, user);
				break;
			case "delete":
				handleDeleteTask(request, response, user);
				break;
			case "updateStatus":
			case "claim":
			case "unclaim":
				handleUserTaskAction(request, response, user, action);
				break;
			default:
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unbekannte Aktion.");
				break;
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid ID format in task action request.", e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige ID.");
		}
	}

	private void handleSaveTask(HttpServletRequest request, HttpServletResponse response, User user)
			throws IOException {
		int eventId = Integer.parseInt(request.getParameter("eventId"));
		Event event = eventDAO.getEventById(eventId);
		boolean hasPermission = user.hasAdminAccess() || (event != null && user.getId() == event.getLeaderUserId());

		if (!hasPermission) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
			return;
		}

		try {
			int displayOrder = Integer.parseInt(request.getParameter("displayOrder"));
			if (displayOrder < 0) {
				request.getSession().setAttribute("errorMessage", "Die Anzeigereihenfolge darf nicht negativ sein.");
				response.sendRedirect(request.getContextPath() + "/veranstaltungen/details?id=" + eventId);
				return;
			}

			int requiredPersons = 0;
			String assignmentType = request.getParameter("assignmentType");
			if (!"direct".equals(assignmentType)) {
				long requiredPersonsLong = Long.parseLong(request.getParameter("requiredPersons"));
				if (requiredPersonsLong > Integer.MAX_VALUE || requiredPersonsLong < 0) {
					request.getSession().setAttribute("errorMessage",
							"Die Anzahl der benötigten Personen ist ungültig.");
					response.sendRedirect(request.getContextPath() + "/veranstaltungen/details?id=" + eventId);
					return;
				}
				requiredPersons = (int) requiredPersonsLong;
			}

			boolean isUpdate = !request.getParameter("taskId").isEmpty();
			EventTask task = new EventTask();
			task.setEventId(eventId);
			task.setDescription(request.getParameter("description"));
			task.setDetails(request.getParameter("details"));
			task.setDisplayOrder(displayOrder);
			task.setRequiredPersons(requiredPersons);

			if (isUpdate) {
				task.setId(Integer.parseInt(request.getParameter("taskId")));
				task.setStatus(request.getParameter("status"));
			}

			int[] userIds = null;
			if ("direct".equals(assignmentType)) {
				String[] userIdsStr = request.getParameterValues("userIds");
				userIds = userIdsStr == null ? new int[0]
						: Arrays.stream(userIdsStr).mapToInt(Integer::parseInt).toArray();
			}

			String[] itemIds = request.getParameterValues("itemIds");
			String[] itemQuantities = request.getParameterValues("itemQuantities");
			String[] kitIds = request.getParameterValues("kitIds");

			int taskId = taskDAO.saveTask(task, userIds, itemIds, itemQuantities, kitIds);

			if (taskId > 0) {
				String logAction = isUpdate ? "UPDATE_TASK" : "CREATE_TASK";
				String logDetails = String.format("Aufgabe '%s' (ID: %d) für Event '%s' (ID: %d) %s.",
						task.getDescription(), taskId, event.getName(), eventId,
						isUpdate ? "aktualisiert" : "erstellt");
				adminLogService.log(user.getUsername(), logAction, logDetails);
				request.getSession().setAttribute("successMessage", "Aufgabe erfolgreich gespeichert.");
			} else {
				request.getSession().setAttribute("errorMessage", "Fehler beim Speichern der Aufgabe.");
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid number format during task save.", e);
			request.getSession().setAttribute("errorMessage",
					"Ungültiges Zahlenformat für Reihenfolge oder benötigte Personen.");
		}
		response.sendRedirect(request.getContextPath() + "/veranstaltungen/details?id=" + eventId);
	}

	private void handleDeleteTask(HttpServletRequest request, HttpServletResponse response, User user)
			throws IOException {
		int taskId = Integer.parseInt(request.getParameter("taskId"));
		EventTask taskToDelete = taskDAO.getTaskById(taskId);
		if (taskToDelete == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Task not found.");
			return;
		}
		int eventId = taskToDelete.getEventId();
		Event event = eventDAO.getEventById(eventId);
		boolean hasPermission = user.hasAdminAccess() || (event != null && user.getId() == event.getLeaderUserId());

		if (!hasPermission) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
			return;
		}

		if (taskDAO.deleteTask(taskId)) {
			String eventName = event != null ? event.getName() : "Unbekannt";
			adminLogService.log(user.getUsername(), "DELETE_TASK", "Aufgabe '" + taskToDelete.getDescription()
					+ "' (ID: " + taskId + ") von Event '" + eventName + "' gelöscht.");
			request.getSession().setAttribute("successMessage", "Aufgabe erfolgreich gelöscht.");
		} else {
			request.getSession().setAttribute("errorMessage", "Aufgabe konnte nicht gelöscht werden.");
		}
		response.sendRedirect(request.getContextPath() + "/veranstaltungen/details?id=" + eventId);
	}

	private void handleUserTaskAction(HttpServletRequest request, HttpServletResponse response, User user,
			String action) throws IOException {
		int taskId = Integer.parseInt(request.getParameter("taskId"));
		EventTask task = taskDAO.getTaskById(taskId);
		if (task == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Task not found.");
			return;
		}
		Event event = eventDAO.getEventById(task.getEventId());
		if (event == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Associated event not found.");
			return;
		}

		boolean isLeader = event.getLeaderUserId() == user.getId();
		boolean isAdmin = user.hasAdminAccess();
		boolean isTaskAssignee = taskDAO.isUserAssignedToTask(taskId, user.getId());
		boolean isUserParticipant = eventDAO.isUserAssociatedWithEvent(event.getId(), user.getId());

		switch (action) {
		case "updateStatus":
			if (!isAdmin && !isLeader && !isTaskAssignee) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
				return;
			}
			String status = request.getParameter("status");
			if (taskDAO.updateTaskStatus(taskId, status)) {
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Status konnte nicht aktualisiert werden.");
			}
			break;
		case "claim":
		case "unclaim":
			if (!isUserParticipant) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
				return;
			}
			boolean success = "claim".equals(action) ? taskDAO.claimTask(taskId, user.getId())
					: taskDAO.unclaimTask(taskId, user.getId());
			if (!success) {
				request.getSession().setAttribute("errorMessage", "Aktion fehlgeschlagen.");
			}
			response.sendRedirect(request.getHeader("Referer"));
			break;
		}
	}
}