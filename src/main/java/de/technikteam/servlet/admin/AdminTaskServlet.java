package de.technikteam.servlet.admin;

import de.technikteam.dao.EventTaskDAO;
import de.technikteam.model.EventTask;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Mapped to `/admin/tasks`, this servlet manages administrative actions for
 * event tasks. It handles POST requests for creating and assigning tasks, and
 * DELETE requests for removing them. All actions are logged to the admin audit
 * trail. It's designed to be called via forms or AJAX from the event details
 * page.
 */
@WebServlet("/admin/tasks")
public class AdminTaskServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminTaskServlet.class);
	private EventTaskDAO taskDAO;

	@Override
	public void init() {
		taskDAO = new EventTaskDAO();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		logger.debug("AdminTaskServlet received POST with action: {}", action);

		switch (action) {
		case "create":
			handleCreateTask(request, response);
			break;
		case "assign":
			handleAssignTask(request, response);
			break;
		default:
			logger.warn("Unknown POST action received: {}", action);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
			break;
		}
	}

	private void handleCreateTask(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		int eventId = Integer.parseInt(request.getParameter("eventId"));
		String description = request.getParameter("description");
		logger.info("Creating new task '{}' for event ID {}", description, eventId);

		EventTask newTask = new EventTask();
		newTask.setEventId(eventId);
		newTask.setDescription(description);

		int newTaskId = taskDAO.createTask(newTask);
		if (newTaskId > 0) {
			AdminLogService.log(adminUser.getUsername(), "CREATE_TASK",
					"Aufgabe '" + description + "' (ID: " + newTaskId + ") für Event-ID " + eventId + " erstellt.");
		}
		response.sendRedirect(request.getContextPath() + "/eventDetails?id=" + eventId);
	}

	private void handleAssignTask(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		int eventId = Integer.parseInt(request.getParameter("eventId"));
		int taskId = Integer.parseInt(request.getParameter("taskId"));
		String[] userIdsStr = request.getParameterValues("userIds");
		int[] userIds = userIdsStr == null ? new int[0]
				: Arrays.stream(userIdsStr).mapToInt(Integer::parseInt).toArray();

		logger.info("Assigning task ID {} to {} users for event ID {}", taskId, userIds.length, eventId);
		taskDAO.assignTaskToUsers(taskId, userIds);

		String assignedUserIdsString = userIds.length > 0
				? Arrays.stream(userIds).mapToObj(String::valueOf).collect(Collectors.joining(", "))
				: "niemandem";
		AdminLogService.log(adminUser.getUsername(), "ASSIGN_TASK", "Aufgabe (ID: " + taskId + ") für Event-ID "
				+ eventId + " an Nutzer-IDs [" + assignedUserIdsString + "] zugewiesen.");

		response.sendRedirect(request.getContextPath() + "/eventDetails?id=" + eventId);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getSession().getAttribute("user");
		try {
			int taskId = Integer.parseInt(req.getParameter("taskId"));
			logger.warn("Attempting to delete task ID {} by user '{}'", taskId, adminUser.getUsername());
			if (taskDAO.deleteTask(taskId)) {
				AdminLogService.log(adminUser.getUsername(), "DELETE_TASK", "Aufgabe mit ID " + taskId + " gelöscht.");
				resp.setStatus(HttpServletResponse.SC_OK);
			} else {
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid task ID format for deletion.", e);
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID.");
		}
	}
}