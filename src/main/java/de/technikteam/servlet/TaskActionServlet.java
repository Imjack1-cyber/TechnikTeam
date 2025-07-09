package de.technikteam.servlet;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventTaskDAO;
import de.technikteam.model.Event;
import de.technikteam.model.EventTask;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/task-action")
public class TaskActionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(TaskActionServlet.class);
	private EventTaskDAO taskDAO;
	private EventDAO eventDAO;

	@Override
	public void init() {
		taskDAO = new EventTaskDAO();
		eventDAO = new EventDAO();
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
		boolean hasPermission = user.getPermissions().contains("ACCESS_ADMIN_PANEL")
				|| user.getPermissions().contains("EVENT_MANAGE_TASKS")
				|| (event != null && user.getId() == event.getLeaderUserId());

		if (!hasPermission) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
			return;
		}

		boolean isUpdate = !request.getParameter("taskId").isEmpty();
		EventTask task = new EventTask();
		task.setEventId(eventId);
		task.setDescription(request.getParameter("description"));
		task.setDetails(request.getParameter("details"));
		task.setDisplayOrder(Integer.parseInt(request.getParameter("displayOrder")));

		if (isUpdate) {
			task.setId(Integer.parseInt(request.getParameter("taskId")));
			task.setStatus(request.getParameter("status"));
		}

		String assignmentType = request.getParameter("assignmentType");
		int[] userIds = null;
		if ("direct".equals(assignmentType)) {
			String[] userIdsStr = request.getParameterValues("userIds");
			userIds = userIdsStr == null ? new int[0] : Arrays.stream(userIdsStr).mapToInt(Integer::parseInt).toArray();
			task.setRequiredPersons(0);
		} else {
			task.setRequiredPersons(Integer.parseInt(request.getParameter("requiredPersons")));
		}

		String[] itemIds = request.getParameterValues("itemIds");
		String[] itemQuantities = request.getParameterValues("itemQuantities");
		String[] kitIds = request.getParameterValues("kitIds");

		int taskId = taskDAO.saveTask(task, userIds, itemIds, itemQuantities, kitIds);

		if (taskId > 0) {
			String logAction = isUpdate ? "UPDATE_TASK" : "CREATE_TASK";
			String logDetails = String.format("Aufgabe '%s' (ID: %d) für Event '%s' (ID: %d) %s.",
					task.getDescription(), taskId, event.getName(), eventId, isUpdate ? "aktualisiert" : "erstellt");
			AdminLogService.log(user.getUsername(), logAction, logDetails);
			request.getSession().setAttribute("successMessage", "Aufgabe erfolgreich gespeichert.");
		} else {
			request.getSession().setAttribute("errorMessage", "Fehler beim Speichern der Aufgabe.");
		}
		response.sendRedirect(request.getContextPath() + "/veranstaltungen/details?id=" + eventId);
	}

	private void handleDeleteTask(HttpServletRequest request, HttpServletResponse response, User user)
			throws IOException {
		int taskId = Integer.parseInt(request.getParameter("taskId"));
		int eventId = Integer.parseInt(request.getParameter("eventId"));

		EventTask taskToDelete = taskDAO.getTaskById(taskId);
		if (taskToDelete == null) {
			request.getSession().setAttribute("errorMessage", "Fehler: Aufgabe nicht gefunden.");
			response.sendRedirect(request.getContextPath() + "/veranstaltungen/details?id=" + eventId);
			return;
		}

		Event event = eventDAO.getEventById(taskToDelete.getEventId());
		boolean hasPermission = user.getPermissions().contains("ACCESS_ADMIN_PANEL")
				|| user.getPermissions().contains("EVENT_MANAGE_TASKS")
				|| (event != null && user.getId() == event.getLeaderUserId());

		if (!hasPermission) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
			return;
		}

		if (taskDAO.deleteTask(taskId)) {
			String eventName = event != null ? event.getName() : "Unbekannt";
			AdminLogService.log(user.getUsername(), "DELETE_TASK", "Aufgabe '" + taskToDelete.getDescription()
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
		boolean isAdmin = user.getPermissions().contains("ACCESS_ADMIN_PANEL")
				|| user.getPermissions().contains("EVENT_MANAGE_TASKS");
		boolean isTaskAssignee = taskDAO.isUserAssignedToTask(taskId, user.getId());

		boolean isUserAssigned = eventDAO.getAssignedUsersForEvent(event.getId()).stream()
				.anyMatch(u -> u.getId() == user.getId());
		boolean isUserParticipant = eventDAO.getSignedUpUsersForEvent(event.getId()).stream()
				.anyMatch(u -> u.getId() == user.getId());

		switch (action) {
		case "updateStatus":
			if (!isAdmin && !isLeader && !isTaskAssignee) {
				logger.warn("Authorization DENIED for user '{}' to update status on task {}", user.getUsername(),
						taskId);
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
			if (!isUserAssigned && !isUserParticipant) {
				logger.warn("Authorization DENIED for user '{}' to {} task {}. Not a participant.", user.getUsername(),
						action, taskId);
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
				return;
			}

			boolean success = false;
			String errorMessage = "";
			if ("claim".equals(action)) {
				success = taskDAO.claimTask(taskId, user.getId());
				errorMessage = "Aufgabe konnte nicht übernommen werden (vielleicht schon voll?).";
			} else {
				success = taskDAO.unclaimTask(taskId, user.getId());
				errorMessage = "Aufgabe konnte nicht zurückgegeben werden.";
			}

			if (success) {
				response.sendRedirect(request.getHeader("Referer"));
			} else {
				request.getSession().setAttribute("errorMessage", errorMessage);
				response.sendRedirect(request.getHeader("Referer"));
			}
			break;
		}
	}
}